package {{basePackage}};

{{bootstrapTestImports}}

/**
 * 验证所有业务模块可以被 Bootstrap 完整组装。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@SpringBootTest{{bootstrapTestConfiguration}}
final class BootstrapApplicationTest {
    /** 验证 Spring 应用上下文可以启动。 */
    @Test
    void contextLoads() {}
}
