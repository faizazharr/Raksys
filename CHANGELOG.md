# Changelog

All notable changes to **Raksys** are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/) and the project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Features
- **Light theme and Settings** (*File › Settings…*, `⌘,` / `Ctrl+,`): appearance *Ikuti sistem* (default) / *Terang* / *Gelap*, applied instantly and saved to `~/.raksys/settings.json`. The SQL editor, command palette, badges, and Redis type colors all follow. Both palettes are contrast-checked by a unit test.
- **Privacy switch for query history** in Settings, plus *Hapus semua riwayat tersimpan*.
- **Multiple SQL console tabs** with per-tab text, a `+` button, close buttons, and *File › New Query Tab*.
- **Query history is saved per connection** under `~/.raksys/history/` (last 100, owner-only permissions where supported); *Bersihkan* deletes it from disk.
- **Save results to a file**: *Simpan CSV…* / *Simpan JSON…* with a native save dialog. The JSON export now escapes quotes, backslashes, and line breaks correctly and keeps numbers, booleans, and `null` typed (it used to quote every value and break on newlines).

### Fixes
- Dialogs that open over the SQL editor (settings, connection form, production safeguard, command palette, and others) are no longer partly covered by it; the editor steps aside while a dialog is open.
- The first console tab now shows its sample query instead of an empty editor.
- PostgreSQL badge text in the dark theme raised from 4.3:1 to 5.1:1.
- Query results from one connection no longer stay on screen after switching to another.
- SQLite and other passwordless connections work on machines without an OS keyring; only *saving* a password fails, with a clear message.
- Search fields no longer clip their text (new `RaksysSearchField`); the ERD search box no longer covers the first tables; default window is 1280×800.

### Performance
- **Fixed a regression** from the schema cache change: SQLite databases with many tables (about 40 or more) failed to load in the navigator and ERD ("too many terms in compound SELECT"). SQLite now reads columns per table; other engines still try the single bulk read and fall back per table if it is rejected. Covered by new tests (120 tables, key attachment, `_` in table names).
- **ERD**: hovering no longer recomposes every table card (hover state is read per card through derived states), cards dim with a drawn wash instead of `alpha` layers, and each card uses a plain column list instead of its own `LazyColumn` (lazy only above 40 columns).
- **Data grid**: rows receive their selection as plain values, so clicking a cell recomposes 2 rows instead of every visible row; cell text is capped at 200 characters for layout (the inspector still shows the full value), which keeps scrolling smooth on wide JSON / text columns.
- **Memory**: the heap now shrinks back after large results (`-Xms32m`, `MinHeapFreeRatio=10`, `MaxHeapFreeRatio=30`): resident memory after scrolling a 10,000-row grid dropped from about 510 MB to 390 MB in a measured run.
- Schema is cached per connection and read with one column query instead of one per table; idle pool connections are released after 60 s; Redis scans are pipelined and collection previews capped at 100 elements; the grid row filter is debounced.

### UI / Accessibility
- **Contrast fixes** (WCAG AA): muted text 4.0:1 → 5.7:1, accent text 4.9:1 → 6.8:1, white-on-accent buttons 3.3:1 → 5.2:1, PROD badge 2.7:1 → 5.3:1. Filled buttons now use the new `PrimaryFill` token; destructive confirm uses `EnvProdFill`.
- **Minimum text size**: no text below 10 sp (previously 9 sp); captions and badges raised to 11 sp.
- **Native menu bar** (File / View / Go) with platform-correct shortcuts (`⌘` on macOS, `Ctrl` elsewhere); shortcut hints in the status bar, toolbar, and command palette follow the platform.
- **Hover and keyboard focus** feedback on tabs, connection cards, table rows, collections, and Redis keys.
- **Vector icons** for the Query / ERD / Permissions tabs and the sidebar toggle, replacing emoji.
- **Screen-reader labels** on every icon-only button (refresh, copy, edit, delete, show/hide password, close, zoom, sidebar toggle, row menu). Icon-only buttons are now 28 dp (desktop default) instead of 20–24 dp.
- **Emoji removed from the UI.** Decorative emoji in labels are gone (the text already says what the button does); icon-only uses are replaced by the new vector set in `ActionIcons.kt` (search, refresh, eye, copy, edit, trash, key, link, lock, user, plus, document, table). Empty states, the command palette, and the ERD key/foreign-key markers use the same icons.
- **Query menu > Run SQL** and vector icons (Run, Format, History) in the SQL toolbar; the Run button is now `SuccessFill` (5.5:1 with white text, was 3.8:1).
- **macOS type scale** for Material text styles (Body 13, Callout 12, Caption 11, Title 15 / 17); nothing below 11 sp.
- **Environment edge**: a 2 dp window-wide line in the active connection's environment color.

