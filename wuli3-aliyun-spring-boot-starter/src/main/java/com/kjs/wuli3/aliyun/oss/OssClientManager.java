package com.kjs.wuli3.aliyun.oss;

import com.kjs.wuli3.aliyun.autoconfigure.AliYunProperties;
import com.kjs.wuli3.aliyun.error.OssErrorCode;
import com.kjs.wuli3.aliyun.profile.OssConfig;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 管理多套命名 OSS 操作模板，负责创建、查找和关闭模板。 */
public final class OssClientManager implements AutoCloseable {

    private final String defaultProfile;
    private final Map<String, OssTemplate> templates;

    private OssClientManager(final String defaultProfile, final Map<String, OssTemplate> templates) {
        this.defaultProfile = defaultProfile;
        this.templates = Collections.unmodifiableMap(templates);
    }

    /** 根据应用配置创建模板管理器，凭据由每套 AccessKeyConfig 独立提供。 */
    public static OssClientManager create(final AliYunProperties properties) {
        final Map<String, OssTemplate> templates = new LinkedHashMap<>();
        properties.getProfiles().forEach((name, profile) -> {
            OssClientManager.validateProfile(name, profile.getOss());
            templates.put(name, new OssTemplate(profile.getAccessKey(), profile.getOss()));
        });
        OssClientManager.validateDefaultProfile(properties.getDefaultProfile(), templates.keySet());
        return new OssClientManager(properties.getDefaultProfile(), templates);
    }

    /** 获取指定配置的 OSS 操作模板。 */
    public OssTemplate get(final String profileName) {
        final OssTemplate template = this.templates.get(profileName);
        if (template == null) {
            throw new ErrorCodeException(
                    OssErrorCode.PROFILE_NOT_FOUND, "AliYun OSS profile not found: " + profileName);
        }
        return template;
    }

    /** 获取默认配置的 OSS 操作模板。 */
    public OssTemplate getDefault() {
        if (this.defaultProfile.isBlank()) {
            throw new ErrorCodeException(OssErrorCode.DEFAULT_PROFILE_MISSING);
        }
        return this.get(this.defaultProfile);
    }

    /** 返回已配置的 profile 名称。 */
    public Set<String> profileNames() {
        return this.templates.keySet();
    }

    /** 关闭管理器创建的全部 OSS 操作模板。 */
    @Override
    public void close() {
        Exception failure = null;
        for (final OssTemplate template : this.templates.values()) {
            try {
                template.close();
            } catch (final Exception exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw new ErrorCodeException(OssErrorCode.CLIENT_CLOSE_FAILED, failure);
        }
    }

    private static void validateProfile(final String name, final OssConfig config) {
        if (name.isBlank()) {
            throw new ErrorCodeException(OssErrorCode.INVALID_PROFILE, "AliYun OSS profile name must not be blank");
        }
        if (config.getRegion().isBlank()) {
            throw new ErrorCodeException(
                    OssErrorCode.INVALID_PROFILE, "AliYun OSS profile '" + name + "' must configure region");
        }
        if (config.getBucket().isBlank()) {
            throw new ErrorCodeException(
                    OssErrorCode.INVALID_PROFILE, "AliYun OSS profile '" + name + "' must configure bucket");
        }
    }

    private static void validateDefaultProfile(final String defaultProfile, final Set<String> profileNames) {
        if (!defaultProfile.isBlank() && !profileNames.contains(defaultProfile)) {
            throw new ErrorCodeException(
                    OssErrorCode.DEFAULT_PROFILE_INVALID, "AliYun OSS default profile not found: " + defaultProfile);
        }
    }
}
