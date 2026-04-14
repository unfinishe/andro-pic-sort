# Local release and Play upload (No CI)

This project uses a local-first release workflow (see `arc42/09_architecture_decisions.md`, ADR-008).

## 1) One-time setup

1. Create an upload keystore (if you do not already have one).
2. Copy `keystore.properties.example` to `keystore.properties` in the project root.
3. Fill in real local values:

```properties
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

Important:
- Never commit `keystore.properties`.
- Never commit keystore files (`.jks` / `.keystore`).
- Keep backups of your upload key in a secure location.

## 2) Build signed release AAB locally

From project root:

```bash
./scripts/release-local.sh
```

The script will:
- run `:core:test` and `:app:testDebugUnitTest`
- run `:app:bundleRelease`
- copy the generated AAB to a timestamped folder under `release-artifacts/`
- generate `metadata.txt` with version and checksum
- generate `test-reports/v<version>-<timestamp>.md` with a Markdown test summary

## 3) Output artifacts

Example output folder:

```text
release-artifacts/20260414-210000/
  app-release.aab
  metadata.txt

test-reports/
  v1.0.5-20260414-210000.md   ← commit this to git
```

`metadata.txt` includes:
- release timestamp
- `versionCode`
- `versionName`
- git SHA (if available)
- artifact filename
- SHA-256 checksum

After the build, commit the test report:

```bash
git add test-reports/
git commit -m "chore: test report v1.0.5"
```

## 4) Upload flow in Play Console

1. Use **Internal testing** track first.
2. Upload `app-release.aab`.
3. Copy release text from `PLAY_STORE_LISTING.md` (Play-specific) and `RELEASE_NOTES.md` (canonical history).
4. Verify Data safety and policy declarations are still accurate.
5. Promote to production with staged rollout.

## 5) Troubleshooting

- If you see `Missing keystore.properties`, create it from `keystore.properties.example`.
- If bundle generation fails, verify JDK and Android SDK setup from `README.md`.
- If signing errors occur, verify keystore path, alias, and passwords in `keystore.properties`.

