plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core"))
    api(project(":wuli3-json"))
    api(project(":wuli3-context-propagation"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")

    implementation("com.google.guava:guava")
    implementation("cn.hutool:hutool-all")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4")
    implementation("org.apache.commons:commons-text")
    implementation("commons-io:commons-io")
}
