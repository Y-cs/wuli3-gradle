package {{basePackage}}.app.port.in;

import {{basePackage}}.api.{{domainType}}StatusView;

/**
 * {{domainType}} 状态查询用例端口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public interface {{domainType}}UseCase {
    /** 查询示例聚合的状态。 */
    {{domainType}}StatusView status();
}
