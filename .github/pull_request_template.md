## 📋 Pull Request Summary

<!-- Briefly explain the problem, motivation, or new feature. -->

---

## 🛠️ Changes Made

- 
- 
- 

---

## 🛡️ Safety & Quality Checklist

Please verify each item before requesting review:

- [ ] **No Secrets on Disk**: passwords / SSH secrets only go through `CredentialStore` (OS keyring) — never to files, logs, or toasts.
- [ ] **Production Safety**: data-mutating features respect the `PRODUCTION` safeguard; `isDestructiveSql` was not weakened.
- [ ] **Zero Telemetry**: no analytics, tracking, or unsolicited network calls.
- [ ] **Responsive & Bounded**: I/O runs off the UI thread, is cancellable, and respects timeouts / row caps.
- [ ] **Architecture**: follows Presenter / Event / UiState; depends on driver *interfaces*; no new feature→feature dependency.
- [ ] **Tests**: new pure logic has unit tests and `./gradlew test` passes.
- [ ] **Tested Manually**: ran `./gradlew :app:run` against the affected engine(s): <!-- e.g. PostgreSQL 16, SQLite -->
- [ ] **Docs**: updated `README.md` / `CHANGELOG.md` if behavior changed.

---

## 📸 Screenshots / Demos (If Applicable)

<!-- Attach a screenshot or screen recording for UI changes. Use dummy data only. -->
