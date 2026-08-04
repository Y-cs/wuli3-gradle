package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.PropagationContext;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/** 定义一个传播上下文的协议字段读写契约。 */
public interface ContextFieldEncoder<C extends PropagationContext> {

    /**
     * 返回该编码器处理的上下文类型。
     *
     * @return 上下文类型
     */
    Class<C> contextType();

    /**
     * 返回该编码器管理的协议字段名。
     *
     * @return 不可变字段名集合
     */
    Set<String> fieldNames();

    /**
     * 将上下文写入协议字段。
     *
     * @param context 待编码的上下文
     * @param fieldWriter 字段写入器
     */
    void encode(C context, BiConsumer<String, String> fieldWriter);

    /**
     * 从协议字段读取上下文。
     *
     * @param fieldReader 字段读取器；字段不存在时返回 {@code null}
     * @return 解码结果；字段缺失或内容无效时为空
     */
    Optional<C> decode(Function<String, @Nullable String> fieldReader);
}
