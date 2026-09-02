package com.kjs.wuli3.audit.autoconfigure;

import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.annotation.AuditLog;
import com.kjs.wuli3.audit.internal.AuditLogInterceptor;
import com.kjs.wuli3.audit.internal.DefaultAuditLogRecorder;
import com.kjs.wuli3.core.id.IdGenerator;
import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.core.time.ClockProvider;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.time.Clock;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;

/**
 * 自动配置审计日志客户端侧的记录入口。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration(after = EventAutoConfiguration.class)
@ConditionalOnProperty(prefix = "wuli3.audit-log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogAutoConfiguration {

    @Bean
    @ConditionalOnBean(EventPublisher.class)
    @ConditionalOnMissingBean(AuditLogRecorder.class)
    AuditLogRecorder auditLogRecorder(
            final EventPublisher eventPublisher,
            final ObjectProvider<ContextReader> contextReaders,
            final ObjectProvider<TraceContextAccessor> traceContextAccessors,
            final Environment environment) {
        final String application = environment.getProperty("spring.application.name", "application");
        return new DefaultAuditLogRecorder(
                application,
                eventPublisher,
                contextReaders.getIfAvailable(),
                traceContextAccessors.getIfAvailable());
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(AuditLogRecorder.class)
    @ConditionalOnMissingBean(name = "auditLogAdvisor")
    Advisor auditLogAdvisor(final AuditLogRecorder auditLogRecorder) {
        final AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, AuditLog.class, true);
        final AuditLogInterceptor interceptor = new AuditLogInterceptor(auditLogRecorder);
        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }
}
