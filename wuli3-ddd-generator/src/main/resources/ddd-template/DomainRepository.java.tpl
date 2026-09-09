package {{basePackage}}.app.port.out;

import {{basePackage}}.domain.{{domainPackage}}.{{domainType}};

/**
 * {{domainType}} 持久化输出端口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public interface {{domainType}}Repository {
    /** 加载示例聚合。 */
    {{domainType}} load();
}
