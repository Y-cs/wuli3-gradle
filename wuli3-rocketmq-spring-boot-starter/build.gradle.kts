plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-event-spring-boot-starter"))
    api(project(":wuli3-context-propagation"))
    api("org.apache.rocketmq:rocketmq-spring-boot-starter")
    implementation(project(":wuli3-json"))
    compileOnly("org.apache.rocketmq:rocketmq-client-java")
    testImplementation("org.apache.rocketmq:rocketmq-client-java")
}
