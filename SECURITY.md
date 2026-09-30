# Security & Privacy Policy 🔒

## 🛡️ Privacy & Zero-Data Collection Guarantee

Raksys **does not collect, harvest, or store any private information or telemetry** from users or contributors.

- **Local-Only Processing**: queries, results, schema metadata, and history are kept in memory on your machine.
- **No Remote Servers**: Raksys contains no analytics or tracking SDKs and never phones home.
- **Only Your Connections**: the sole network traffic is to the database you configured — directly, or through the SSH bastion you configured.

---

## 🔐 Security Model

| Concern | How Raksys handles it |
|---|---|
| **Database passwords** | Stored in the OS keyring (Apple Keychain, Windows Credential Manager, or Secret Service) under service `com.raksys.dbtool`. |
| **SSH passwords** | Stored in the OS keyring under `<profileId>:ssh`. |
| **SSH private keys** | Only the *file path* is saved; the key file is read from disk at connect time and never copied. |
| **Profile file** | `~/.raksys/connections.json` holds non-secret settings only (name, engine, host, port, database, username, flags, environment). |
| **SSH host trust** | Verified against `~/.ssh/known_hosts`. Unknown hosts / missing file ⇒ connection refused (no silent trust-on-first-use). |
| **Transport encryption** | Optional SSL/TLS toggle for PostgreSQL (`sslmode=require`), MySQL (`useSSL=true&requireSSL=true`), and MongoDB (`ssl=true`). |
| **Privilege changes** | `GRANT` / `REVOKE` use quoted identifiers and escaped literals; revoking requires confirmation. |
| **Production safeguard** | Destructive SQL on `PRODUCTION`-tagged connections requires explicit confirmation. |
| **Resource limits** | 30 s query timeout, 10,000-row result cap, bounded connection pools. |

---

## ⚠️ Known Limitations

Please be aware of these when deciding how to use Raksys:

- **The production safeguard is a safety net, not a SQL parser.** It is regex-based and only inspects statements typed into the SQL console on `PRODUCTION`-tagged connections. It can miss obfuscated or multi-statement edge cases, and it does not replace least-privilege database accounts.
- **`WHERE` detection is coarse**: an `UPDATE` is considered safe if the word `WHERE` appears anywhere in the text.
- **The SQL console executes what you type.** Use a read-only database role for exploratory work on sensitive systems.
- **Redis SSL is not configurable** in the current version; use an SSH tunnel for untrusted networks.
- **Linux** requires a running Secret Service provider (e.g. GNOME Keyring, KWallet) to save credentials.
- **Query history is in-memory** and may contain sensitive literals; it is cleared when the app closes or when you press *Bersihkan*.
- Privilege listing for PostgreSQL is scoped to the `public` schema.

**Recommended practice**: connect with the least-privileged account that does the job, tag production connections as `PROD`, and prefer SSH tunnels over exposing database ports.

---

## 🚨 Reporting a Vulnerability

If you discover a security vulnerability, please **do not open a public issue**. Report it privately via a [GitHub Security Advisory](../../security/advisories/new) or by contacting the maintainer at `faizazharr@gmail.com`.

Please include:

1. A description of the issue and its impact.
2. Steps to reproduce (engine, version, OS).
3. Any suggested fix, if you have one.

You can expect an acknowledgement within a few days. Please allow reasonable time for a fix before public disclosure.

## ✅ Supported Versions

Only the **latest release** receives security fixes.
