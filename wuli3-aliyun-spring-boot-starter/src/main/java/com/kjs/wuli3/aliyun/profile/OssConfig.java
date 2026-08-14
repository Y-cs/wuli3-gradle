package com.kjs.wuli3.aliyun.profile;

import lombok.Getter;
import lombok.Setter;

/** OSS 连接和默认 Bucket 配置，与访问凭据隔离。 */
@Getter
@Setter
public class OssConfig {

    private String endpoint = "";
    private String region = "";
    private String bucket = "";
    private boolean async;
}
