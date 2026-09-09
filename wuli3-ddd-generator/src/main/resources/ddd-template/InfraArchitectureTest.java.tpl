package {{basePackage}}.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 验证 Infra 只能通过 App 输出端口接入应用层。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@AnalyzeClasses(packages = "{{basePackage}}", importOptions = ImportOption.DoNotIncludeTests.class)
final class InfraArchitectureTest {
    @ArchTest
    static final ArchRule APP_OUTPUT_PORTS_ARE_INTERFACES =
            classes().that().resideInAPackage("..app.port.out..").should().beInterfaces();

    @ArchTest
    static final ArchRule INFRA_ONLY_DEPENDS_ON_ALLOWED_BOUNDARIES = classes()
            .that()
            .resideInAPackage("..infra..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "..infra..",
                    "..app.port.out..",
                    "..domain..",
                    "..sharedkernel..",
                    "java..",
                    "javax..",
                    "jakarta..",
                    "org.jspecify..",
                    "org.slf4j..",
                    "org.springframework..",
                    "org.apache.ibatis..",
                    "com.baomidou..");

    private InfraArchitectureTest() {}
}
