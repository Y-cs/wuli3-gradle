plugins {
    base
    id("com.kjs.wuli3.java-conventions") version "0.1.0-SNAPSHOT" apply false
    id("org.springframework.boot") version "3.5.15" apply false
}

allprojects {
    group = "com.example.order"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    pluginManager.apply("com.kjs.wuli3.java-conventions")
}
