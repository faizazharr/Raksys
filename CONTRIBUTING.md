# Contributing to Raksys

Thank you for your interest in contributing to **Raksys**! 🎉

Raksys is a desktop database studio that people point at real databases — sometimes production ones. To keep it trustworthy, all contributors must follow the guidelines below.

---

## 🛡️ 1. Safety & Data-Protection Rules (Crucial)

1. **Never Store Secrets in Plain Text**:
   - ❌ Do NOT write passwords, SSH passwords, or tokens to `connections.json`, logs, toasts, or exception messages.
   - ✅ Always go through `CredentialStore` (OS keyring). Profile fields in `ConnectionProfile` must stay non-secret.
2. **Protect Production**:
   - ❌ Do NOT bypass or weaken `ProductionSafeguardDialog` / `isDestructiveSql`. New destructive patterns should be *added* (with tests), never removed.
   - ✅ Any new feature that mutates data must consider what happens on a `PRODUCTION`-tagged profile.
3. **Strict Host-Key Verification**:
   - ❌ Never replace `OpenSSHKnownHosts` with a "trust all" verifier.
4. **Safe Identifier & Value Handling**:
   - ❌ Never concatenate raw user input into SQL that Raksys generates itself (e.g. `GRANT`/`REVOKE`).
   - ✅ Quote identifiers with `quotedIdentifier(...)` and escape string literals.

---

## 🔒 2. Zero-Telemetry Rule

- ❌ No analytics SDKs, crash reporters, update pings, or any network call the user did not configure.
- ✅ The only outbound connections are the user's database, and optionally the user's SSH bastion.

---

## ⚡ 3. Performance & Responsiveness Rules

1. **Keep the UI Thread Free**: all JDBC / Mongo / Redis / SSH work runs in drivers on `Dispatchers.IO`; UI observes `StateFlow`.
2. **Bound Everything**: keep result caps (`MAX_ROWS = 10_000`), timeouts (30 s query, 10 s connect), and pagination (100 rows/page) — do not add unbounded reads.
3. **Never Use `KEYS *`** on Redis; use cursor-based `SCAN`.
4. **Release Resources**: pools and tunnels must be closable via `invalidate(profileId)` / `close()`.

---

## 🏗️ 4. Architecture Rules

Read [`ARCHITECTURE.md`](ARCHITECTURE.md) first. In short:

1. **Presenter / Event / UiState**: screens dispatch `XxxEvent`s to `XxxPresenter`, which exposes `StateFlow<UiState<T>>`.
2. **Interfaces, not implementations**: features depend on `DatabaseDriver`, `DocumentDatabaseDriver`, `KeyValueDriver`, `PermissionDriver`, and `CredentialStore`, injected through Koin.
3. **Feature isolation**: feature modules must not depend on each other (`query → grid` is the sole exception).
4. **Shared UI in `core:ui`**: reuse `RaksysThemeColors`, `RaksysEmptyState`, `RaksysLoadingState`, `RaksysErrorState`, `RaksysStatusBadge`, and `ToastManager` — do not hardcode new colors in features.
5. **Pure logic stays testable**: put validators, URL builders, formatters, and layout math in plain functions with unit tests.

---

## 🧑‍💻 5. Development Setup

**Prerequisites**: JDK 17+, Git. (The Gradle wrapper fetches Gradle 8.11.1.)

```bash
git clone https://github.com/faizazharr/Raksys.git
cd Raksys

./gradlew :app:run     # launch the app
./gradlew test         # run all unit tests
```

Handy local databases for manual testing:

```bash
docker run -d --name raksys-pg    -e POSTGRES_PASSWORD=secret -p 5432:5432 postgres:16
docker run -d --name raksys-mysql -e MYSQL_ROOT_PASSWORD=secret -p 3306:3306 mysql:8
docker run -d --name raksys-mongo -p 27017:27017 mongo:7
docker run -d --name raksys-redis -p 6379:6379 redis:7
```

SQLite needs no server — point the wizard at any `.db` file.

---

## 🧭 6. Code Style

- Follow **official Kotlin style** (`kotlin.code.style=official` in `gradle.properties`).
- Prefer immutable `data class`es and `sealed interface`s for events/state.
- Return `Result<T>` from drivers; convert failures to friendly messages (see `friendlyConnectionError`).
- Keep Composables small; hoist state; avoid business logic inside `@Composable` bodies.
- Match the surrounding code's naming, comment density, and idiom. User-facing strings in the UI are currently **Indonesian** — keep new UI text consistent.

---

## 🔀 7. Workflow

1. **Fork** the repository and create a branch: `feat/<short-name>` or `fix/<short-name>`.
2. Make focused commits using [Conventional Commits](https://www.conventionalcommits.org/) — this repository already uses them:
   - `feat(permission): add PermissionScreen and PrivilegeMatrix`
   - `fix(connection): reject port outside 1-65535`
   - `docs: update README`
3. Run `./gradlew test` and verify the feature manually with `./gradlew :app:run`.
4. Open a pull request and fill in the [PR template](.github/pull_request_template.md). Include screenshots for UI changes.

---

## ✅ 8. Pull Request Checklist

- [ ] Builds and all tests pass (`./gradlew test`).
- [ ] New pure logic has unit tests.
- [ ] No secrets are written to disk or logs.
- [ ] No new telemetry / unsolicited network calls.
- [ ] Data-mutating features respect the Production Safeguard.
- [ ] Long-running work is off the UI thread, cancellable, and bounded.
- [ ] Docs updated (`README.md`, `CHANGELOG.md`) when behavior changes.

---

## 🐛 Reporting Bugs & Requesting Features

Use the issue templates: **Bug report** or **Feature request**. For security problems, follow [`SECURITY.md`](SECURITY.md) instead of opening a public issue.

By contributing you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
