package {{basePackage}}.infra.persistence;

import {{basePackage}}.app.port.out.{{domainType}}Repository;
import {{basePackage}}.domain.{{domainPackage}}.{{domainType}};
import {{basePackage}}.sharedkernel.EntityId;
import org.springframework.stereotype.Repository;

/**
 * {{domainType}} 持久化输出端口的基础设施实现。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@Repository
public final class {{domainType}}RepositoryAdapter implements {{domainType}}Repository {
{{repositoryFields}}{{repositoryConstructor}}
    /** 加载示例聚合。 */
    @Override
    public {{domainType}} load() {
{{repositoryLoad}}
    }
}
