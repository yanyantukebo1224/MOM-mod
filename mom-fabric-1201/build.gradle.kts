plugins { `java-library` }
dependencies {
    implementation(project(":mom-api"))
    compileOnly("net.fabricmc:fabric-loader:0.15.11")
}
