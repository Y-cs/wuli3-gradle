import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.WriteProperties

plugins {
    base
}

group = "com.kjs.wuli3"
version = "0.1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

val publishAllPublicationsToTemporaryRepository = tasks.register("publishAllPublicationsToTemporaryRepository") {
    group = "publishing"
    description = "Publishes every public component and the BOM to the temporary Maven repository."
}

gradle.projectsEvaluated {
    publishAllPublicationsToTemporaryRepository.configure {
        dependsOn(subprojects.mapNotNull { project ->
            project.tasks.findByName("publishAllPublicationsToTemporaryRepository")
        })
    }
}

val verifyGradleBomConsumer = tasks.register<Exec>("verifyGradleBomConsumer") {
    group = "verification"
    dependsOn(publishAllPublicationsToTemporaryRepository)
    workingDir(layout.projectDirectory.dir("integration-tests/gradle-consumer"))
    commandLine("../../gradlew", "--no-daemon", "--gradle-user-home", "../../build/consumer-gradle-home", "test")
}

val cleanMavenConsumerRepository = tasks.register<Delete>("cleanMavenConsumerRepository") {
    delete(layout.buildDirectory.dir("consumer-maven-home"))
}

val verifyMavenBomConsumer = tasks.register<Exec>("verifyMavenBomConsumer") {
    group = "verification"
    dependsOn(publishAllPublicationsToTemporaryRepository, cleanMavenConsumerRepository)
    workingDir(layout.projectDirectory.dir("integration-tests/maven-consumer"))
    commandLine(
        "mvn",
        "--settings",
        "settings.xml",
        "-Dmaven.repo.local=../../build/consumer-maven-home",
        "test",
    )
}

tasks.register("verifyBomConsumers") {
    group = "verification"
    description = "Verifies versionless Gradle and Maven consumption through the Wuli3 BOM."
    dependsOn(verifyGradleBomConsumer, verifyMavenBomConsumer)
}

tasks.register<WriteProperties>("apiCompatibilityCheck") {
    group = "verification"
    description = "Records API compatibility baseline status for the first unpublished version."
    destinationFile = layout.buildDirectory.file("reports/api-compatibility/status.properties")
    property("status", "baseline-created")
    property("version", version.toString())
    property("comparison", "skipped-no-published-baseline")
}
