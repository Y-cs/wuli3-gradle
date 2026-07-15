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
        api("com.kjs.wuli3:wuli3-core:${project.version}")
        api("com.kjs.wuli3:wuli3-json:${project.version}")
        api("com.kjs.wuli3:wuli3-event-core:${project.version}")
        api("com.kjs.wuli3:wuli3-context-propagation:${project.version}")
        api("com.kjs.wuli3:wuli3-web-spring-boot-starter:${project.version}")
        api("com.kjs.wuli3:wuli3-mysql-spring-boot-starter:${project.version}")
        api("com.kjs.wuli3:wuli3-redis-spring-boot-starter:${project.version}")
        api("com.kjs.wuli3:wuli3-rocketmq-spring-boot-starter:${project.version}")
        api("com.kjs.wuli3:wuli3-elasticsearch-spring-boot-starter:${project.version}")
        api("com.kjs.wuli3:wuli3-mongodb-spring-boot-starter:${project.version}")
        api("org.jspecify:jspecify:1.0.0")
        api("com.google.errorprone:error_prone_core:2.50.0")
        api("com.uber.nullaway:nullaway:0.13.7")
        api("com.puppycrawl.tools:checkstyle:13.6.0")
        api("org.redisson:redisson-spring-boot-starter:4.6.1")
        api("com.google.guava:guava:33.6.0-jre")
        api("cn.hutool:hutool-core:5.8.46")
        api("org.apache.commons:commons-lang3:3.20.0")
        api("org.apache.commons:commons-collections4:4.5.0")
        api("commons-io:commons-io:2.22.0")
        api("org.apache.commons:commons-text:1.15.0")
        api("it.unimi.dsi:fastutil:8.5.18")
        api("org.projectlombok:lombok:1.18.46")
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
    repositories {
        maven {
            name = "temporary"
            url = rootProject.layout.buildDirectory.dir("temporary-maven-repository").get().asFile.toURI()
        }
    }
}
