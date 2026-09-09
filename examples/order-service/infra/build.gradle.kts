dependencies {
    implementation(project(":app"))
    implementation(project(":domain"))
    implementation("com.kjs.wuli3:wuli3-mysql-spring-boot-starter")
    implementation("com.kjs.wuli3:wuli3-rocketmq-spring-boot-starter")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
}
