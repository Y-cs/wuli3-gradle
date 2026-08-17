package com.kjs.wuli3.aliyun.autoconfigure;

import com.kjs.wuli3.aliyun.profile.AccessKeyConfig;
import com.kjs.wuli3.aliyun.profile.OssConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AliYun OSS SDK V2 的多套命名配置。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.aliyun")
public class AliYunProperties {

    /** 默认配置名称；为空时使用方必须显式指定配置名称。 */
    private String defaultProfile = "";

    /** 按名称区分的 OSS 配置集合，例如 default、archive。 */
    private Map<String, OssProfile> profiles = new LinkedHashMap<>();

    /** 一套 OSS Bucket 及其客户端连接配置。 */
    @Getter
    @Setter
    public static class OssProfile {
        /** 访问凭据。 */
        private AccessKeyConfig accessKey = new AccessKeyConfig();

        /** OSS 连接参数。 */
        private OssConfig oss = new OssConfig();
    }
}
