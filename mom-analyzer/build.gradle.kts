plugins {
    `java-library`
    application
}

application {
    mainClass.set("dev.mom.analyzer.JarAnalysisCli")
}

dependencies {
    api(project(":mom-api"))
    implementation(project(":mom-forge"))
    implementation(project(":mom-neoforge"))
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.ow2.asm:asm:9.8")
}
