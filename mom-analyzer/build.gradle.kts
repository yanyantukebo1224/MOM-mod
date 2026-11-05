plugins { `java-library` }
dependencies {
    api(project(":mom-api"))
    implementation(project(":mom-forge"))
    implementation(project(":mom-neoforge"))
    implementation("com.google.code.gson:gson:2.13.2")
}
