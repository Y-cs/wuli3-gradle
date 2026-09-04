package com.kjs.wuli3.spring.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 暴露统一日志策略配置。
 *
 * @author GuoYang create on 2026/9/3 10:00
 */
@AutoConfiguration
@ConditionalOnClass(name = "ch.qos.logback.classic.LoggerContext")
@ConditionalOnProperty(prefix = LoggingProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {}
