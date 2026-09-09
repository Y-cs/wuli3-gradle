package com.kjs.wuli3.generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 使用内置版本化模板生成单体多模块 DDD 工程。
 *
 * 注意：生成器拒绝写入非空目录，避免覆盖业务代码。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
final class DddProjectGenerator {
    private static final String TEMPLATE_ROOT = "/ddd-template/";
    private static final String TEMPLATE_VERSION = "1.0.0";
    private static final List<TemplateFile> BASE_FILES = List.of(
            file("settings.gradle.kts.tpl", "settings.gradle.kts"),
            file("build.gradle.kts.tpl", "build.gradle.kts"),
            file("gradle.properties.tpl", "gradle.properties"),
            file("README.md.tpl", "README.md"),
            file("generation.properties.tpl", ".wuli3/generation.properties"),
            file("shared-kernel.gradle.kts.tpl", "shared-kernel/build.gradle.kts"),
            file("domain.gradle.kts.tpl", "domain/build.gradle.kts"),
            file("api.gradle.kts.tpl", "api/build.gradle.kts"),
            file("app.gradle.kts.tpl", "app/build.gradle.kts"),
            file("infra.gradle.kts.tpl", "infra/build.gradle.kts"),
            file("adapter.gradle.kts.tpl", "adapter/build.gradle.kts"),
            file("bootstrap.gradle.kts.tpl", "bootstrap/build.gradle.kts"),
            file("Status.java.tpl", "shared-kernel/src/main/java/{{packagePath}}/sharedkernel/Status.java"),
            file("EntityId.java.tpl", "shared-kernel/src/main/java/{{packagePath}}/sharedkernel/EntityId.java"),
            file(
                    "SharedKernelArchitectureTest.java.tpl",
                    "shared-kernel/src/test/java/{{packagePath}}/architecture/SharedKernelArchitectureTest.java"),
            file(
                    "Domain.java.tpl",
                    "domain/src/main/java/{{packagePath}}/domain/{{domainPackage}}/{{domainType}}.java"),
            file(
                    "DomainTest.java.tpl",
                    "domain/src/test/java/{{packagePath}}/domain/{{domainPackage}}/{{domainType}}Test.java"),
            file("DomainApi.java.tpl", "api/src/main/java/{{packagePath}}/api/{{domainType}}Api.java"),
            file("DomainStatusView.java.tpl", "api/src/main/java/{{packagePath}}/api/{{domainType}}StatusView.java"),
            file("DomainUseCase.java.tpl", "app/src/main/java/{{packagePath}}/app/port/in/{{domainType}}UseCase.java"),
            file(
                    "DomainRepository.java.tpl",
                    "app/src/main/java/{{packagePath}}/app/port/out/{{domainType}}Repository.java"),
            file(
                    "DomainApplicationService.java.tpl",
                    "app/src/main/java/{{packagePath}}/app/internal/{{domainType}}ApplicationService.java"),
            file(
                    "DomainRepositoryAdapter.java.tpl",
                    "infra/src/main/java/{{packagePath}}/infra/persistence/{{domainType}}RepositoryAdapter.java"),
            file(
                    "InfraArchitectureTest.java.tpl",
                    "infra/src/test/java/{{packagePath}}/architecture/InfraArchitectureTest.java"),
            file(
                    "DomainController.java.tpl",
                    "adapter/src/main/java/{{packagePath}}/adapter/in/web/{{domainType}}Controller.java"),
            file("BootstrapApplication.java.tpl", "bootstrap/src/main/java/{{packagePath}}/BootstrapApplication.java"),
            file("application.yml.tpl", "bootstrap/src/main/resources/application.yml"),
            file(
                    "BootstrapApplicationTest.java.tpl",
                    "bootstrap/src/test/java/{{packagePath}}/BootstrapApplicationTest.java"));
    private static final List<TemplateFile> MYSQL_FILES = List.of(
            file(
                    "mysql/DomainPo.java.tpl",
                    "infra/src/main/java/{{packagePath}}/infra/persistence/{{domainType}}Po.java"),
            file(
                    "mysql/DomainMapper.java.tpl",
                    "infra/src/main/java/{{packagePath}}/infra/persistence/{{domainType}}Mapper.java"));
    private static final Map<String, String> WRAPPER_FILES = Map.of(
            "wrapper/gradlew", "gradlew",
            "wrapper/gradlew.bat", "gradlew.bat",
            "wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.jar",
            "wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.properties");

    /** 生成工程并返回工程根目录。 */
    Path generate(final GeneratorOptions options) throws IOException {
        final Path root = options.output().resolve(options.service()).normalize();
        this.requireWritableTarget(root);
        Files.createDirectories(root);
        final Map<String, String> variables = this.variables(options);

        final List<TemplateFile> files = new ArrayList<>(DddProjectGenerator.BASE_FILES);
        if (options.persistence().equals("mysql")) {
            files.addAll(DddProjectGenerator.MYSQL_FILES);
        }
        for (final TemplateFile template : files) {
            this.render(root, template, variables);
        }
        for (final Map.Entry<String, String> wrapper : DddProjectGenerator.WRAPPER_FILES.entrySet()) {
            this.copyResource(root, wrapper.getKey(), wrapper.getValue());
        }
        this.makeExecutable(root.resolve("gradlew"));
        return root;
    }

