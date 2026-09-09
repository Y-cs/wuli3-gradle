package {{basePackage}}.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 验证 Shared Kernel 只依赖 Java 和 Wuli3 Core。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@AnalyzeClasses(packages = "{{basePackage}}.sharedkernel", importOptions = ImportOption.DoNotIncludeTests.class)
final class SharedKernelArchitectureTest {
    @ArchTest
    static final ArchRule SHARED_KERNEL_STAYS_PURE = classes()
            .that()
            .resideInAPackage("..sharedkernel..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage("..sharedkernel..", "java..", "com.kjs.wuli3.core..");

    private SharedKernelArchitectureTest() {}
}
