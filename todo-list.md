[] 一个日志模块
[] 鉴权模块
[] 提供统一的json-resource path处理相关内容

Elasticsearch 可以提供：

与 wuli3-json 集成 - 统一 Jackson 配置，保证 ES 文档序列化行为与你的 JSON 标准一致
批量操作工具 - 封装 BulkProcessor，提供更简洁的批量写入 API
搜索日志记录（可选）- 记录慢查询、大结果集查询，便于排查性能问题
测试支持 - 提供 Testcontainers 或嵌入式 ES 的自动配置

MongoDB 可以提供：

审计字段自动填充 - 集成 wuli3-context-propagation，自动填充创建人、更新人、时间戳
逻辑删除支持 - 类似 MyBatis-Plus 的逻辑删除
与 wuli3-json 集成 - 统一序列化行为
测试支持 - 嵌入式 MongoDB 配置