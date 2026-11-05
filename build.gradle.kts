plugins { base }

allprojects {
    group = "dev.mom"
    version = "0.2.0-SNAPSHOT"
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.fabricmc.net/") }
    }
}

subprojects {
    apply(plugin = "java-library")
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(if (project.name == "mom-fabric-263") 25 else 17)
    }
    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.13.4"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
    }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
