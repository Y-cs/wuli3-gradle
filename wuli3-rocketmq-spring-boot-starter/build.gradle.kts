plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-event-spring-boot-starter"))
    implementation(project(":wuli3-json"))
    api("org.apache.rocketmq:rocketmq-spring-boot-starter")
    implementation("io.cloudevents:cloudevents-core")
    implementation("io.cloudevents:cloudevents-json-jackson")
}
