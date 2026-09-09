plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    api(project(":domain"))
    implementation("org.springframework:spring-context")
}
