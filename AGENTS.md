# AGENTS.md

Roam for Android is a small, fictional Jetpack Compose travel app. It is a public, MIT-licensed example of a customer's Android app using the released Assetlib Android SDK ([AssetLib/sdk-android](https://github.com/AssetLib/sdk-android), package `com.assetlib.sdk`) with three placements and bundled fallbacks. The SDK comes in as a hash-checked AAR from a GitHub release; signed-manifest, cache and decoder tests live in the SDK repo. Users create workspaces in the hosted console (https://console.assetlib.dev).

## Commands

Needs JDK 17, Android SDK platform 36 with build-tools 36.1.0, and Node 22+ (every build runs `scripts/fetch-sdk.mjs`). Set `ANDROID_HOME` or put `sdk.dir` in `local.properties` (ignored).

```sh
./gradlew :app:assembleDebug :app:lint
./gradlew :app:installDebug
./gradlew :app:testDebugUnitTest :app:lint :app:assembleDebug :app:assembleRelease   # what CI runs
./gradlew :app:connectedDebugAndroidTest   # needs an API 26+ emulator or device

# Regenerate, or check, typed references after editing catalog.json (offline)
node scripts/generate-assets.mjs catalog.json app/src/main/java/com/assetlib/demo/AppAssets.kt com.assetlib.demo
node scripts/generate-assets.mjs catalog.json app/src/main/java/com/assetlib/demo/AppAssets.kt com.assetlib.demo --check

# Optional read-only hosted test (public config file only; skips when unset)
ASSETLIB_PUBLIC_CONFIG_FILE=/absolute/path/public-config.json ./gradlew :app:connectedDebugAndroidTest

# SDK development only: build against a local SDK checkout instead of the release AAR
./gradlew -PassetlibSdkDir=../sdk-android :app:assembleDebug
```

CI (`.github/workflows/check.yml`, ubuntu, Java 17) runs the `--check` command, then `:app:testDebugUnitTest :app:lint :app:assembleDebug :app:assembleRelease`, and uploads the APKs. Lint aborts on error. CI does not run instrumentation tests, so run `:app:connectedDebugAndroidTest` yourself when you touch `ArtworkImage`, descriptions or `TravelModel`. Work is done when the CI commands pass locally.

## Layout

- `catalog.json`: placements `travel.coast` and `travel.ridge` (1200 × 900) and `tasks.garden` (600 × 400), with `bundledAccessibility` descriptions of the bundled images.
- `app/src/main/java/com/assetlib/demo/AppAssets.kt`: generated `AssetRef`s. Never edit by hand.
- `TravelModel.kt`: connection, saved config, refresh, measured pixel targets (`setTarget`) and disconnect. A `generation` token stops an old connection from overwriting a newer one.
- `MainActivity.kt`: Compose UI, `ArtworkImage` (description follows the pixels actually shown), source labels, connection sheet.
- `AssetArtworkPainter.kt`: a `BitmapPainter` over the verified bitmap, or the bundled resource when there is none.
- `app/src/main/res/drawable-nodpi/`: bundled fallbacks `coast_hero.webp`, `ridge_card.png`, `task_garden.png`.
- `app/src/test/.../CatalogTest.kt`, `app/src/androidTest/.../ArtworkAccessibilityTest.kt`, `HostedArtworkTest.kt`; `app/src/debug/` holds the test-only host activity and a debug-only network security config whose `debug-overrides` trust user-installed CAs, so a debug build can reach a local HTTPS front (the workspace's `e2e/` harness installs its test CA on the emulator). Release builds have no network security config: system CAs only, no cleartext.
- `sdk-release.json` + `scripts/fetch-sdk.mjs`: the SDK lock and its verifier.

## Invariants

- **Placements come from the catalog.** Edit `catalog.json`, regenerate `AppAssets.kt`, and commit both. Use `AppAssets.<Group>.<name>`, not key strings, and do not build a flow where placements are typed into the console first. The console's seeded demo workspace has these same three placements, so keys and sizes must keep matching. `CatalogTest` and `ArtworkAccessibilityTest` assert the keys, sizes and the coast's bundled description; update them only for an intended change.
- **Bundled fallbacks always render.** Every placement keeps a drawable fallback, and the painter uses it whenever the verified bitmap is absent (no connection, failed download, verification or decode). With bundled pixels the image uses only the bundled description; an undescribed remote image is decorative, never labeled with a stale description. The garden is always decorative.
- **Public config only.** `PublicConfig.parse` validates the pasted JSON. It is saved as `assetlib-public-config.json` in `noBackupFilesDir`; the manifest keeps `allowBackup="false"` and `usesCleartextTraffic="false"`. The hosted-test config is copied only into the test APK's build output, never into app assets or source control. Disconnect deletes the config file only; verified cache and replay protection remain by design. Configurations saved before the console moved name the legacy host `assetlib-console.vercel.app`; the app must keep accepting them (`CatalogTest.savedConfigurationsOnEitherConsoleHostStillParse`), while links people click go to https://console.assetlib.dev.
- **SDK pin.** The default build uses the exact GitHub-release AAR named in `sdk-release.json` (currently `v0.4.0-preview.1`) and verifies its SHA-256 on every build; take the hash from the release's `SHA256SUMS`. Never commit the AAR (`app/libs/*.aar` is ignored), weaken the hash check, or make a sibling checkout the default. A version change updates together: `sdk-release.json` (version, URL, sha256), `scripts/fetch-sdk.mjs` (hard-codes the version, URL pattern and AAR file name), `app/build.gradle.kts` (AAR file name, `versionName`, `versionCode`), and `README.md`. The SDK's runtime dependencies are pinned in `app/build.gradle.kts` because an AAR does not carry its dependency graph; keep them matching the SDK release.
- **The app keeps its own identity.** Roam uses its own color scheme, serif display type and the `roam.` wordmark. Assetlib appears only as quiet text: the "ARTWORK BY ASSETLIB" section label, the "Open Assetlib console" link and the launcher label suffix. Do not add Assetlib logos, colors or marketing copy.
- **Copy.** In prose and UI text the product is "Assetlib"; `AssetLib` is only the GitHub org in URLs. Keep claims plain and specific; label unbuilt features as planned; no invented customers or metrics. Verification notes carry absolute dates and must stay true.
- **Fix what you find.** Fix a confirmed defect in the same change and record how you verified it. If a fix is unsafe right now, say so in the PR and say when it will be done.

## Release and versioning

There are no tags or GitHub releases. `versionName` matches the SDK version and `versionCode` has gone up by one with each SDK bump (both in `app/build.gradle.kts`); `applicationId` is `com.assetlib.demo.travel`. Release builds use R8 and resource shrinking and produce an unsigned APK. Dated verification notes live in `README.md` ("Checks and distribution") for 0.2.x and in `VERIFICATION.md` for the first release and from 0.3.0-preview.1 on (`versionCode` 4).

## Don'ts

- Don't hand-edit `AppAssets.kt`.
- Don't commit keystores, signing credentials, `local.properties`, the SDK AAR, or a public config file.
- Don't add Maven repositories to the app module (`settings.gradle.kts` sets `FAIL_ON_PROJECT_REPOS`) or pull the SDK from anywhere but its release.
- Don't add analytics, booking or purchase flows; the app is a fictional demo.
- Don't publish, tag, or change repository settings from an agent session.
