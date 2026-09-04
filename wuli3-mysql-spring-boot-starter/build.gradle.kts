plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core-spring-boot-starter"))
    api("com.baomidou:mybatis-plus-spring-boot3-starter")
    api("com.baomidou:mybatis-plus-jsqlparser-4.9")
}
