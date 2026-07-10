package com.kjs.wuli3.web.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

/**
 * WebJsonResourcePathProperties
 *
 * @author GuoYang create on 2026/7/9 17:11
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.web.json.resource")
public class WebJsonResourcePathProperties {

    private Map<String, String> path = Map.of();

}
