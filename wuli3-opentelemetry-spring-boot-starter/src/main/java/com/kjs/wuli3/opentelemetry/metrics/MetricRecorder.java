package com.kjs.wuli3.opentelemetry.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 OpenTelemetry API 记录业务自定义指标。
 *
 * <p>指标由 Java Agent 提供的全局 SDK 汇聚和导出。本类只缓存线程安全的 instrument，不创建 SDK 或 exporter。
 * 指标属性必须保持低基数，禁止使用 traceId、requestId、用户标识等无界值。
 *
 * @author GuoYang create on 2026/8/18 10:00
 */
public final class MetricRecorder {

    /** 本模块创建业务指标时使用的 instrumentation scope。 */
    public static final String INSTRUMENTATION_SCOPE_NAME = "com.kjs.wuli3.business";

    private final Meter meter;
    private final Map<String, LongCounter> counters = new ConcurrentHashMap<>();
    private final Map<String, DoubleHistogram> histograms = new ConcurrentHashMap<>();
    private final Map<String, DoubleGauge> gauges = new ConcurrentHashMap<>();

    /** 创建使用指定 OpenTelemetry Meter 的指标记录器。 */
    public MetricRecorder(final Meter meter) {
        this.meter = Objects.requireNonNull(meter, "meter");
    }

    /** 将指定计数器增加 1。 */
    public void incrementCounter(final String name) {
        this.addToCounter(name, 1L, Attributes.empty());
    }

    /** 将指定计数器增加 1，并关联低基数属性。 */
    public void incrementCounter(final String name, final Attributes attributes) {
        this.addToCounter(name, 1L, attributes);
    }

    /**
     * 增加指定计数器。
     *
     * @throws IllegalArgumentException 当增量为负数时
     */
    public void addToCounter(final String name, final long delta, final Attributes attributes) {
        if (delta < 0L) {
            throw new IllegalArgumentException("counter delta must not be negative");
        }
        final String metricName = MetricRecorder.requireName(name);
        final Attributes actualAttributes = Objects.requireNonNull(attributes, "attributes");
        this.counters
                .computeIfAbsent(
                        metricName, key -> this.meter.counterBuilder(key).build())
                .add(delta, actualAttributes);
    }

    /** 记录一个分布值，例如耗时、大小或批次数量。 */
    public void recordHistogram(final String name, final double value, final Attributes attributes) {
        MetricRecorder.requireFinite(value);
        final String metricName = MetricRecorder.requireName(name);
        final Attributes actualAttributes = Objects.requireNonNull(attributes, "attributes");
        this.histograms
                .computeIfAbsent(
                        metricName, key -> this.meter.histogramBuilder(key).build())
                .record(value, actualAttributes);
    }

    /** 记录一个瞬时值；导出端按 Gauge 的 last-value 聚合处理。 */
    public void recordGauge(final String name, final double value, final Attributes attributes) {
        MetricRecorder.requireFinite(value);
        final String metricName = MetricRecorder.requireName(name);
        final Attributes actualAttributes = Objects.requireNonNull(attributes, "attributes");
        this.gauges
                .computeIfAbsent(metricName, key -> this.meter.gaugeBuilder(key).build())
                .set(value, actualAttributes);
    }

    private static String requireName(final String name) {
        final String metricName = Objects.requireNonNull(name, "name");
        if (metricName.isBlank()) {
            throw new IllegalArgumentException("metric name must not be blank");
        }
        return metricName;
    }

    private static void requireFinite(final double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("metric value must be finite");
        }
    }
}
