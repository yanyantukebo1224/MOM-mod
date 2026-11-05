plugins { `java-library` }

dependencies {
    api(project(":mom-api"))
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
}
