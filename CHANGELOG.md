# Changelog

All notable changes to **Raksys** are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/) and the project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

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
