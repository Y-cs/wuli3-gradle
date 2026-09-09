plugins {
    `java-library`
}

dependencies {
    api("com.kjs.wuli3:wuli3-core:${providers.gradleProperty("wuli3.version").get()}")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
}

// 质量约定默认注入 JSpecify；Shared Kernel 不使用注解，因此移除该非业务依赖。
configurations.matching {
    it.name in setOf("api", "implementation", "compileOnly", "compileOnlyApi", "runtimeOnly", "annotationProcessor")
}.configureEach {
    withDependencies {
        removeIf {
            (it.group == "org.jspecify" && it.name == "jspecify")
                || (it.group == "com.kjs.wuli3" && it.name == "wuli3-dependencies")
        }
    }
}

val sharedKernelProductionConfigurations =
    listOf("api", "implementation", "compileOnly", "compileOnlyApi", "runtimeOnly", "annotationProcessor")
val allowedSharedKernelDependencies =
    setOf("com.kjs.wuli3:wuli3-core")

val verifySharedKernelDependencies = tasks.register("verifySharedKernelDependencies") {
    group = "verification"
    description = "验证 Shared Kernel 的生产依赖只包含 wuli3-core。"

    doLast {
        val forbiddenDependencies =
            sharedKernelProductionConfigurations
                .flatMap { configurations.getByName(it).dependencies }
                .map { "${it.group ?: "local"}:${it.name}" }
                .filterNot(allowedSharedKernelDependencies::contains)
                .distinct()
                .sorted()
        check(forbiddenDependencies.isEmpty()) {
            "Shared Kernel 包含禁止的生产依赖: ${forbiddenDependencies.joinToString()}"
        }
    }
}

tasks.named("check") {
    dependsOn(verifySharedKernelDependencies)
}
