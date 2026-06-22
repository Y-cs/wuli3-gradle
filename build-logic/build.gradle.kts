plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.7.0")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.5.8")
    implementation("de.thetaphi:forbiddenapis:3.10")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
}

gradlePlugin {
    plugins {
        register("javaConventions") {
            id = "com.kjs.wuli3.java-conventions"
            implementationClass = "com.kjs.wuli3.JavaConventionsPlugin"
        }
        register("qualityConventions") {
            id = "com.kjs.wuli3.quality-conventions"
            implementationClass = "com.kjs.wuli3.QualityConventionsPlugin"
        }
        register("springConventions") {
            id = "com.kjs.wuli3.spring-conventions"
            implementationClass = "com.kjs.wuli3.SpringConventionsPlugin"
        }
    }
}
