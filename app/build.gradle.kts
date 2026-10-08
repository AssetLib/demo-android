plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "com.assetlib.demo"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    defaultConfig { applicationId = "com.assetlib.demo.travel"; minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; targetSdk = 36; versionCode = 1; versionName = "0.1.0-preview.1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    buildTypes { release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt")) } }
    lint { abortOnError = true }
    sourceSets["androidTest"].assets.srcDir(layout.buildDirectory.dir("generated/hosted-test-assets"))
}
val fetchSdk by tasks.registering(Exec::class) {
    workingDir(rootProject.projectDir)
    commandLine("node","scripts/fetch-sdk.mjs")
    inputs.file(rootProject.file("sdk-release.json"))
    // Always verify the digest, including when the AAR already exists.
}
dependencies {
    if(findProject(":assetlib-sdk") != null) implementation(project(":assetlib-sdk"))
    else implementation(files("libs/assetlib-android-0.1.0-preview.1.aar").builtBy(fetchSdk))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.86")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

// Optional public-only configuration is copied into the test APK, never the demo APK.
val prepareHostedTestAssets by tasks.registering {
    doLast {
        val folder=layout.buildDirectory.dir("generated/hosted-test-assets").get().asFile
        folder.mkdirs()
        val output=folder.resolve("assetlib-hosted-config.json")
        output.delete()
        val source=System.getenv("ASSETLIB_PUBLIC_CONFIG_FILE")
        if(!source.isNullOrBlank()) {
            val bytes=file(source).readBytes()
            require(bytes.size <= 8192) { "Public configuration exceeds the limit." }
            output.writeBytes(bytes)
        }
    }
}
tasks.matching { it.name == "preDebugAndroidTestBuild" }.configureEach { dependsOn(prepareHostedTestAssets) }
