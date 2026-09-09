package {{basePackage}}.domain.{{domainPackage}};

import static org.assertj.core.api.Assertions.assertThat;

import {{basePackage}}.sharedkernel.EntityId;
import {{basePackage}}.sharedkernel.Status;
import org.junit.jupiter.api.Test;

/**
 * 验证 {{domainType}} 聚合的核心状态规则。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
final class {{domainType}}Test {
    /** 验证新聚合默认启用且可以停用。 */
    @Test
    void deactivatesAggregate() {
        final {{domainType}} aggregate = new {{domainType}}(new EntityId("sample-1"));

        aggregate.deactivate();

        assertThat(aggregate.status()).isEqualTo(Status.INACTIVE);
    }
}
