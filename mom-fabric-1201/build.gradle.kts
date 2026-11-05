plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.19-SNAPSHOT"
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

val forgeFixtureJar by tasks.registering(Jar::class) {
    archiveClassifier.set("forge-fixture")
    from(sourceSets.test.get().output)
    include("dev/mom/fabric/fixtures/**")
    from("src/test/resources/forge-fixture") {
        include("META-INF/mods.toml")
    }
    dependsOn(tasks.testClasses)
}
