# Roam — native Android Assetlib demo

A small Jetpack Compose travel app using the native [Assetlib Android SDK](https://github.com/AssetLib/sdk-android). Browse coastal and alpine weekends with bundled original artwork, save a destination, then connect your own workspace to change artwork without rebuilding a compatible app.

[Assetlib console](https://assetlib-console.vercel.app) · [Product site](https://assetlib-sable.vercel.app) · [Public web travel demo](https://assetlib-travel.vercel.app)

Android API 26+; the current source targets developer preview **0.2.0-preview.1**. No account is required to explore the bundled app. Hosted administration uses GitHub sign-in. No app-store listing or production support guarantee is claimed.

## Build from this checkout

Install JDK 17, Android SDK 36 / build-tools 36.1.0, and Node 22+. Set `ANDROID_HOME` or add your SDK path to a private `local.properties` file.

```sh
./gradlew :app:assembleDebug :app:lint
./gradlew :app:installDebug
```

The default build runs `scripts/fetch-sdk.mjs`. It downloads the exact `v0.2.0-preview.1` GitHub-release AAR from `AssetLib/sdk-android`, validates the committed SHA-256 in `sdk-release.json` before use, and verifies an existing download on every build. There is no sibling checkout requirement or Maven Central dependency for Assetlib. Runtime dependencies are pinned in `app/build.gradle.kts` because a standalone AAR does not package its dependency graph.

For SDK development only:

```sh
./gradlew -PassetlibSdkDir=../sdk-android :app:assembleDebug
```

The local override intentionally bypasses the release AAR. It is not the default install path. Use it only while developing a matching SDK source version; public builds consume the locked release.

## Try an artwork release

1. Open the app and explore its bundled pictures. **Save** keeps a destination in the **Saved** tab for the current app session/activity state.
2. Tap **Connect**, then **Open Assetlib console**. Sign in with GitHub and create a seeded demo workspace.
3. Copy the workspace's public SDK JSON configuration. Paste it into **Public SDK configuration**, then tap **Connect and check release**. This contains an app ID, organization ID, delivery endpoint and verification key; never paste an admin credential.
4. Return to the app. Each picture reports **Bundled**, **Downloaded & verified**, or **Verified cache**, with the image's release sequence, actual delivered PNG/WebP format, and pixel dimensions when available.
5. In the console, upload an image of the same aspect ratio, bind it to `travel.coast`, and publish. In the running app, tap **Check for updates**. The illustration changes while the native layout stays the same.
6. In the console, roll back to the earlier release. A rollback creates a higher sequence. Tap **Check for updates** again to restore the earlier artwork.
7. Once artwork has downloaded, restart the app without network connectivity. Previously verified artwork can load from this device's cache. Cache eviction or unavailable/incompatible artwork uses the bundled images.

**Disconnect** removes the saved public configuration and returns to the bundled artwork. It retains cached images and monotonic release protection; Android app-data clearing removes those. The delivery server sees normal network request information. Published artwork is public.

## Placement contracts

| Typed reference | Remote key | Logical dimensions |
| --- | --- | --- |
| `AppAssets.Travel.coast` | `travel.coast` | 1200 × 900 |
| `AppAssets.Travel.ridge` | `travel.ridge` | 1200 × 900 |
| `AppAssets.Tasks.garden` | `tasks.garden` | 600 × 400 |

The third placement appears in the weekend-ritual card. Image views remain normal Compose `Image` calls with a demo-local `BitmapPainter` adapter over the SDK's verified Android `Bitmap`. This adds no Compose dependency to the SDK and no native SVG renderer. The SDK validates SVG candidate metadata but selects prepared PNG/WebP rasters.

Logical dimensions define compatibility, not exact delivered pixel size. The demo forwards each image's Compose `onSizeChanged` result as `AssetPixelSize`: these are actual display pixels, including density, not dp. The SDK chooses a sufficiently large rendition or the largest available alternative, then legacy WebP. A size change resolves again against the accepted release. The displayed format and dimensions describe the selected decoded image, not its original upload format. Outside the demo, callers must pass their own target size; Compose modifiers do not automatically reach the SDK.

`catalog.json` and generated `AppAssets.kt` are committed. Regenerate without network access:

```sh
node scripts/generate-assets.mjs catalog.json app/src/main/java/com/assetlib/demo/AppAssets.kt com.assetlib.demo
node scripts/generate-assets.mjs catalog.json app/src/main/java/com/assetlib/demo/AppAssets.kt com.assetlib.demo --check
```

This preview does not scan source usage, upload source code, contain analytics, or integrate Figma, A/B tests, billing or bookings. Saved destinations are a local demo interaction, not a travel reservation service.

## Checks and distribution

```sh
./gradlew :app:testDebugUnitTest :app:lint :app:assembleDebug :app:assembleRelease
```

The release build enables R8 and resource shrinking. It produces an unsigned release APK; publishing a signed store build needs your own signing process. Never commit a keystore or credentials. CI runs the generated-reference check, lint, tests and debug/release assembly. See the SDK repository for signed-protocol, concurrency, cache and native decoder tests.

Artwork in `app/src/main/res/drawable-nodpi` is original Assetlib demo artwork shared with the web/Expo examples and provided under this repository's MIT license. The coast WebP is the normalized original illustration, not third-party travel photography. Dependency notices are in `THIRD-PARTY-NOTICES.md`.

An optional read-only hosted test runs the released SDK on an emulator, verifies all three real images with the Android decoder, then resolves an independently restarted client with networking disabled:

```sh
ASSETLIB_PUBLIC_CONFIG_FILE=/absolute/path/public-config.json ./gradlew :app:connectedDebugAndroidTest
```

The file is copied only to ignored build output for the **test APK**, never to the demo APK or source control. With no environment variable the hosted test skips. Use public configuration only; do not distribute that test APK if the workspace identity should stay private. The test retains accepted replay-protection state and verified cache on the emulator.

The 0.2 preview was validated with a clean default build using the public, hash-locked AAR (no sibling override): unit checks, lint, debug/release assembly, and a read-only hosted emulator test resolving all three placements and an independent offline restart passed. The SDK's separate tests cover 65 signed manifest cases and four native decoder scenarios, including PNG rendition sizes and malformed PNG fallback. This is emulator evidence, not validation on every device or a native SVG-rendering claim.

On October 7, 2026, the public AAR also passed against a hosted release containing an SVG-backed coast placement with prepared renditions. Android selected and fully decoded its 2048 × 1536 WebP; the two legacy placements decoded at 1200 × 900 and 600 × 400. All three resolved the newly accepted sequence 4, and a separate client with networking disabled restored that sequence and the cached coast image. The test asserts the resolved sequence equals the accepted sequence so historical fallback cannot count as current-release success. This was a read-only SDK/emulator check, not a native UI publication test; hosted release numbers change as new releases are published.
