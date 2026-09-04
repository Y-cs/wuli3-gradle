plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core-spring-boot-starter"))
    api(project(":wuli3-core"))
    api(project(":wuli3-json"))
    api(project(":wuli3-context-propagation"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.jspecify:jspecify")
    testImplementation("org.jspecify:jspecify")
}
