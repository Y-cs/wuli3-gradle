plugins {
    id("com.kjs.wuli3.spring-conventions")
}

description = "Spring Boot audit log protocol, context enrichment, and transport ports."

dependencies {
    api(project(":wuli3-event-core"))
    api(project(":wuli3-context-propagation"))
    api(project(":wuli3-opentelemetry-spring-boot-starter"))
    compileOnly("org.jspecify:jspecify")
    testImplementation("org.jspecify:jspecify")
}
