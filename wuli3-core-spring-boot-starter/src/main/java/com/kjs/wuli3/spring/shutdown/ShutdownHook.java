package com.kjs.wuli3.spring.shutdown;

/**
 * 执行一项有时间边界的优雅关闭工作。
 *
 * <p>关闭钩子在应用停止过程中被调用，用于释放资源、完成正在进行的工作、
 * 或通知外部系统。钩子必须在给定的时间预算内完成，并应该响应线程中断。
 *
 * <h3>实现指南</h3>
 * <ul>
 *   <li>尊重 {@link ShutdownContext#remaining()} 提供的剩余时间</li>
 *   <li>响应线程中断信号 ({@link Thread#isInterrupted()})</li>
 *   <li>避免长时间阻塞操作，或使用带超时的等待</li>
 *   <li>抛出的异常不会传播到其他钩子，但会被记录</li>
 *   <li>钩子应该是幂等的，多次调用不应产生副作用</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class ConnectionPoolShutdownHook implements ShutdownHook {
 *     private final ConnectionPool pool;
 *
 *     @Override
 *     public void shutdown(ShutdownContext context) throws InterruptedException {
 *         // 等待活跃连接完成，但不超过剩余时间
 *         pool.awaitTermination(context.remaining().toMillis(), TimeUnit.MILLISECONDS);
 *     }
 *
 *     @Override
 *     public String name() {
 *         return "connection-pool";
 *     }
 * }
 * }</pre>
 *
 * @author GuoYang create on 2026/9/3 14:55
 * @see ShutdownContext
 * @see ShutdownHookRegistry#register(ShutdownPhase, ShutdownHook, int)
 */
@FunctionalInterface
public interface ShutdownHook {

    /**
     * 在上下文提供的时间预算内执行关闭工作。
     *
     * <p>实现应该定期检查 {@link ShutdownContext#remaining()} 或响应线程中断，
     * 以便在超时时能够快速退出。
     *
     * @param context 提供超时边界和开始时间的上下文
     * @throws InterruptedException 如果线程在等待时被中断
     */
    void shutdown(ShutdownContext context) throws InterruptedException;

    /**
     * 返回用于关闭日志和指标的稳定名称。
     *
     * <p>默认使用类的简单名称。建议覆盖此方法以提供更具描述性的名称，
     * 特别是在使用匿名类或 lambda 表达式时。
     *
     * @return 钩子的稳定标识符，用于日志和指标
     */
    default String name() {
        return this.getClass().getSimpleName();
    }
}
