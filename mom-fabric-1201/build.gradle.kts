plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.19-SNAPSHOT"
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
    implementation(project(":mom-api"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
