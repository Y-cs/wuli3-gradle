package {{basePackage}}.app.internal;

import {{basePackage}}.api.{{domainType}}Api;
import {{basePackage}}.api.{{domainType}}StatusView;
import {{basePackage}}.app.port.in.{{domainType}}UseCase;
import {{basePackage}}.app.port.out.{{domainType}}Repository;
import org.springframework.stereotype.Service;

/**
 * 编排 {{domainType}} 状态查询用例。
 *
 * 注意：实现保持包可见，基础设施模块只能依赖公开的输出端口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@Service
final class {{domainType}}ApplicationService implements {{domainType}}Api, {{domainType}}UseCase {
    private final {{domainType}}Repository repository;

    {{domainType}}ApplicationService(final {{domainType}}Repository repository) {
        this.repository = repository;
    }

    /** 查询示例聚合并转换为对外视图。 */
    @Override
    public {{domainType}}StatusView status() {
        final var aggregate = this.repository.load();
        return new {{domainType}}StatusView(aggregate.id(), aggregate.status());
    }
}
