plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core-spring-boot-starter"))
    api(project(":wuli3-event-spring-boot-starter"))
    api(project(":wuli3-context-propagation"))
    api("org.springframework.boot:spring-boot-starter-amqp")
    implementation(project(":wuli3-json"))
}
