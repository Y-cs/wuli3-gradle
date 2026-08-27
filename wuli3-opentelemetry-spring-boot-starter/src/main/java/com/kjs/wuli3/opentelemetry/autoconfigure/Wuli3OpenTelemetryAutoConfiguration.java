package com.kjs.wuli3.opentelemetry.autoconfigure;

import com.kjs.wuli3.opentelemetry.internal.OpenTelemetryTraceContextAccessor;
import com.kjs.wuli3.opentelemetry.metrics.MetricRecorder;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 暴露 Java Agent 所提供 OpenTelemetry 全局实例的 Trace 上下文和手工指标记录能力。
 *
 * <p>SDK、采样、传播和导出全部由 Java Agent 管理，本自动配置不会创建第二套遥测管线。没有安装 Agent 时，
 * OpenTelemetry API 自动退化为无操作实现。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration
@ConditionalOnClass(OpenTelemetry.class)
public class Wuli3OpenTelemetryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TraceContextAccessor.class)
    TraceContextAccessor traceContextAccessor() {
        return new OpenTelemetryTraceContextAccessor();
    }

    @Bean
    @ConditionalOnMissingBean(MetricRecorder.class)
    MetricRecorder metricRecorder() {
        return new MetricRecorder(GlobalOpenTelemetry.get().getMeter(MetricRecorder.INSTRUMENTATION_SCOPE_NAME));
    }
}
