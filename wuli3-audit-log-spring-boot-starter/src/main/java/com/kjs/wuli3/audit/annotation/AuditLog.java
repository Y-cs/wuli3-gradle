package com.kjs.wuli3.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法的执行应被自动记录为审计日志。
 *
 * <p>操作主体和调用链信息由框架从当前上下文自动补全，业务代码只需描述"干了什么"。
 *
 * <p>{@code targetId} 和 {@code content} 支持 {@code #{}} 模板语法，可引用方法参数（按名称）、
 * {@code #result}（方法返回值）和 {@code #exception}（捕获的异常）。不含 {@code #{}} 时视为字面量。
 * 表达式采用只读数据绑定上下文，仅支持属性读取；不支持方法调用、类型或 Bean 引用以及赋值。
 *
 * <p>操作结果 {@code outcome} 由是否抛出异常自动推断：正常返回为 {@code SUCCESS}，抛出异常为 {@code FAILURE}。
 * 异常会在记录后原样抛出，不会被吞掉。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 发生操作的业务模块。 */
    String module();

    /** 操作动作名称。 */
    String action();

    /**
     * 被操作对象的业务标识。
     *
     * <p>支持 {@code #{}} 模板语法，例如 {@code "#{#orderId}"} 或 {@code "order-#{#order.id}"}。
     */
    String targetId();

    /**
     * 操作的可读描述。
     *
     * <p>支持 {@code #{}} 模板语法，例如 {@code "创建订单 #{#order.id}，金额 #{#order.amount}"}。
     */
    String content();

    /**
     * 是否延后到当前事务提交后再发布。
     *
     * <p>默认 {@code true}：避免事务回滚后审计事件已发送的不一致。
     */
    boolean afterCommit() default true;

    /**
     * 当方法执行失败（抛出异常）时是否仍记录。
     *
     * <p>默认 {@code true}：失败操作通常也需要审计。设为 {@code false} 可跳过失败记录。
     */
    boolean recordFailure() default true;
}
