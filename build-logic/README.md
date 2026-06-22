# build-logic

Gradle 构建逻辑模块，不作为业务依赖发布。

提供约定插件：

- `com.kjs.wuli3.java-conventions`：JDK 21、测试、基础依赖约定。
- `com.kjs.wuli3.quality-conventions`：Checkstyle、Forbidden APIs、Error Prone、NullAway。
- `com.kjs.wuli3.spring-conventions`：Spring Boot starter 模块约定。

业务模块只应用约定插件，不重复配置质量规则。
