package {{basePackage}}.sharedkernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * 验证 Shared Kernel 中共享值对象的基本约束。
 *
 * @author GuoYang create on 2026/9/9 12:00
 */
final class SharedKernelTest {
    /** 验证实体标识保留值并拒绝空白值。 */
    @Test
    void validatesEntityId() {
        assertThat(new EntityId("sample-1").value()).isEqualTo("sample-1");

        assertThatThrownBy(() -> new EntityId(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value cannot be blank");
    }
}
