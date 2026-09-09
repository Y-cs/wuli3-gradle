package {{basePackage}}.api;

/**
 * {{domainType}} 业务能力的稳定对外契约。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public interface {{domainType}}Api {
    /** 查询示例聚合的状态。 */
    {{domainType}}StatusView status();
}
