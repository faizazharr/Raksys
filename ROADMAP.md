# Raksys Project Roadmap 🗺️

This document outlines planned milestones and ideas for **Raksys**. It is a living plan — priorities can change, and community feedback and pull requests are warmly encouraged!

---

## 📍 Current Release: v0.1.0

- [x] **Five engines**: PostgreSQL, MySQL / MariaDB, SQLite, MongoDB, Redis.
- [x] **Secure connections**: OS-keyring credentials, SSH tunnel with strict host-key verification, optional SSL/TLS.
- [x] **SQL console** with history, formatter, cancel, timeout, and row cap.
- [x] **Data grid** with sort, filter, cell inspector, and CSV / JSON export.
- [x] **Schema tooling**: navigator, table structure inspector, DDL / `INSERT` generators, Visual ERD.
- [x] **Roles & Permissions matrix** for PostgreSQL and MySQL.
- [x] **Production safeguard** for destructive SQL.
- [x] **MongoDB browser** with JSON viewer and document insert.
- [x] **Redis browser** with `SCAN`, type filters, and TTL.
- [x] **Command Palette** and collapsible / resizable layout.
- [x] **Native installers** for macOS (`.dmg`) and Windows (`.exe`).

---

## 🔮 Upcoming Milestones

### 📌 Milestone 1: Editing & Multi-Tab Workflow (v0.2.0)
- [ ] **Multiple query tabs** with per-tab results.
- [ ] **Saved queries / snippets** with persistence across sessions.
- [ ] **Persistent query history** (opt-in, local only) with per-connection filtering.
- [ ] **Inline row editing** in the data grid (with a generated, reviewable `UPDATE` preview).
- [ ] **Export to file** (CSV / JSON / SQL) instead of clipboard only.

### 📌 Milestone 2: Deeper NoSQL Support (v0.3.0)
- [ ] **MongoDB**: edit and delete documents, query by JSON filter, index viewer.
- [ ] **Redis**: edit values, set / remove TTL, delete keys with confirmation, TLS support.
- [ ] Cursor-based paging for large collections and key spaces.

### 📌 Milestone 3: Schema Intelligence (v0.4.0)
- [ ] **SQL autocomplete** driven by the live schema (tables, columns, keywords).
- [ ] **Index & constraint viewer** in the structure inspector.
- [ ] **Multi-schema support** (PostgreSQL schemas beyond `public`).
- [ ] **ERD improvements**: draggable boxes, export as PNG / SVG, focus on a table's neighbors.
- [ ] **Explain plan visualizer** (`EXPLAIN` / `EXPLAIN ANALYZE`).

### 📌 Milestone 4: Safety & Team Features (v0.5.0)
- [ ] **Configurable safeguard rules** (per-environment; e.g. also guard STAGING).
- [ ] **Read-only connection mode**.
- [ ] **Import / export connection profiles** (secrets excluded).
- [ ] **Light theme** and theme switcher.
- [ ] **Localization** (Indonesian / English UI strings).

### 📌 Milestone 5: Distribution (v1.0.0)
- [ ] **Signed & notarized** macOS builds.
- [ ] **Linux packages** (`.deb` / AppImage).
- [ ] **CI/CD** pipeline for automated test + release builds.
- [ ] **Auto-update** check (opt-in, privacy-preserving).

---

## 💡 Ideas Under Consideration

- Additional engines (SQL Server, MariaDB-specific features, ClickHouse).
- Data compare / schema diff between two connections.
- Query result charts.
- Plugin API for custom drivers.

Have an idea? [Open a feature request](../../issues/new?template=feature_request.md).
