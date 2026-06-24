plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core"))
    implementation(project(":wuli3-json"))
    api("com.google.guava:guava")
    api("cn.hutool:hutool-all")
    api("org.apache.commons:commons-lang3")
    api("org.apache.commons:commons-collections4")
    api("org.apache.commons:commons-text")
    api("commons-io:commons-io")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
}
