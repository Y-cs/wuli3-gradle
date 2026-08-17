package com.kjs.wuli3.logging.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** 暴露 Wuli3 日志策略属性；早期默认值由环境后处理器负责。 */
@AutoConfiguration
@ConditionalOnClass(name = "ch.qos.logback.classic.LoggerContext")
@ConditionalOnProperty(prefix = LoggingProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {}
