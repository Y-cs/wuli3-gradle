package {{basePackage}}.infra.persistence;

import {{basePackage}}.domain.{{domainPackage}}.{{domainType}};
import {{basePackage}}.sharedkernel.EntityId;

/**
 * {{domainType}} 的数据库持久化对象示例。
 *
 * @param id 数据库主键
 * @author GuoYang create on 2026/9/7 15:30
 */
public record {{domainType}}Po(String id) {
    /** 转换为领域聚合。 */
    public {{domainType}} toDomain() {
        return new {{domainType}}(new EntityId(this.id));
    }
}
