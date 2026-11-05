plugins { `java-library` }
dependencies {
    api(project(":mom-api"))
    implementation("com.google.code.gson:gson:2.13.2")
}
