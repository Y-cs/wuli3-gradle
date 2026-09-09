package com.kjs.wuli3.generator;

import java.io.IOException;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.api.tasks.options.Option;

/**
 * 接收 Gradle 属性并创建一个新的单体多模块 DDD 业务服务。
 *
 * 注意：该任务是一次性脚手架操作，目标服务目录非空时会拒绝覆盖。
 *
 * @author GuoYang create on 2026/9/8 15:00
 */
@UntrackedTask(because = "脚手架生成属于一次性创建操作，不能覆盖或增量修改已有业务工程")
public abstract class GenerateDddServiceTask extends DefaultTask {
    /** 返回待生成的服务名称。 */
    @Input
    @Option(option = "service", description = "设置小写服务名，可包含数字和连字符。")
    public abstract Property<String> getService();

    /** 返回生成源码使用的 Java 基础包名。 */
    @Input
    @Option(option = "base-package", description = "设置小写 Java 基础包名。")
    public abstract Property<String> getBasePackage();

    /** 返回业务领域名称。 */
    @Input
    @Option(option = "domain", description = "设置领域名，可包含数字和连字符。")
    public abstract Property<String> getDomain();

    /** 返回持久化实现选项。 */
    @Input
    @Option(option = "persistence", description = "设置持久化实现：none 或 mysql。")
    public abstract Property<String> getPersistence();

    /** 返回消息实现选项。 */
    @Input
    @Option(option = "messaging", description = "设置消息实现：none、rocketmq 或 rabbitmq。")
    public abstract Property<String> getMessaging();

    /** 返回生成工程使用的 Wuli3 BOM 和 Starter 版本。 */
    @Input
    @Option(option = "wuli3-version", description = "设置 Wuli3 BOM 和 Starter 版本。")
    public abstract Property<String> getWuli3Version();

    /** 返回生成工程使用的 Wuli3 Gradle 约定插件版本。 */
    @Input
    @Option(option = "build-logic-version", description = "设置 Wuli3 Gradle 约定插件版本。")
    public abstract Property<String> getBuildLogicVersion();

    /** 返回生成服务的父目录。 */
    @Internal
    @Option(option = "output", description = "设置生成服务的父目录。")
    public abstract DirectoryProperty getOutputDirectory();

    /** 校验任务参数并调用共享的工程生成内核。 */
    @TaskAction
    public final void generate() throws IOException {
        final Path output = this.getOutputDirectory().get().getAsFile().toPath();
        final GeneratorOptions options = GeneratorOptions.parse(new String[] {
            "--service",
            this.getService().get(),
            "--package",
            this.getBasePackage().get(),
            "--domain",
            this.getDomain().get(),
            "--persistence",
            this.getPersistence().get(),
            "--messaging",
            this.getMessaging().get(),
            "--wuli3-version",
            this.getWuli3Version().get(),
            "--build-logic-version",
            this.getBuildLogicVersion().get(),
            "--output",
            output.toString()
        });
        final Path generated = DddGenerator.generate(options);
        this.getLogger().lifecycle("DDD 业务服务已生成: {}", generated);
    }
}
