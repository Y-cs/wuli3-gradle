package com.kjs.wuli3.audit.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.audit.AuditLogEntry;
import com.kjs.wuli3.audit.AuditLogOutcome;
import com.kjs.wuli3.audit.AuditLogReceipt;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.annotation.AuditLog;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

class AuditLogInterceptorTest {

    @Test
    void resolvesSpelTemplatesFromMethodArgumentsAndResult() {
        final RecordingRecorder recorder = new RecordingRecorder();
        final OrderService service = AuditLogInterceptorTest.proxy(new DefaultOrderService(), recorder);

        service.create("order-7", 99);

        assertThat(recorder.entries).singleElement().satisfies(entry -> {
            assertThat(entry.module()).isEqualTo("ORDER");
            assertThat(entry.action()).isEqualTo("CREATE");
            assertThat(entry.targetId()).isEqualTo("order-7");
            assertThat(entry.content()).isEqualTo("创建订单 order-7 金额 99，结果 created:order-7");
            assertThat(entry.outcome()).isEqualTo(AuditLogOutcome.SUCCESS);
        });
        assertThat(recorder.afterCommitFlags).containsExactly(true);
    }

    @Test
    void keepsLiteralAttributesUntouched() {
        final RecordingRecorder recorder = new RecordingRecorder();
        final OrderService service = AuditLogInterceptorTest.proxy(new DefaultOrderService(), recorder);

        service.archive();

        assertThat(recorder.entries).singleElement().satisfies(entry -> {
            assertThat(entry.targetId()).isEqualTo("all-orders");
            assertThat(entry.content()).isEqualTo("归档全部订单");
        });
        assertThat(recorder.afterCommitFlags).containsExactly(false);
    }

    @Test
    void recordsFailureAndRethrowsTheOriginalException() {
        final RecordingRecorder recorder = new RecordingRecorder();
        final OrderService service = AuditLogInterceptorTest.proxy(new DefaultOrderService(), recorder);

        assertThatThrownBy(() -> service.cancel("order-9"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("order is locked");

        assertThat(recorder.entries).singleElement().satisfies(entry -> {
            assertThat(entry.outcome()).isEqualTo(AuditLogOutcome.FAILURE);
            assertThat(entry.targetId()).isEqualTo("order-9");
            assertThat(entry.content()).isEqualTo("取消失败：order is locked");
        });
    }

    @Test
    void skipsFailureRecordingWhenDisabled() {
        final RecordingRecorder recorder = new RecordingRecorder();
        final OrderService service = AuditLogInterceptorTest.proxy(new DefaultOrderService(), recorder);

        assertThatThrownBy(() -> service.purge()).isInstanceOf(IllegalStateException.class);

        assertThat(recorder.entries).isEmpty();
    }

    @Test
    void leavesUnannotatedMethodsAlone() {
        final RecordingRecorder recorder = new RecordingRecorder();
        final OrderService service = AuditLogInterceptorTest.proxy(new DefaultOrderService(), recorder);

        assertThat(service.find("order-1")).isEqualTo("order-1");
        assertThat(recorder.entries).isEmpty();
    }

    private static OrderService proxy(final OrderService target, final AuditLogRecorder recorder) {
        final ProxyFactory factory = new ProxyFactory(target);
        factory.addAdvisor(new DefaultPointcutAdvisor(
                new AnnotationMatchingPointcut(null, AuditLog.class, true), new AuditLogInterceptor(recorder)));
        return (OrderService) factory.getProxy();
    }

    interface OrderService {

        String create(String orderId, int amount);

        void archive();

        void cancel(String orderId);

        void purge();

        String find(String orderId);
    }

    static class DefaultOrderService implements OrderService {

        @Override
        @AuditLog(
                module = "ORDER",
                action = "CREATE",
                targetId = "#{#orderId}",
                content = "创建订单 #{#orderId} 金额 #{#amount}，结果 #{#result}")
        public String create(final String orderId, final int amount) {
            return "created:" + orderId;
        }

        @Override
        @AuditLog(
                module = "ORDER",
                action = "ARCHIVE",
                targetId = "all-orders",
                content = "归档全部订单",
                afterCommit = false)
        public void archive() {
            // 无需返回值的审计场景
        }

        @Override
        @AuditLog(module = "ORDER", action = "CANCEL", targetId = "#{#orderId}", content = "取消失败：#{#exception.message}")
        public void cancel(final String orderId) {
            throw new IllegalStateException("order is locked");
        }

        @Override
        @AuditLog(module = "ORDER", action = "PURGE", targetId = "all-orders", content = "清理订单", recordFailure = false)
        public void purge() {
            throw new IllegalStateException("not allowed");
        }

        @Override
        public String find(final String orderId) {
            return orderId;
        }
    }

    private static final class RecordingRecorder implements AuditLogRecorder {

        private final List<AuditLogEntry> entries = new ArrayList<>();
        private final List<Boolean> afterCommitFlags = new ArrayList<>();

        @Override
        public AuditLogReceipt record(final AuditLogEntry entry) {
            return this.record(entry, true);
        }

        @Override
        public AuditLogReceipt record(final AuditLogEntry entry, final boolean afterCommit) {
            this.entries.add(entry);
            this.afterCommitFlags.add(afterCommit);
            return new AuditLogReceipt("event-" + this.entries.size(), Instant.EPOCH);
        }
    }
}
