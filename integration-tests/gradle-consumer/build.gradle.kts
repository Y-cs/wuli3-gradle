plugins {
    java
}

dependencies {
    implementation(platform("com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT"))
    implementation("com.kjs.wuli3:wuli3-core")
    implementation("com.kjs.wuli3:wuli3-json")
    implementation("com.kjs.wuli3:wuli3-event-core")
    implementation("com.kjs.wuli3:wuli3-event-spring-boot-starter")
    implementation("com.kjs.wuli3:wuli3-rocketmq-spring-boot-starter")
    implementation("com.kjs.wuli3:wuli3-context-propagation")
    implementation("com.kjs.wuli3:wuli3-web-spring-boot-starter")
    implementation("com.kjs.wuli3:wuli3-redis-spring-boot-starter")
    compileOnly("org.apache.rocketmq:rocketmq-client-java")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

tasks.test {
    useJUnitPlatform()
}
