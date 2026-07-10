plugins {
    id("com.kjs.wuli3.java-conventions")
}

dependencies {
    api(project(":wuli3-core"))
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("cn.hutool:hutool-all")
}
