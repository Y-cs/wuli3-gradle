plugins {
    application
    `java-gradle-plugin`
    id("com.kjs.wuli3.java-conventions")
}

application {
    mainClass.set("com.kjs.wuli3.generator.DddGenerator")
}

gradlePlugin {
    plugins {
        register("dddGenerator") {
            id = "com.kjs.wuli3.ddd-generator"
            implementationClass = "com.kjs.wuli3.generator.DddGeneratorPlugin"
            displayName = "Wuli3 DDD Generator"
            description = "生成带架构门禁的单体多模块 DDD 业务服务。"
        }
    }
}

tasks.processResources {
    from(rootProject.file("gradlew")) {
        into("ddd-template/wrapper")
    }
    from(rootProject.file("gradlew.bat")) {
        into("ddd-template/wrapper")
    }
    from(rootProject.file("gradle/wrapper/gradle-wrapper.jar")) {
        into("ddd-template/wrapper")
    }
    from(rootProject.file("gradle/wrapper/gradle-wrapper.properties")) {
        into("ddd-template/wrapper")
    }
}
