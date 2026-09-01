package com.kjs.wuli3.core.error.propagation;

import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 在错误传播协议与字符串字段之间进行协议无关的编码和解码。
 *
 * <p>字段名是跨 HTTP、Dubbo 等适配层共享的稳定传输契约，适配层只负责提供字段读写函数。
 *
 * @author GuoYang create on 2026/8/31 10:00
 */
public final class ErrorCodePropagator {

    /** 错误码字段；值为 {@link ErrorCodeCarrier#code()}。 */
    public static final String CODE = "X-Wuli3-Error-Code";

    /** 错误消息字段；值为 {@link ErrorCodeCarrier#message()}。 */
    public static final String MESSAGE = "X-Wuli3-Error-Message";

    /** 错误责任归属字段；值为 {@link ErrorOrigin} 的枚举名称。 */
    public static final String ORIGIN = "X-Wuli3-Error-Origin";

    /** 错误严重程度字段；值为 {@link ErrorSeverity} 的枚举名称。 */
    public static final String SEVERITY = "X-Wuli3-Error-Severity";

    /** 错误来源服务字段；值为 {@link ErrorCodeCarrier#sourceService()}。 */
    public static final String SOURCE_SERVICE = "X-Wuli3-Error-Source-Service";

    /**
     * 将错误传播协议写入目标协议字段。
     *
     * @param protocol 待编码的错误传播协议
     * @param fieldWriter 目标协议的字段写入函数
     */
    public void inject(final ErrorCodeCarrier protocol, final BiConsumer<String, String> fieldWriter) {
        final ErrorCodeCarrier actualProtocol = Objects.requireNonNull(protocol, "protocol");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        actualFieldWriter.accept(ErrorCodePropagator.CODE, actualProtocol.code());
        actualFieldWriter.accept(ErrorCodePropagator.MESSAGE, actualProtocol.message());
        actualFieldWriter.accept(
                ErrorCodePropagator.ORIGIN, actualProtocol.origin().name());
        actualFieldWriter.accept(
                ErrorCodePropagator.SEVERITY, actualProtocol.severity().name());
        actualFieldWriter.accept(ErrorCodePropagator.SOURCE_SERVICE, actualProtocol.sourceService());
    }

    /**
     * 从来源协议字段读取完整且合法的错误传播协议。
     *
     * <p>错误码、消息、责任归属或严重程度缺失、为空或非法时返回空；来源服务缺失时使用空字符串。
     *
     * @param fieldReader 来源协议的字段读取函数；字段不存在时返回 {@code null}
     * @return 解码后的错误传播协议，字段不完整或非法时为空
     */
    public Optional<ErrorCodeCarrier> extract(final Function<String, @Nullable String> fieldReader) {
        final Function<String, @Nullable String> actualFieldReader = Objects.requireNonNull(fieldReader, "fieldReader");
        final @Nullable String code = actualFieldReader.apply(ErrorCodePropagator.CODE);
        final @Nullable String message = actualFieldReader.apply(ErrorCodePropagator.MESSAGE);
        final @Nullable String originName = actualFieldReader.apply(ErrorCodePropagator.ORIGIN);
        final @Nullable String severityName = actualFieldReader.apply(ErrorCodePropagator.SEVERITY);
        if (code == null || message == null || originName == null || severityName == null) {
            return Optional.empty();
        }
        final @Nullable String sourceService = actualFieldReader.apply(ErrorCodePropagator.SOURCE_SERVICE);
        try {
            return Optional.of(new ErrorCodeCarrier(
                    code,
                    message,
                    ErrorOrigin.valueOf(originName),
                    ErrorSeverity.valueOf(severityName),
                    Objects.requireNonNullElse(sourceService, "")));
        } catch (final IllegalArgumentException invalidValue) {
            return Optional.empty();
        }
    }
}
