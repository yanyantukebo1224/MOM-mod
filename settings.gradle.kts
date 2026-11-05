pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "MOM"
include(":mom-api", ":mom-analyzer", ":mom-transformer", ":mom-forge", ":mom-neoforge",
        ":mom-version-adapter", ":mom-fabric-1201", ":mom-fabric-263", ":mom-translation")
