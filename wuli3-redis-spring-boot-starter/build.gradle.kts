plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core-spring-boot-starter"))
    api(project(":wuli3-json"))
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.redisson:redisson-spring-boot-starter")
}
