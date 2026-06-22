plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.15"))
    api(platform("com.baomidou:mybatis-plus-bom:3.5.16"))

    constraints {
        api("org.jspecify:jspecify:1.0.0")
        api("com.google.errorprone:error_prone_core:2.50.0")
        api("com.uber.nullaway:nullaway:0.13.7")
        api("com.puppycrawl.tools:checkstyle:13.6.0")
        api("org.redisson:redisson-spring-boot-starter:4.6.1")
        api("org.junit.jupiter:junit-jupiter:5.12.2")
        api("org.assertj:assertj-core:3.27.3")
        api("org.apache.rocketmq:rocketmq-spring-boot-starter:2.3.4")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenBom") {
            from(components["javaPlatform"])

            pom {
                name.set("wuli3-dependencies")
                description.set("Dependency management BOM for wuli3 projects.")
            }
        }
    }
}
