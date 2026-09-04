plugins {
    id("com.kjs.wuli3.spring-conventions")
}

description = "Spring Boot audit log protocol, context enrichment, and event-based transport."

dependencies {
    api(project(":wuli3-core-spring-boot-starter"))
    api(project(":wuli3-event-core"))
    api(project(":wuli3-core"))
    api(project(":wuli3-context-propagation"))
    api(project(":wuli3-opentelemetry-spring-boot-starter"))
    api(project(":wuli3-event-spring-boot-starter"))
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework:spring-aop")
    implementation("org.springframework:spring-expression")
    compileOnly("org.jspecify:jspecify")
    testImplementation("org.jspecify:jspecify")
}
