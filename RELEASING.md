# Release Guide & Semantic Versioning 🚀

This document describes how to cut a release of **Raksys** using **Semantic Versioning (SemVer)**.

---

## 📌 1. Semantic Versioning Standards

All release tags follow the format:

```
vMAJOR.MINOR.PATCH
```

- **`MAJOR`**: breaking changes (e.g. incompatible profile-file format).
- **`MINOR`**: new backwards-compatible features (new engine, new tool).
- **`PATCH`**: bug fixes, small optimizations, documentation.

---

## 🔢 2. Where Versions Live

| Location | Purpose |
|---|---|
| `build.gradle.kts` → `allprojects { version = "…" }` | Gradle project version (currently `0.1.0`). |
| `app/build.gradle.kts` → `nativeDistributions { packageVersion = "…" }` | Version embedded in the `.dmg` / `.exe` installers (currently `1.0.0`). |
| `CHANGELOG.md` | Human-readable release notes. |

> ⚠️ **Installer versions have platform rules.** Compose's packaging (jpackage) requires the macOS `.dmg` version to start with a `MAJOR` of **1 or higher**, which is why `packageVersion` is `1.0.0` while the Gradle version is `0.1.0`. Decide on one scheme before the first public release and keep both values in sync so the asset filenames match the Git tag.

---

## 🚀 3. Release Steps

### Step 1: Prepare the Branch
```bash
git checkout main
git pull origin main
./gradlew clean test
```

### Step 2: Bump Versions & Update the Changelog
1. Update `version` and `packageVersion` (see table above).
2. Move the **Unreleased** notes in `CHANGELOG.md` under a new `## vX.Y.Z` heading.
3. Commit:
```bash
git commit -am "chore(release): vX.Y.Z"
```

### Step 3: Build the Installers

Installers can only be built **on the target OS** (jpackage does not cross-compile):

```bash
# On macOS  → .dmg
./gradlew :app:packageDmg

# On Windows → .exe
./gradlew :app:packageExe
```

Artifacts are written to `app/build/compose/binaries/main/<format>/`.

### Step 4: Smoke-Test the Installer
- [ ] Installs and launches on a clean machine.
- [ ] Create a connection, run **Test Connection**, then `SELECT 1`.
- [ ] Password is saved to (and read from) the OS keyring after restarting the app.
- [ ] `PROD`-tagged connection shows the safeguard for `DELETE FROM some_table;`.

### Step 5: Tag & Push
```bash
git tag -a vX.Y.Z -m "Raksys vX.Y.Z"
git push origin main --tags
```

### Step 6: Publish the GitHub Release
1. Open **Releases → Draft a new release** and choose the `vX.Y.Z` tag.
2. Paste the matching section of `CHANGELOG.md` as the description.
3. Attach the `.dmg` and `.exe` files.
4. Publish.

---

## 🧯 4. Hotfixes

1. Branch from the release tag: `git checkout -b fix/<name> vX.Y.Z`.
2. Fix, add a regression test, and bump the **PATCH** version.
3. Repeat steps 3–6 above.

---

## 🔮 5. Automating Releases (Future)

The roadmap includes a CI pipeline (see [`ROADMAP.md`](ROADMAP.md)): a build matrix of `macos-latest` and `windows-latest` running `./gradlew test` and the package tasks, then uploading artifacts to the GitHub Release on a `v*` tag push.
