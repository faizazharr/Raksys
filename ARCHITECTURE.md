# Architecture & Technical Design

Technical documentation for building, understanding, and contributing to **Raksys**.

---

## 🚀 Building & Packaging

Raksys is a multi-module Gradle project (Kotlin DSL + version catalog) targeting the JVM with a **JDK 17** toolchain. The UI is Compose Multiplatform Desktop.

```bash
# Run the app
./gradlew :app:run

# Run all unit tests
./gradlew test

# Run tests for a single module
./gradlew :core:database:test

# Package a native installer for the current OS (.dmg / .exe)
./gradlew :app:packageDistributionForCurrentOS
```

Packaging targets (`Dmg`, `Exe`), the package name (`Raksys`), and the installer version are configured in [`app/build.gradle.kts`](app/build.gradle.kts). Dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

---

## 🏛️ Presenter / Event / UiState Pattern

Each feature is a **Composable screen + a Presenter**. Screens never touch drivers directly; they dispatch typed events and render an immutable state.

```kotlin
// 1. Events: everything a screen can ask for
sealed interface QueryEvent {
    data class Execute(val profile: ConnectionProfile, val sql: String) : QueryEvent
    data object Cancel : QueryEvent
    data object ClearHistory : QueryEvent
}

// 2. Presenter: single entry point + observable state
class QueryPresenter(private val driver: DatabaseDriver) {
    val state: StateFlow<UiState<QueryResult>>
    suspend fun onEvent(event: QueryEvent) { /* ... */ }
}

// 3. UiState (core:model): one shape for every async result
sealed interface UiState<out T> { Idle; Loading; Success(data); Error(message) }
```

- **Unidirectional flow**: `Screen → Event → Presenter → Driver → UiState → Screen`.
- **Uniform async states**: `Idle`, `Loading`, `Success`, `Error` map to shared composables (`RaksysLoadingState`, `RaksysErrorState`, `RaksysEmptyState`) in `core:ui`.
- **Drivers return `Result<T>`**: failures surface as `Result.failure`, never as uncaught exceptions in the UI.
- **Dependency injection via Koin**: presenters and drivers are registered in per-module Koin modules (`queryModule`, `databaseModule`, …) and assembled in `startKoin { }` in `Main.kt`.

---

## 📂 Modular Project Structure

```
Raksys/
├── app/                              # Desktop entry point & shell
│   └── src/main/kotlin/com/raksys/app/
│       ├── Main.kt                   # Koin bootstrap, window, layout, shortcuts, status bar
│       ├── WelcomeWorkspace.kt       # Empty-state landing page
│       └── CommandPaletteDialog.kt   # ⌘K launcher
│
├── core/
│   ├── model/                        # Pure data: ConnectionProfile, DbType, TableSchema,
│   │                                 #   QueryResult, Permission, NoSql, UiState
│   ├── security/                     # CredentialStore + KeyringCredentialStore (OS keyring)
│   ├── database/                     # Drivers & infrastructure
│   │   ├── DatabaseDriver.kt         #   SQL interface        → JdbcDatabaseDriver (HikariCP)
│   │   ├── DocumentDatabaseDriver.kt #   Document interface   → MongoDatabaseDriver
│   │   ├── KeyValueDriver.kt         #   Key-value interface  → RedisKeyValueDriver (Jedis)
│   │   ├── PermissionDriver.kt       #   GRANT/REVOKE         → JdbcPermissionDriver
│   │   ├── SshTunnelManager.kt       #   SSHJ local port-forwarding
│   │   └── ConnectionErrorMapper.kt  #   Raw exception → friendly message
│   └── ui/                           # Design system: RaksysTheme, icons, state views,
│                                     #   toast, resizable splitters
│
└── feature/
    ├── connection/                   # Profile list, wizard, test banner, quick local DB, repository
    ├── navigator/                    # Schema navigator (tables & columns)
    ├── grid/                         # Sortable / filterable DataGrid + CSV/JSON export
    ├── query/                        # SQL console, history, pagination, structure inspector,
    │                                 #   production safeguard
    ├── erd/                          # Layout algorithm + Bézier canvas
    ├── permission/                   # Roles list + privilege matrix
    ├── document/                     # MongoDB collection list + JSON viewer + add document
    └── keyvalue/                     # Redis key browser
```

### Dependency Rules

```
app ──► feature:* ──► core:ui, core:model, core:database
                      core:database ──► core:security, core:model
feature:query ──► feature:grid          (the only feature→feature dependency)
```

- `core:model` depends on nothing but `kotlinx.serialization`.
- `core:ui` knows about models, not drivers.
- Features **never** depend on each other except `query → grid` (the result table).
- `app` is the only module that wires every feature together.

---

## 🔌 Driver Layer

