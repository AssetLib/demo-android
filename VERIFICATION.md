# Verification — October 7, 2026 (America/New_York)

The current default dependency is the public GitHub release `AssetLib/sdk-android@v0.3.0-preview.1`, verified against committed SHA-256 `a80744898801350032b71aa9241a7aba8344f333ef3360f22310d08c59ab2d20`; its checks are in the October 9 section at the end. The record below covers the first release, whose default dependency was `AssetLib/sdk-android@v0.1.0-preview.1`, verified against SHA-256 `adb0f4fdc5ceaa69c2092e3987ebc17afc0fb5f34bbf7576eea025147445eba2`. The 0.2.x checks are dated in the README under "Checks and distribution".

Observed locally:

- Deleted the cached AAR, downloaded the actual public release with `scripts/fetch-sdk.mjs`, and verified its exact digest.
- An isolated source-only copy (no sibling SDK, local properties, AAR, build outputs, or project caches) fetched the public SDK and passed all unit/lint/debug/release build tasks with the build cache disabled.
- Default build without `assetlibSdkDir`: `:app:testDebugUnitTest` (**2 passed**), `:app:lint`, `:app:assembleDebug`, and R8-enabled `:app:assembleRelease` passed.
- Artwork updates are applied progressively as coast, ridge, and garden resolve, with a generation guard to prevent an old connection from replacing a new one. Busy state remains until all three finish.
- Checked-in generated Kotlin references pass the offline `--check` command for all three seeded contracts.
- Debug APK installed successfully on the existing Android 36.1 arm64 emulator, package `com.assetlib.demo.travel`.
- Optional `HostedArtworkTest`: **1 passed, zero skipped**, using only the real workspace's public configuration from ignored test assets. The test accepted its signed release, downloaded or retrieved all three placements, decoded all three as native Android bitmaps, then reloaded persisted state/cache in an independent client with networking explicitly disabled. This test did not upload, bind, publish, or roll back server content.
- Inspected the normal demo APK ZIP entries: no hosted configuration asset. Public test configuration is confined to generated test-APK assets and is not checked in.

The whole unsigned, shrunk demo APK is approximately 1.5 MiB on this build; this is not an incremental SDK size benchmark. Debug packages are larger and dependencies are separate from the small SDK AAR.

Manual UI/gesture verification remains unconfirmed because the Android Studio emulator view stalled, despite healthy adb and successful instrumentation. Physical-device coverage, Android 26 execution, store distribution, and a native end-to-end console publish/rollback UI sequence are not established by these checks. The SDK's synthetic tests do cover publish/update/rollback sequence semantics. Remote CI is separate evidence.

## SDK 0.3.0-preview.1 — October 9, 2026 (America/New_York)

`sdk-release.json`, `scripts/fetch-sdk.mjs` and `app/build.gradle.kts` now name `assetlib-android-0.3.0-preview.1.aar` from the public `v0.3.0-preview.1` release, with the SHA-256 published in that release's `SHA256SUMS`. `versionName` is `0.3.0-preview.1` and `versionCode` is 4. The SDK's runtime dependencies are unchanged from 0.2.1, so the pinned coroutines, serialization, Bouncy Castle and OkHttp versions stay as they were.

The release only adds API with defaults: a `staging` environment, appearance and arm variant cells, an optional `decide` callback, a pinned key set, and appearance and arm parameters on `resolve`. The demo uses none of them and needed no code change for the upgrade. The storage namespace now derives from the manifest origin, organization, app, and environment, and the SDK migrates the previous state once after verifying it.

The connection sheet's **Open Assetlib console** button and the README now point to `https://console.assetlib.dev`, and the README's product site link to `https://www.assetlib.dev`. The demo does not check the configuration's host; `PublicConfig.parse` accepts an HTTPS manifest URL scoped to the app on any host. A new JVM test, `savedConfigurationsOnEitherConsoleHostStillParse`, parses a saved configuration on `assetlib-console.vercel.app` and on `console.assetlib.dev`, with the legacy and the environment manifest paths, and round-trips each through `toJson()`.

Observed locally with JDK 17 (Zulu 17.54.21), Node 24, Android SDK 36 and build-tools 36.1.0:

- `node scripts/generate-assets.mjs ... --check` passed.
- `./gradlew :app:testDebugUnitTest :app:lint :app:assembleDebug :app:assembleRelease` passed, the CI command. `fetchSdk` downloaded the release AAR and printed "Verified Assetlib Android 0.3.0-preview.1."; its SHA-256 matches `SHA256SUMS`. `CatalogTest`: 3 passed. Lint reported warnings only (dependency and plugin version notices, data extraction rules, launcher icon, a KTX suggestion).
- `./gradlew :app:connectedDebugAndroidTest` on the API 36.1 emulator (Android 16): `ArtworkAccessibilityTest` passed; `HostedArtworkTest` skipped with no configuration supplied.
- The same task with `ASSETLIB_PUBLIC_CONFIG_FILE` set to the demo workspace's public configuration, whose manifest URL is on the legacy host `assetlib-console.vercel.app`: 2 passed, 0 skipped. All three placements resolved signed release 6 as WebP (1200 × 900, 1200 × 900, 600 × 400) and decoded natively, and an independent client with networking disabled restored release 6 from cache. The test is read-only and published nothing; the configuration was copied only into the test APK's ignored build output.
- The debug APK was installed and launched. The connection sheet rendered, and **Open Assetlib console** started a `VIEW` intent for `https://console.assetlib.dev`.

Not verified: manual TalkBack, a physical device, Android 8 (API 26), and a console publish and rollback while the app is open.
