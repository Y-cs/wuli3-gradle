package com.kjs.wuli3.aliyun.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.aliyun.oss.OssClientManager;
import com.kjs.wuli3.core.error.ErrorCodeException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AliYunAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AliYunAutoConfiguration.class));

    @Test
    void registersEmptyPropertiesAndManagerByDefault() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AliYunProperties.class);
            assertThat(context).hasSingleBean(OssClientManager.class);
            assertThat(context.getBean(AliYunProperties.class).getProfiles()).isEmpty();
            assertThat(context.getBean(OssClientManager.class).profileNames()).isEmpty();
        });
    }

    @Test
    void createsMultipleNamedOssClients() {
        this.contextRunner
                .withPropertyValues(
                        "wuli3.aliyun.default-profile=default",
                        "wuli3.aliyun.profiles.default.oss.region=cn-hangzhou",
                        "wuli3.aliyun.profiles.default.oss.bucket=example-default",
                        "wuli3.aliyun.profiles.archive.oss.endpoint=https://oss-cn-shanghai.aliyuncs.com",
                        "wuli3.aliyun.profiles.archive.oss.region=cn-shanghai",
                        "wuli3.aliyun.profiles.archive.oss.bucket=example-archive")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    final OssClientManager manager = context.getBean(OssClientManager.class);
                    assertThat(manager.profileNames()).containsExactly("default", "archive");
                    assertThat(manager.get("default")).isNotNull();
                    assertThat(manager.get("archive")).isNotNull();
                    assertThat(manager.getDefault()).isSameAs(manager.get("default"));
                });
    }

    @Test
    void rejectsProfileWithoutRequiredRegion() {
        this.contextRunner
                .withPropertyValues("wuli3.aliyun.profiles.default.oss.bucket=example-default")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasRootCauseInstanceOf(ErrorCodeException.class)
                        .hasRootCauseMessage("AliYun OSS profile 'default' must configure region"));
    }

    @Test
    void rejectsUnknownDefaultProfile() {
        this.contextRunner
                .withPropertyValues(
                        "wuli3.aliyun.default-profile=missing",
                        "wuli3.aliyun.profiles.default.oss.region=cn-hangzhou",
                        "wuli3.aliyun.profiles.default.oss.bucket=example-default")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasRootCauseInstanceOf(ErrorCodeException.class)
                        .hasRootCauseMessage("AliYun OSS default profile not found: missing"));
    }

    @Test
    void backsOffForApplicationProvidedManager() {
        final AliYunProperties properties = new AliYunProperties();
        final OssClientManager manager = OssClientManager.create(properties);
        this.contextRunner
                .withBean(OssClientManager.class, () -> manager)
                .run(context ->
                        assertThat(context.getBean(OssClientManager.class)).isSameAs(manager));
    }
}
