package com.kjs.wuli3.opentelemetry.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import org.junit.jupiter.api.Test;

class MetricRecorderTest {

    @Test
    void recordsAndCachesCounters() {
        final Meter meter = mock(Meter.class);
        final LongCounterBuilder builder = mock(LongCounterBuilder.class);
        final LongCounter counter = mock(LongCounter.class);
        final Attributes attributes =
                Attributes.builder().put("result", "success").build();
        when(meter.counterBuilder("orders.created")).thenReturn(builder);
        when(builder.build()).thenReturn(counter);
        final MetricRecorder recorder = new MetricRecorder(meter);

        recorder.incrementCounter("orders.created");
        recorder.addToCounter("orders.created", 2L, attributes);

        verify(meter, times(1)).counterBuilder("orders.created");
        verify(counter).add(1L, Attributes.empty());
        verify(counter).add(2L, attributes);
    }

    @Test
    void recordsAndCachesHistogramsAndGauges() {
        final Meter meter = mock(Meter.class);
        final DoubleHistogramBuilder histogramBuilder = mock(DoubleHistogramBuilder.class);
        final DoubleHistogram histogram = mock(DoubleHistogram.class);
        final DoubleGaugeBuilder gaugeBuilder = mock(DoubleGaugeBuilder.class);
        final DoubleGauge gauge = mock(DoubleGauge.class);
        final Attributes attributes =
                Attributes.builder().put("operation", "create").build();
        when(meter.histogramBuilder("orders.duration")).thenReturn(histogramBuilder);
        when(histogramBuilder.build()).thenReturn(histogram);
        when(meter.gaugeBuilder("orders.pending")).thenReturn(gaugeBuilder);
        when(gaugeBuilder.build()).thenReturn(gauge);
        final MetricRecorder recorder = new MetricRecorder(meter);

        recorder.recordHistogram("orders.duration", 12.5D, attributes);
        recorder.recordHistogram("orders.duration", 8.5D, attributes);
        recorder.recordGauge("orders.pending", 3D, attributes);
        recorder.recordGauge("orders.pending", 2D, attributes);

        verify(meter, times(1)).histogramBuilder("orders.duration");
        verify(histogram).record(12.5D, attributes);
        verify(histogram).record(8.5D, attributes);
        verify(meter, times(1)).gaugeBuilder("orders.pending");
        verify(gauge).set(3D, attributes);
        verify(gauge).set(2D, attributes);
    }

    @Test
    @SuppressWarnings("NullAway")
    void rejectsInvalidMeasurements() {
        final MetricRecorder recorder = new MetricRecorder(mock(Meter.class));

        assertThatThrownBy(() -> recorder.addToCounter("orders.created", -1L, Attributes.empty()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recorder.incrementCounter(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recorder.incrementCounter("orders.created", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> recorder.recordHistogram("orders.duration", Double.NaN, Attributes.empty()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recorder.recordGauge("orders.pending", Double.POSITIVE_INFINITY, Attributes.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
