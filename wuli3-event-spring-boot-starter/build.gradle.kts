plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    api(project(":wuli3-event-core"))
    implementation("org.springframework:spring-tx")
}
