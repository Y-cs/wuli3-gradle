package com.kjs.wuli3.web.json;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Web 资源路径类型与域名映射配置。
 *
 * @author GuoYang create on 2026/7/9 17:11
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.web.json.resource")
@Validated
public class WebJsonResourcePathProperties {

    @NotNull
    private Map<@NotBlank String, @NotBlank String> path = Map.of();
}
