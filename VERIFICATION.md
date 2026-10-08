# Verification — October 7, 2026 (America/New_York)

The default dependency is the public GitHub release `AssetLib/sdk-android@v0.1.0-preview.1`, verified against committed SHA-256 `adb0f4fdc5ceaa69c2092e3987ebc17afc0fb5f34bbf7576eea025147445eba2`.

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
