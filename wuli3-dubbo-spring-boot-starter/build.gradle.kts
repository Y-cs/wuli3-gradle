plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-core"))
    api(project(":wuli3-context-propagation"))
    implementation("org.apache.dubbo:dubbo-spring-boot-starter")
    compileOnly("org.apache.dubbo:dubbo-registry-nacos")
}
