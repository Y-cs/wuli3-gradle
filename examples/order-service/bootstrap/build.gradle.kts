plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":infra"))
    implementation(project(":adapter"))
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("com.example.order.BootstrapApplication")
}