### Documentation
- Added full project documentation: `README.md`, `ARCHITECTURE.md`, `CONTRIBUTING.md`, `SECURITY.md`, `ROADMAP.md`, `RELEASING.md`, `DOWNLOAD.md`, `CODE_OF_CONDUCT.md`, donation info, a flow diagram, and issue / PR templates.

## v0.1.0 — Initial Development Build

First public build of the Raksys multi-engine database studio.

### Features

- **Multi-Engine Connections**:
  - PostgreSQL, MySQL / MariaDB, SQLite, MongoDB, and Redis in a single window.
  - Two-step connection wizard with engine picker, inline **Test Connection**, and field validation (host, port 1–65535, database name, SSH fields).
  - Create-database-on-connect for PostgreSQL / MySQL, and a **Quick Local DB** dialog (PostgreSQL / MySQL / SQLite).
  - Connection list with search, edit, duplicate, and confirm-before-delete.
  - Environment tags (`DEV` / `STG` / `PROD`) shown across the sidebar, toolbar, and status bar.
  - Human-friendly connection error messages (refused, DNS, timeout, auth, SSL, database missing / already exists).

- **Security**:
  - Database and SSH passwords stored in the OS keyring; profiles persisted to `~/.raksys/connections.json` without secrets.
  - SSH tunnel (bastion) support with password or private-key auth and strict `known_hosts` host-key verification.
  - Production safeguard dialog for destructive SQL on `PRODUCTION` connections.

- **SQL Workflow**:
  - SQL console with syntax highlighting, one-click formatter, run (`⌘↵` / `Ctrl+↵`) and cancel.
  - 30-second query timeout and 10,000-row result cap with truncation notice.
  - Query history (last 100 statements) with reuse / copy / clear.
  - Schema navigator with filter and context actions (open data, copy name, copy `SELECT`, copy DDL).
  - Paginated table browser (100 rows per page).
  - Data grid with column sort, row filter, cell inspector, and CSV / JSON clipboard export.
  - Table structure inspector with PK / FK badges, nullability, defaults, and DDL / `INSERT` template generation.

- **Visual ERD**: auto-layout diagram with Bézier foreign-key edges and 40%–220% zoom.

- **Roles & Permissions** (PostgreSQL / MySQL): role list with `superuser` / `no login` badges, per-table `SELECT` / `INSERT` / `UPDATE` / `DELETE` matrix, table search, bulk grant / revoke, and confirm-before-revoke.

- **MongoDB**: collection list with document counts, JSON document viewer (pretty / compact, filter, copy), and add-document dialog with JSON validation and formatter.

- **Redis**: cursor-based `SCAN` key browser with pattern search, type filter chips, TTL badges, and type-aware value rendering (`string`, `hash`, `list`, `set`, `zset`).

- **Studio Shell**:
  - Command Palette (`⌘K` / `Ctrl+K`) for tables, connections, and navigation.
  - Collapsible (`⌘B` / `Ctrl+B`) and resizable panels, dark theme, toast notifications, and a status bar with active-connection context.
  - Native packaging for macOS (`.dmg`) and Windows (`.exe`).

### Testing
- Unit tests for JDBC URL building, Mongo connection strings, Redis value formatting, connection error mapping, form validation, connection repository, ERD layout, production safeguard, and table structure inspector.
