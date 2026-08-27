package com.kjs.wuli3.opentelemetry.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class OpenTelemetryTraceContextAccessorTest {

    private static final String TRACE_ID = "0123456789abcdef0123456789abcdef";
    private static final String SPAN_ID = "0123456789abcdef";

    private final OpenTelemetryTraceContextAccessor accessor = new OpenTelemetryTraceContextAccessor();

    @Test
    void returnsTheCurrentOpenTelemetryIdentifiers() {
        final SpanContext spanContext = SpanContext.create(
                OpenTelemetryTraceContextAccessorTest.TRACE_ID,
                OpenTelemetryTraceContextAccessorTest.SPAN_ID,
                TraceFlags.getSampled(),
                TraceState.getDefault());

        final Scope scope = Span.wrap(spanContext).makeCurrent();
        try {
            assertThat(this.accessor.current())
                    .contains(new TraceContext(
                            OpenTelemetryTraceContextAccessorTest.TRACE_ID,
                            OpenTelemetryTraceContextAccessorTest.SPAN_ID));
            assertThat(this.accessor.traceId()).contains(OpenTelemetryTraceContextAccessorTest.TRACE_ID);
            assertThat(this.accessor.spanId()).contains(OpenTelemetryTraceContextAccessorTest.SPAN_ID);
        } finally {
            scope.close();
        }
    }

    @Test
    void returnsEmptyWithoutAValidCurrentSpan() {
        assertThat(this.accessor.current()).isEmpty();
    }
}
