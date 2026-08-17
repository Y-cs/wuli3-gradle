package com.kjs.wuli3.propagation;

/** 表示已恢复上下文的作用域，关闭时恢复进入作用域前的上下文。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface ContextScope extends AutoCloseable {

    /** 结束当前恢复作用域，并恢复进入作用域前的上下文。 */
    @Override
    void close();
}
