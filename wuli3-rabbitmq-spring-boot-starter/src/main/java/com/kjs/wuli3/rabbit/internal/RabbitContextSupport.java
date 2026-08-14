package com.kjs.wuli3.rabbit.internal;

import com.kjs.wuli3.propagation.ContextPropagator;
import com.kjs.wuli3.propagation.ContextScope;
import com.kjs.wuli3.propagation.encoding.ContextEncoder;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import com.kjs.wuli3.propagation.store.ContextWriter;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/** 从 RabbitMQ 消息 headers 解码传播上下文，由消费适配器显式恢复。 */
public final class RabbitContextSupport {

    private final ContextWriter contextWriter;
    private final ContextEncoder contextEncoder;

    /**
     * 创建 RabbitMQ 上下文支持实例。
     *
     * @param contextWriter  上下文写入器
     * @param contextEncoder 上下文字段编码器
     * @throws NullPointerException 当任一参数为 {@code null} 时
     */
    public RabbitContextSupport(final ContextWriter contextWriter, final ContextEncoder contextEncoder) {
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
        this.contextEncoder = Objects.requireNonNull(contextEncoder, "contextEncoder");
    }

    /**
     * 从消息 headers 解码传播上下文。
     *
     * <p>调用返回传播器的 {@link ContextPropagator#restore(ContextSnapshot)} 后，关闭作用域会恢复进入
     * 作用域前的完整上下文。
     *
     * <p>典型用法：
     * <pre>{@code
     * final ContextPropagator propagator = rabbitContextSupport.restoreFrom(message.getMessageProperties().getHeaders());
     * try (ContextScope ignored = propagator.restore(propagator.capture())) {
     *     handleMessage(message);
     * }
     * }</pre>
     *
     * @param headers 消息 headers（来自 {@code MessageProperties.getHeaders()}）
     * @return 持有解码快照的传播器
     * @throws NullPointerException 当 {@code headers} 为 {@code null} 时
     */
    @SuppressWarnings("NullAway")
    public RabbitContextPropagator restoreFrom(final Map<String, ?> headers) {
        final Map<String, ?> actualHeaders = Objects.requireNonNull(headers, "headers");
        final Function<String, @Nullable String> fieldReader = key -> {
            final Object value = actualHeaders.get(key);
            return value == null ? null : value.toString();
        };
        final ContextSnapshot contextSnapshot = this.contextEncoder.readFrom(fieldReader);
        return new RabbitContextPropagator(this.contextWriter, contextSnapshot);
    }

    @RequiredArgsConstructor
    public static final class RabbitContextPropagator implements ContextPropagator {

        private final ContextWriter contextWriter;
        private final ContextSnapshot contextSnapshot;

        @Override
        public ContextSnapshot capture() {
            return this.contextSnapshot;
        }

        @Override
        public ContextScope restore(final ContextSnapshot snapshot) {
            return this.contextWriter.restore(snapshot);
        }
    }
}
