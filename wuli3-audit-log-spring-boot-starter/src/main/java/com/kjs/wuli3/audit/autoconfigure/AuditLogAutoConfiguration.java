package com.kjs.wuli3.audit.autoconfigure;

import com.kjs.wuli3.audit.AuditLogQueryClient;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.internal.DefaultAuditLogQueryClient;
import com.kjs.wuli3.audit.internal.DefaultAuditLogRecorder;
import com.kjs.wuli3.audit.internal.StoreBackedAuditLogTransport;
import com.kjs.wuli3.audit.store.AuditLogStore;
import com.kjs.wuli3.audit.transport.AuditLogQueryTransport;
import com.kjs.wuli3.audit.transport.AuditLogWriteTransport;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 自动配置审计日志记录、查询和独立服务本地存储适配。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "wuli3.audit-log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogAutoConfiguration {

    @Bean
    @ConditionalOnBean(AuditLogStore.class)
    @ConditionalOnMissingBean(AuditLogWriteTransport.class)
    StoreBackedAuditLogTransport storeBackedAuditLogWriteTransport(final AuditLogStore auditLogStore) {
        return new StoreBackedAuditLogTransport(auditLogStore);
    }

    @Bean
    @ConditionalOnBean(AuditLogStore.class)
    @ConditionalOnMissingBean(AuditLogQueryTransport.class)
    AuditLogQueryTransport storeBackedAuditLogQueryTransport(final AuditLogStore auditLogStore) {
        return new StoreBackedAuditLogTransport(auditLogStore);
    }

    @Bean
    @ConditionalOnBean(AuditLogWriteTransport.class)
    @ConditionalOnMissingBean(AuditLogRecorder.class)
    AuditLogRecorder auditLogRecorder(
            final AuditLogWriteTransport writeTransport,
            final ObjectProvider<ContextReader> contextReaders,
            final ObjectProvider<TraceContextAccessor> traceContextAccessors,
            final ObjectProvider<Clock> clocks,
            final Environment environment) {
        final String application = environment.getProperty("spring.application.name", "application");
        final Clock clock = clocks.getIfAvailable(Clock::systemUTC);
        return new DefaultAuditLogRecorder(
                application,
                writeTransport,
                contextReaders.getIfAvailable(),
                traceContextAccessors.getIfAvailable(),
                clock,
                () -> UUID.randomUUID().toString());
    }

    @Bean
    @ConditionalOnBean(AuditLogQueryTransport.class)
    @ConditionalOnMissingBean(AuditLogQueryClient.class)
    AuditLogQueryClient auditLogQueryClient(final AuditLogQueryTransport queryTransport) {
        return new DefaultAuditLogQueryClient(queryTransport);
    }
}
