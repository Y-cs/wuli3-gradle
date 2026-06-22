plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core"))
    api("org.apache.rocketmq:rocketmq-spring-boot-starter")
}
