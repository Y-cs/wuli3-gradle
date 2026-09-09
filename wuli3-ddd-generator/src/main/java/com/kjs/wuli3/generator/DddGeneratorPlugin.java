package com.kjs.wuli3.generator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * 为 Gradle 工程注册 DDD 业务服务生成任务。
 *
 * @author GuoYang create on 2026/9/8 15:00
 */
public final class DddGeneratorPlugin implements Plugin<Project> {
    /** Gradle 中稳定暴露的生成任务名称。 */
    public static final String TASK_NAME = "generateDddService";

    /** 注册生成任务并设置基础设施选项的默认值。 */
    @Override
    public void apply(final Project project) {
        project.getTasks()
                .register(
                        DddGeneratorPlugin.TASK_NAME,
                        GenerateDddServiceTask.class,
                        (final GenerateDddServiceTask task) -> {
                            task.setGroup("wuli3");
                            task.setDescription("生成单体多模块 DDD 业务服务。");
                            task.getPersistence().convention(GeneratorOptions.DEFAULT_PERSISTENCE);
                            task.getMessaging().convention(GeneratorOptions.DEFAULT_MESSAGING);
                            task.getWuli3Version().convention(GeneratorOptions.DEFAULT_WULI3_VERSION);
                            task.getBuildLogicVersion().convention(GeneratorOptions.DEFAULT_BUILD_LOGIC_VERSION);
                            task.getOutputDirectory()
                                    .convention(
                                            project.getRootProject().getLayout().getProjectDirectory());
                        });
    }
}
