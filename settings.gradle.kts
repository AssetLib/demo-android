pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "assetlib-travel-android"
include(":app")
val localSdk = providers.gradleProperty("assetlibSdkDir").orNull
if(localSdk != null) { include(":assetlib-sdk"); project(":assetlib-sdk").projectDir = file("$localSdk/sdk") }
