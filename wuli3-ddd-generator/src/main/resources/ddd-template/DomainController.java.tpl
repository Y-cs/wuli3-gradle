package {{basePackage}}.adapter.in.web;

import {{basePackage}}.api.{{domainType}}Api;
import {{basePackage}}.api.{{domainType}}StatusView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 将 {{domainType}} 应用契约适配为 HTTP 接口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@RestController
@RequestMapping("/api/{{domain}}")
public final class {{domainType}}Controller {
    private final {{domainType}}Api api;

    public {{domainType}}Controller(final {{domainType}}Api api) {
        this.api = api;
    }

    /** 查询示例聚合状态。 */
    @GetMapping("/status")
    public {{domainType}}StatusView status() {
        return this.api.status();
    }
}
