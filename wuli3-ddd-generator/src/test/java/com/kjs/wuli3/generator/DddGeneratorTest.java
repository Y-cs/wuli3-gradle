package com.kjs.wuli3.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 验证 DDD 生成器的模块边界、选项渲染和写入保护。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
final class DddGeneratorTest {
    @TempDir
    private Path output;

    /** 验证默认生成完整的七模块工程和架构门禁。 */
    @Test
    void generatesModularMonolithWithArchitectureRules() throws Exception {
        DddGenerator.main(new String[] {
            "generate",
            "--service",
            "order-service",
            "--package",
            "com.example.order",
            "--domain",
            "order",
            "--wuli3-version",
            "1.2.3",
            "--build-logic-version",
            "4.5.6",
            "--output",
            this.output.toString()
        });

        final Path root = this.output.resolve("order-service");
        assertThat(root.resolve("gradlew")).isExecutable();
        assertThat(Files.readString(root.resolve("settings.gradle.kts")))
                .contains(
                        "\"shared-kernel\"",
                        "\"domain\"",
                        "\"api\"",
                        "\"app\"",
                        "\"infra\"",
                        "\"adapter\"",
                        "\"bootstrap\"");
        assertThat(Files.readString(root.resolve("shared-kernel/build.gradle.kts")))
                .contains("wuli3-core", "verifySharedKernelDependencies", "allowedSharedKernelDependencies")
                .doesNotContain("spring", "project(\"", "`java-library`");
        assertThat(root.resolve("shared-kernel/src/test/java/com/example/order/sharedkernel/SharedKernelTest.java"))
                .exists();
        assertThat(Files.readString(root.resolve("build.gradle.kts")))
                .contains(
                        "java-conventions\") version \"4.5.6\"",
                        "pluginManager.apply(\"com.kjs.wuli3.java-conventions\")")
                .doesNotContain("JavaPluginExtension", "JavaLanguageVersion", "tasks.withType<JavaCompile>");
        assertThat(Files.readString(root.resolve("gradle.properties")))
                .contains(
                        "wuli3.version=1.2.3",
                        "wuli3.conventions.bom-coordinates=com.kjs.wuli3:wuli3-dependencies:1.2.3",
                        "wuli3.conventions.jacoco.verification.enabled=false");
        assertThat(Files.readString(root.resolve("domain/build.gradle.kts")))
                .contains("api(project(\":shared-kernel\"))")
                .doesNotContain("plugins", "java-library");
        assertThat(Files.readString(root.resolve("infra/build.gradle.kts")))
                .contains("project(\":app\")", "project(\":domain\")", "archunit-junit5")
                .doesNotContain("wuli3-mysql");
        assertThat(Files.readString(
                        root.resolve("infra/src/test/java/com/example/order/architecture/InfraArchitectureTest.java")))
                .contains("..app.port.out..", "onlyDependOnClassesThat()", "APP_OUTPUT_PORTS_ARE_INTERFACES")
                .doesNotContain("..app.internal..");
        assertThat(Files.readString(
                        root.resolve("app/src/main/java/com/example/order/app/internal/OrderApplicationService.java")))
                .contains("final class OrderApplicationService")
                .doesNotContain("public final class OrderApplicationService");
        assertThat(Files.readString(root.resolve(".wuli3/generation.properties")))
                .contains("template.version=1.0.0", "persistence=none", "messaging=none");
    }

    /** 验证基础设施选项只修改 Infra 组合并生成对应持久化类型。 */
    @Test
    void generatesSelectedInfrastructureAdapters() throws Exception {
        DddGenerator.main(new String[] {
            "generate",
            "--service",
            "catalog-service",
            "--package",
            "com.example.catalog",
            "--domain",
            "product-catalog",
            "--persistence",
            "mysql",
            "--messaging",
            "rocketmq",
            "--output",
            this.output.toString()
        });

        final Path root = this.output.resolve("catalog-service");
        assertThat(Files.readString(root.resolve("infra/build.gradle.kts")))
                .contains("wuli3-mysql-spring-boot-starter", "wuli3-rocketmq-spring-boot-starter");
        assertThat(root.resolve("infra/src/main/java/com/example/catalog/infra/persistence/ProductCatalogMapper.java"))
                .exists();
        assertThat(root.resolve("domain/src/main/java/com/example/catalog/domain/product_catalog/ProductCatalog.java"))
                .exists();
        assertThat(
                        Files.readString(
                                root.resolve(
                                        "infra/src/main/java/com/example/catalog/infra/persistence/ProductCatalogRepositoryAdapter.java")))
                .contains(
                        "public class ProductCatalogRepositoryAdapter",
                        "private final ProductCatalogMapper mapper",
                        "this.mapper.selectById")
                .doesNotContain("public final class ProductCatalogRepositoryAdapter");
        assertThat(Files.readString(root.resolve("bootstrap/src/main/resources/application.yml")))
                .contains("name: catalog-service", "service-code: catalog-service")
                .doesNotContain("generator:", "messaging:");
        assertThat(Files.readString(
                        root.resolve("bootstrap/src/test/java/com/example/catalog/BootstrapApplicationTest.java")))
                .contains(
                        "import com.example.catalog.infra.persistence.ProductCatalogMapper;",
                        "@MockitoBean(types = ProductCatalogMapper.class)",
                        "@ImportAutoConfiguration(",
                        "exclude = {",
                        "DataSourceAutoConfiguration.class",
                        "JdbcTemplateAutoConfiguration.class");
    }

    /** 验证必填参数缺失时给出明确错误。 */
    @Test
    void rejectsMissingRequiredArguments() {
        assertThatThrownBy(() -> DddGenerator.main(new String[] {"generate", "--service", "order-service"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("缺少 --package");
    }

    /** 验证生成器不会覆盖非空目标目录。 */
    @Test
    void rejectsNonEmptyTargetDirectory() throws Exception {
        final Path root = Files.createDirectories(this.output.resolve("order-service"));
        Files.writeString(root.resolve("owned-by-user.txt"), "keep");

        assertThatThrownBy(() -> DddGenerator.main(new String[] {
                    "generate",
                    "--service",
                    "order-service",
                    "--package",
                    "com.example.order",
                    "--domain",
                    "order",
                    "--output",
                    this.output.toString()
                }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("目标目录非空");
        assertThat(root.resolve("owned-by-user.txt")).hasContent("keep");
    }
}
