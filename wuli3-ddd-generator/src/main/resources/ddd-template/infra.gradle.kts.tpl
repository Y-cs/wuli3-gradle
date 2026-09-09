dependencies {
    implementation(project(":app"))
    implementation(project(":domain"))
{{infraDependencies}}    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
}
