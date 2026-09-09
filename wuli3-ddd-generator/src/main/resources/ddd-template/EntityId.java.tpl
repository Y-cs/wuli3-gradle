package {{basePackage}}.sharedkernel;

import java.util.Objects;

/**
 * 跨领域共享的实体标识值对象。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public record EntityId(String value) {
    /** 校验实体标识不能为空。 */
    public EntityId {
        if (Objects.requireNonNull(value, "value").isBlank()) {
            throw new IllegalArgumentException("value cannot be blank");
        }
    }
}
