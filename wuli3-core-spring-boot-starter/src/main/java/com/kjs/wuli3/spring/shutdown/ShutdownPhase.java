package com.kjs.wuli3.spring.shutdown;

/**
 * 定义应用关闭时的有序阶段。
 *
 * <p>各阶段按照枚举声明顺序依次执行，确保资源按正确的依赖顺序释放。
 * 每个阶段对应关闭流程中的一个逻辑步骤，可以注册多个钩子在同一阶段执行。
 *
 * <h3>阶段执行顺序与用途</h3>
 * <ol>
 *   <li>{@link #DRAIN_ASYNC_TASKS} - 排空本地异步任务</li>
 *   <li>{@link #AWAIT_REMOTE_ACK} - 等待远程系统确认</li>
 *   <li>{@link #CLOSE_CLIENTS} - 释放由业务钩子管理的客户端资源</li>
 * </ol>
 *
 * <p>普通 Spring Bean 使用 {@link RegisterShutdownHook} 声明阶段；只有运行时才能确定
 * 阶段或优先级时，才直接调用 {@link ShutdownHookRegistry#register}。
 *
 * @author GuoYang create on 2026/9/3 14:55
 * @see ShutdownHookRegistry#register(ShutdownPhase, ShutdownHook, int)
 */
public enum ShutdownPhase {
    /**
     * 排空异步任务。
     *
     * <p>在此阶段应该：
     * <ul>
     *   <li>等待线程池中的任务完成</li>
     *   <li>等待后台定时任务完成</li>
     *   <li>等待异步消息处理完成</li>
     * </ul>
     *
     * <p>应使用带超时的等待，避免无限期阻塞。
     */
    DRAIN_ASYNC_TASKS,

    /**
     * 等待远程系统确认。
     *
     * <p>在此阶段应该：
     * <ul>
     *   <li>等待已发送的消息被确认（ACK）</li>
     *   <li>等待正在进行的 RPC 调用完成</li>
     *   <li>刷新并等待缓冲的写操作完成</li>
     * </ul>
     *
     * <p>确保已完成的工作不会因连接过早关闭而丢失。
     */
    AWAIT_REMOTE_ACK,

    /**
     * 关闭客户端连接。
     *
     * <p>在此阶段应该：
     * <ul>
     *   <li>关闭数据库连接池</li>
     *   <li>关闭 HTTP 客户端</li>
     *   <li>关闭消息队列生产者和消费者</li>
     *   <li>释放其他外部资源</li>
     * </ul>
     *
     * <p>这是关闭流程的最后阶段，应确保所有外部连接都被正确释放。
     */
    CLOSE_CLIENTS
}