    private void requireWritableTarget(final Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("目标路径不是目录: " + root);
        }
        try (var entries = Files.list(root)) {
            if (entries.findAny().isPresent()) {
                throw new IllegalArgumentException("目标目录非空: " + root);
            }
        }
    }

    private Map<String, String> variables(final GeneratorOptions options) {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put("service", options.service());
        result.put("basePackage", options.basePackage());
        result.put("packagePath", options.basePackage().replace('.', '/'));
        result.put("domain", options.domain());
        result.put("domainPackage", options.domain().replace('-', '_'));
        result.put("domainType", options.domainType());
        result.put("persistence", options.persistence());
        result.put("messaging", options.messaging());
        result.put("wuli3Version", options.wuli3Version());
        result.put("buildLogicVersion", options.buildLogicVersion());
        result.put("templateVersion", DddProjectGenerator.TEMPLATE_VERSION);
        result.put("infraDependencies", this.infraDependencies(options));
        result.put("messagingConfiguration", this.messagingConfiguration(options));
        result.put("repositoryFields", this.repositoryFields(options));
        result.put("repositoryConstructor", this.repositoryConstructor(options));
        result.put("repositoryLoad", this.repositoryLoad(options));
        return result;
    }

    private String infraDependencies(final GeneratorOptions options) {
        final StringBuilder result = new StringBuilder();
        if (options.persistence().equals("mysql")) {
            result.append("    implementation(\"com.kjs.wuli3:wuli3-mysql-spring-boot-starter\")\n");
        } else {
            result.append("    implementation(\"org.springframework:spring-context\")\n");
        }
        if (options.messaging().equals("rocketmq")) {
            result.append("    implementation(\"com.kjs.wuli3:wuli3-rocketmq-spring-boot-starter\")\n");
        } else if (options.messaging().equals("rabbitmq")) {
            result.append("    implementation(\"com.kjs.wuli3:wuli3-rabbitmq-spring-boot-starter\")\n");
        }
        return result.toString();
    }

    private String messagingConfiguration(final GeneratorOptions options) {
        return switch (options.messaging()) {
            case "rocketmq" -> "  messaging: rocketmq";
            case "rabbitmq" -> "  messaging: rabbitmq";
            default -> "  messaging: none";
        };
    }

    private String repositoryFields(final GeneratorOptions options) {
        if (!options.persistence().equals("mysql")) {
            return "";
        }
        return "    private final " + options.domainType() + "Mapper mapper;\n";
    }

    private String repositoryConstructor(final GeneratorOptions options) {
        if (!options.persistence().equals("mysql")) {
            return "";
        }
        return "\n    public " + options.domainType() + "RepositoryAdapter(final " + options.domainType()
                + "Mapper mapper) {\n        this.mapper = mapper;\n    }\n";
    }

    private String repositoryLoad(final GeneratorOptions options) {
        if (!options.persistence().equals("mysql")) {
            return "        return new " + options.domainType() + "(new EntityId(\"sample-1\"));";
        }
        return "        final " + options.domainType() + "Po stored = this.mapper.selectById(\"sample-1\");\n"
                + "        if (stored == null) {\n"
                + "            return new " + options.domainType() + "(new EntityId(\"sample-1\"));\n"
                + "        }\n"
                + "        return stored.toDomain();";
    }

    private void render(final Path root, final TemplateFile template, final Map<String, String> variables)
            throws IOException {
        String content = this.readText(template.resource());
        String relativePath = template.target();
        for (final Map.Entry<String, String> variable : variables.entrySet()) {
            final String token = "{{" + variable.getKey() + "}}";
            content = content.replace(token, variable.getValue());
            relativePath = relativePath.replace(token, variable.getValue());
        }
        if (content.contains("{{") || relativePath.contains("{{")) {
            throw new IllegalStateException("模板包含未解析变量: " + template.resource());
        }
        this.write(root.resolve(relativePath), content.getBytes(StandardCharsets.UTF_8));
    }

    private void copyResource(final Path root, final String resource, final String target) throws IOException {
        try (InputStream input = this.resource(resource)) {
            this.write(root.resolve(target), input.readAllBytes());
        }
    }

    private String readText(final String resource) throws IOException {
        try (InputStream input = this.resource(resource)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private InputStream resource(final String resource) {
        return Objects.requireNonNull(
                DddProjectGenerator.class.getResourceAsStream(DddProjectGenerator.TEMPLATE_ROOT + resource),
                "缺少模板资源: " + resource);
    }

    private void write(final Path target, final byte[] content) throws IOException {
        Files.createDirectories(Objects.requireNonNull(target.getParent()));
        Files.write(target, content);
    }

    private void makeExecutable(final Path script) throws IOException {
        try {
            final Set<PosixFilePermission> permissions = EnumSet.copyOf(Files.getPosixFilePermissions(script));
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            Files.setPosixFilePermissions(script, permissions);
        } catch (final UnsupportedOperationException ignored) {
            // Windows 文件系统不支持 POSIX 权限，gradlew.bat 仍可直接使用。
        }
    }

    private static TemplateFile file(final String resource, final String target) {
        return new TemplateFile(resource, target);
    }

    private record TemplateFile(String resource, String target) {}
}