| Interface | Implementation | Notes |
|---|---|---|
| `DatabaseDriver` | `JdbcDatabaseDriver` | One **HikariCP** pool per profile (max 5 connections, 10 s connect timeout). Builds JDBC URLs for PostgreSQL / MySQL / SQLite (with optional SSL). |
| `DocumentDatabaseDriver` | `MongoDatabaseDriver` | One `MongoClient` per profile (10 s connect / 30 s read timeout). |
| `KeyValueDriver` | `RedisKeyValueDriver` | One `JedisPool` per profile; cursor-based `SCAN`. |
| `PermissionDriver` | `JdbcPermissionDriver` | Reads `pg_roles` / `information_schema`; issues `GRANT` / `REVOKE` with quoted identifiers. |

Shared behaviors:

- **Guard rails** (SQL): `queryTimeout = 30 s`, `maxRows = 10,000` (+1 to detect truncation → `QueryResult.truncated`).
- **Cancellation**: `cancel()` aborts the active JDBC `Statement`; for Redis the pool is closed to break the blocked socket read.
- **Lifecycle**: `invalidate(profileId)` disposes a single pool (used when a profile is edited/deleted); `close()` disposes all pools when the window closes.
- **All I/O on `Dispatchers.IO`**, results returned to the UI via `StateFlow`.

### SSH Tunnels

`SshTunnelManager.resolve(profile)` returns `(host, port)`:

- SSH disabled → the profile's own host/port.
- SSH enabled → opens (once per profile) an SSHJ connection to the bastion, starts a local port-forward on a random free `localhost` port, and returns `("localhost", localPort)`. Every driver calls `resolve()` before connecting, so tunneling is transparent to all engines.
- Host keys are verified against `~/.ssh/known_hosts` (`OpenSSHKnownHosts`). Missing file or unknown host ⇒ connection refused with instructions.

---

## 🔐 Credential & Profile Storage

| Data | Location | Format |
|---|---|---|
| Profiles (name, engine, host, port, database, username, SSL/SSH flags, environment) | `~/.raksys/connections.json` | Pretty-printed JSON via `kotlinx.serialization` |
| Database password | OS keyring, service `com.raksys.dbtool`, key = `<profileId>` | Encrypted by the OS |
| SSH password | OS keyring, key = `<profileId>:ssh` | Encrypted by the OS |

`connections.json` **never contains passwords**. Unknown JSON keys are ignored (`ignoreUnknownKeys`) so newer files load on older builds.

---

## 🛡️ Production Safeguard

`isDestructiveSql(sql)` (feature/query) is a regex-based check for `DROP …`, `TRUNCATE`, `DELETE FROM x` without a trailing clause, `UPDATE … SET` without `WHERE`, and `ALTER TABLE … DROP`. `QueryEditor` invokes it only when `profile.environment == PRODUCTION` and shows `ProductionSafeguardDialog` before executing. It is a safety net, not a SQL parser — see *Known Limitations* in [`SECURITY.md`](SECURITY.md).

---

## 🗺️ ERD Layout

`computeErdLayout()` (pure function, unit-tested) places table boxes on a grid with fixed gaps; `computeErdEdges()` derives relationship edges from each column's `ForeignKeyRef`. `ErdCanvas` draws boxes and cubic Bézier edges, with a 0.4×–2.2× zoom.

---

## 🧪 Testing

Pure logic is covered by JUnit 5 + `kotlin-test`:

| Module | Tests |
|---|---|
| `core:database` | `JdbcUrlTest`, `MongoConnectionStringTest`, `RedisValueFormatTest`, `ConnectionErrorMapperTest` |
| `feature:connection` | `ConnectionFormValidatorTest`, `ConnectionRepositoryTest` |
| `feature:query` | `ProductionSafeguardTest`, `TableStructureInspectorTest` |
| `feature:erd` | `ErdLayoutTest` |

Run everything with `./gradlew test`. Tests do not require a running database.

---

## ➕ Adding a New Feature Module

1. Create `feature/<name>/build.gradle.kts` (copy a sibling, keep the JDK 17 toolchain).
2. Add `":feature:<name>"` to [`settings.gradle.kts`](settings.gradle.kts) and as an `implementation` dependency in `app/build.gradle.kts`.
3. Add `<Name>Event`, `<Name>Presenter` (exposing `StateFlow<UiState<T>>`), and the Composable screen.
4. Expose a Koin module (`val <name>Module = module { … }`) and register it in `startKoin { }` in `Main.kt`.
5. Depend on drivers **through their interfaces**; add a new driver interface in `core:database` if needed.
6. Add unit tests for any pure logic.

## ➕ Adding a New Database Engine

1. Add the value to `DbType` (with its `DbFamily`) in `core:model`.
2. Implement the matching driver interface in `core:database` (resolve host/port through `SshTunnelManager`, read secrets from `CredentialStore`).
3. Add a `friendlyConnectionError` case if the engine has distinctive errors.
4. Update the type picker, default port, and validator in `feature:connection`, plus the brand color/logo in `core:ui`.
5. Route the new family in `Main.kt` and add tests (URL builder, validator).
