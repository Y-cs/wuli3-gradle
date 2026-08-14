package com.kjs.wuli3.aliyun.profile;

import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import lombok.Getter;
import lombok.Setter;

/** Aliyun AccessKey 凭据配置，与 OSS 连接参数隔离。 */
@Getter
@Setter
public class AccessKeyConfig {

    private String id = "";
    private String secret = "";

    /** 转换为 OSS SDK 使用的静态凭据 Provider。 */
    public CredentialsProvider toCredentialsProvider() {
        return new StaticCredentialsProvider(this.id, this.secret);
    }
}
