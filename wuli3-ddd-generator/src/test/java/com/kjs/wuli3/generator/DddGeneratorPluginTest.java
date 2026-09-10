package com.kjs.wuli3.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 验证 Gradle 插件注册的任务配置和生成行为。
 *
 * @author GuoYang create on 2026/9/8 15:00
 */
final class DddGeneratorPluginTest {
    @TempDir
    private Path projectDirectory;

    /** 验证插件任务复用生成内核并应用约定默认值。 */
    @Test
    void generatesServiceThroughGradleTask() throws Exception {
        final Project project = ProjectBuilder.builder()
                .withProjectDir(this.projectDirectory.toFile())
                .build();
        project.getPluginManager().apply(DddGeneratorPlugin.class);
        final GenerateDddServiceTask task =
                (GenerateDddServiceTask) project.getTasks().getByName(DddGeneratorPlugin.TASK_NAME);
        task.getService().set("billing-service");
        task.getBasePackage().set("com.example.billing");
        task.getDomain().set("billing");

        task.generate();

        final Path generated = this.projectDirectory.resolve("billing-service");
        assertThat(generated.resolve("settings.gradle.kts")).exists();
        assertThat(Files.readString(generated.resolve("AGENTS.md")))
                .contains("# billing-service 项目协作规范", "`com.example.billing`")
                .doesNotContain("{{");
        assertThat(Files.readString(generated.resolve(".wuli3/generation.properties")))
                .contains(
                        "persistence=" + GeneratorOptions.DEFAULT_PERSISTENCE,
                        "messaging=" + GeneratorOptions.DEFAULT_MESSAGING,
                        "wuli3.version=" + GeneratorOptions.DEFAULT_WULI3_VERSION,
                        "build-logic.version=" + GeneratorOptions.DEFAULT_BUILD_LOGIC_VERSION);
    }

    /** 验证真实 Gradle 命令行能够绑定任务选项并生成服务。 */
    @Test
    void bindsCommandLineOptions() throws Exception {
        Files.writeString(
                this.projectDirectory.resolve("settings.gradle.kts"), "rootProject.name = \"generator-test\"\n");
        Files.writeString(
                this.projectDirectory.resolve("build.gradle.kts"), "plugins { id(\"com.kjs.wuli3.ddd-generator\") }\n");
        final Path output = this.projectDirectory.resolve("generated");

        final BuildResult result = GradleRunner.create()
                .withProjectDir(this.projectDirectory.toFile())
                .withPluginClasspath()
                .withArguments(
                        DddGeneratorPlugin.TASK_NAME,
                        "--service",
                        "order-service",
                        "--base-package",
                        "com.example.order",
                        "--domain",
                        "order",
                        "--output",
                        output.toString(),
                        "--stacktrace")
                .build();
        final BuildTask generatedTask = Objects.requireNonNull(result.task(":" + DddGeneratorPlugin.TASK_NAME));

        assertThat(generatedTask.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(output.resolve("order-service/settings.gradle.kts")).exists();
        assertThat(output.resolve("order-service/AGENTS.md")).isRegularFile();
    }
}
