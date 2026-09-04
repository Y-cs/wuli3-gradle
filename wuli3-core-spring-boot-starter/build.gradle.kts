plugins {
    id("com.kjs.wuli3.spring-conventions")
}

description = "Shared Spring Boot infrastructure, logging, encryption, and graceful shutdown support."

dependencies {
    api(project(":wuli3-core"))
    api(project(":wuli3-context-propagation"))
    api("org.springframework.boot:spring-boot-starter")
    api("com.github.ulisesbocchio:jasypt-spring-boot-starter")
    compileOnly("ch.qos.logback:logback-classic")
}
