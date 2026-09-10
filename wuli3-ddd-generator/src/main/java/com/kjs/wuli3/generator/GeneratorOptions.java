package com.kjs.wuli3.generator;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;

/**
 * DDD 服务生成参数及其合法性约束。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
record GeneratorOptions(
        String service,
        String basePackage,
        String domain,
        String persistence,
        String messaging,
        String wuli3Version,
        String buildLogicVersion,
        Path output,
        String domainType) {
    static final String DEFAULT_PERSISTENCE = "none";
    static final String DEFAULT_MESSAGING = "none";
    static final String DEFAULT_WULI3_VERSION = "0.1.0-SNAPSHOT";
    static final String DEFAULT_BUILD_LOGIC_VERSION = "0.1.0-SNAPSHOT";
    private static final List<String> KEYS = List.of(
            "service",
            "package",
            "domain",
            "persistence",
            "messaging",
            "wuli3-version",
            "build-logic-version",
            "output");

    /** 解析严格的 {@code --name value} 参数。 */
    static GeneratorOptions parse(final String[] args) {
        final Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            final String argument = args[index];
            if (!argument.startsWith("--") || index + 1 >= args.length || args[index + 1].startsWith("--")) {
                throw new IllegalArgumentException("参数必须以 --name value 形式提供: " + argument);
            }
            final String key = argument.substring(2);
            if (!GeneratorOptions.KEYS.contains(key)) {
                throw new IllegalArgumentException("不支持的参数: --" + key);
            }
            if (values.put(key, args[++index]) != null) {
                throw new IllegalArgumentException("参数重复: --" + key);
            }
        }

        final String service = GeneratorOptions.required(values, "service");
        final String basePackage = GeneratorOptions.required(values, "package");
        final String domain = GeneratorOptions.required(values, "domain");
        if (!service.matches("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("service 必须是小写字母、数字和单个连字符组成的名称");
        }
        if (!domain.matches("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")
                || !SourceVersion.isName(domain.replace('-', '_'), SourceVersion.RELEASE_21)) {
            throw new IllegalArgumentException("domain 必须是小写字母、数字和单个连字符组成的名称，且转换后必须是合法 Java 包名");
        }
        if (!basePackage.matches("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*")
                || !SourceVersion.isName(basePackage, SourceVersion.RELEASE_21)) {
            throw new IllegalArgumentException("package 必须是合法的小写 Java 包名");
        }

        final String persistence = values.getOrDefault("persistence", GeneratorOptions.DEFAULT_PERSISTENCE);
        final String messaging = values.getOrDefault("messaging", GeneratorOptions.DEFAULT_MESSAGING);
        if (!List.of("none", "mysql").contains(persistence)) {
            throw new IllegalArgumentException("不支持的 persistence: " + persistence);
        }
        if (!List.of("none", "rocketmq", "rabbitmq").contains(messaging)) {
            throw new IllegalArgumentException("不支持的 messaging: " + messaging);
        }
        final Path output =
                Path.of(values.getOrDefault("output", ".")).toAbsolutePath().normalize();
        return new GeneratorOptions(
                service,
                basePackage,
                domain,
                persistence,
                messaging,
                values.getOrDefault("wuli3-version", GeneratorOptions.DEFAULT_WULI3_VERSION),
                values.getOrDefault("build-logic-version", GeneratorOptions.DEFAULT_BUILD_LOGIC_VERSION),
                output,
                GeneratorOptions.toTypeName(domain));
    }

    private static String required(final Map<String, String> values, final String key) {
        final String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少 --" + key);
        }
        return value;
    }

    private static String toTypeName(final String value) {
        final StringBuilder result = new StringBuilder();
        for (final String part : value.split("-", -1)) {
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
}
