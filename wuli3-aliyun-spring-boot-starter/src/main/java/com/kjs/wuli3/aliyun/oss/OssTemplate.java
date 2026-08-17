package com.kjs.wuli3.aliyun.oss;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.kjs.wuli3.aliyun.error.OssOperateException;
import com.kjs.wuli3.aliyun.profile.AccessKeyConfig;
import com.kjs.wuli3.aliyun.profile.OssConfig;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 面向单个 OSS 客户端的操作模板，统一转换 SDK 操作异常。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class OssTemplate implements AutoCloseable {

    private final OSSClient client;

    public OssTemplate(final AccessKeyConfig accessKeyConfig, final OssConfig ossConfig) {
        final OSSClientBuilder builder = OSSClient.newBuilder()
                .credentialsProvider(accessKeyConfig.toCredentialsProvider())
                .region(ossConfig.getRegion());
        if (!ossConfig.getEndpoint().isBlank()) {
            builder.endpoint(ossConfig.getEndpoint());
        }
        this.client = builder.build();
    }

    public OssTemplate(final OSSClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    /** 执行无返回值的 OSS 操作。 */
    public void execute(final Consumer<OSSClient> action) {
        try {
            action.accept(this.client);
        } catch (final Exception exception) {
            throw new OssOperateException(exception);
        }
    }

    /** 执行并返回结果的 OSS 操作。 */
    public <T> T executeWithResult(final Function<OSSClient, T> action) {
        try {
            return action.apply(this.client);
        } catch (final Exception exception) {
            throw new OssOperateException(exception);
        }
    }

    @Override
    public void close() {
        try {
            this.client.close();
        } catch (final Exception exception) {
            throw new OssOperateException(exception);
        }
    }
}
