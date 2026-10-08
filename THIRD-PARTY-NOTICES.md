# Third-party components

The SDK is MIT licensed. Dependencies retain their own licenses:

- Bouncy Castle `bcprov-jdk18on` 1.86: Bouncy Castle License (MIT-style); exact license retained in `licenses/bouncycastle-LICENSE.html`. Used directly for Ed25519 primitives with full public-point validation; no global JCA provider registration. Source: https://github.com/bcgit/bc-java/tree/r1rv86 .
- Kotlin standard library 2.2.20 and kotlinx coroutines 1.10.2 / serialization 1.9.0: Apache 2.0. https://github.com/JetBrains/kotlin and https://github.com/Kotlin/kotlinx.coroutines and https://github.com/Kotlin/kotlinx.serialization .
- OkHttp 4.12.0 / Okio: Apache 2.0. https://github.com/square/okhttp/tree/parent-4.12.0 .

The release AAR contains SDK code, not a shaded copy of these runtime dependencies. This demo resolves those runtime dependencies separately and must retain applicable dependency license notices in its distribution. The demo's release build enables R8 shrinking, but no specific size saving is promised.

The native demo also uses AndroidX Activity1.11.0, Lifecycle2.9.4, and Compose BOM2025.09.01/Material3 under Apache 2.0: https://android.googlesource.com/platform/frameworks/support . Gradle8.14.3 and Android Gradle Plugin8.13.2 are build tooling, not application SDK code.
