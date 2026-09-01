package com.kjs.wuli3.dubbo.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.CommonErrors;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import com.kjs.wuli3.core.error.propagation.ErrorCodePropagator;
import com.kjs.wuli3.dubbo.autoconfigure.DubboProperties;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.junit.jupiter.api.Test;

/**
 * 验证 Dubbo 错误 Filter 的跨服务稳定传播和未知异常兜底规则。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
class DubboErrorFilterTest {
    /** 验证消费方无需提供方业务枚举即可恢复完整错误码和生效策略。 */
    @Test
    void providerAndConsumerTranslateErrorWithoutSharingBusinessEnum() {
        final DubboProperties properties = new DubboProperties();
        final Invocation invocation = mock(Invocation.class);
        final Invoker<?> providerInvoker = mock(Invoker.class);
        when(providerInvoker.getUrl()).thenReturn(URL.valueOf("dubbo://localhost/service?application=group-service"));
        when(providerInvoker.invoke(invocation))
                .thenReturn(new AppResponse(new ErrorCodeException(CommonErrors.ILLEGAL_ARGUMENT, "denied")
                        .withVisibility(ErrorVisibility.CODE_ONLY)));
        final DubboErrorProviderFilter provider = new DubboErrorProviderFilter();
        provider.setDubboProperties(properties);

        final Result wireResult = provider.invoke(providerInvoker, invocation);

        assertThat(wireResult.getException())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Remote service invocation failed");
        assertThat(wireResult.getAttachment(ErrorCodePropagator.CODE))
                .isEqualTo("GROUP-SERVICE.COMMON.ILLEGAL_ARGUMENT");
        assertThat(wireResult.getAttachment(ErrorCodePropagator.MESSAGE)).isEqualTo("Internal server error");
        assertThat(wireResult.getAttachment(ErrorCodePropagator.ORIGIN)).isEqualTo("CALLER");
        assertThat(wireResult.getAttachment(ErrorCodePropagator.SEVERITY)).isEqualTo("NORMAL");
        assertThat(wireResult.getAttachment(ErrorCodePropagator.SOURCE_SERVICE)).isEqualTo("group-service");
        final Invoker<?> consumerInvoker = mock(Invoker.class);
        when(consumerInvoker.invoke(invocation)).thenReturn(wireResult);
        final DubboErrorConsumerFilter consumer = new DubboErrorConsumerFilter();
        consumer.setDubboProperties(properties);

        final Result localResult = consumer.invoke(consumerInvoker, invocation);

        assertThat(localResult.getException()).isInstanceOfSatisfying(ErrorCodeException.class, exception -> {
            assertThat(exception.getErrorCode()).isInstanceOfSatisfying(ErrorCodeCarrier.class, propagated -> {
                assertThat(propagated.code()).isEqualTo("GROUP-SERVICE.COMMON.ILLEGAL_ARGUMENT");
                assertThat(propagated.message()).isEqualTo("Internal server error");
                assertThat(propagated.origin()).isEqualTo(ErrorOrigin.CALLER);
                assertThat(propagated.severity()).isEqualTo(ErrorSeverity.NORMAL);
                assertThat(propagated.sourceService()).isEqualTo("group-service");
            });
            assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
        });
    }

    /** 验证未知 Java 异常在 provider 边界被收敛为不可见的内部系统错误。 */
    @Test
    void providerNormalizesUnknownJavaExceptionAsInternalSystemError() {
        final DubboProperties properties = new DubboProperties();
        final Invocation invocation = mock(Invocation.class);
        final Invoker<?> providerInvoker = mock(Invoker.class);
        when(providerInvoker.getUrl()).thenReturn(URL.valueOf("dubbo://localhost/service?application=group"));
        when(providerInvoker.invoke(invocation))
                .thenReturn(new AppResponse(new IllegalArgumentException("must not cross boundary")));
        final DubboErrorProviderFilter provider = new DubboErrorProviderFilter();
        provider.setDubboProperties(properties);

        final Result wireResult = provider.invoke(providerInvoker, invocation);
        final Invoker<?> consumerInvoker = mock(Invoker.class);
        when(consumerInvoker.invoke(invocation)).thenReturn(wireResult);
        final DubboErrorConsumerFilter consumer = new DubboErrorConsumerFilter();
        consumer.setDubboProperties(properties);

        final Result localResult = consumer.invoke(consumerInvoker, invocation);

        assertThat(localResult.getException()).isInstanceOfSatisfying(ErrorCodeException.class, exception -> {
            assertThat(exception.getErrorCode()).isInstanceOfSatisfying(ErrorCodeCarrier.class, propagated -> {
                assertThat(propagated.code()).isEqualTo("GROUP.SYSTEM.INTERNAL_ERROR");
                assertThat(propagated.message()).isEqualTo("Internal server error");
                assertThat(propagated.origin()).isEqualTo(ErrorOrigin.SERVER);
                assertThat(propagated.severity()).isEqualTo(ErrorSeverity.CRITICAL);
            });
        });
    }

    /** 验证 provider URL 未声明 application 时使用空来源且错误码不添加服务前缀。 */
    @Test
    void providerUsesEmptySourceServiceWhenApplicationIsMissing() {
        final DubboProperties properties = new DubboProperties();
        final Invocation invocation = mock(Invocation.class);
        final Invoker<?> providerInvoker = mock(Invoker.class);
        when(providerInvoker.getUrl()).thenReturn(URL.valueOf("dubbo://localhost/service"));
        when(providerInvoker.invoke(invocation))
                .thenReturn(new AppResponse(new ErrorCodeException(CommonErrors.ILLEGAL_STATE)));
        final DubboErrorProviderFilter provider = new DubboErrorProviderFilter();
        provider.setDubboProperties(properties);

        final Result wireResult = provider.invoke(providerInvoker, invocation);

        assertThat(wireResult.getAttachment(ErrorCodePropagator.CODE)).isEqualTo("COMMON.ILLEGAL_STATE");
        assertThat(wireResult.getAttachment(ErrorCodePropagator.SOURCE_SERVICE)).isEmpty();
    }

    /** 验证同一个 provider Filter 处理不同 application 的 Invoker 时不会混用错误码前缀。 */
    @Test
    void providerKeepsSourceServiceIsolatedAcrossInvokers() {
        final DubboProperties properties = new DubboProperties();
        final Invocation invocation = mock(Invocation.class);
        final Invoker<?> firstInvoker = mock(Invoker.class);
        when(firstInvoker.getUrl()).thenReturn(URL.valueOf("dubbo://localhost/service?application=first-service"));
        when(firstInvoker.invoke(invocation))
                .thenReturn(new AppResponse(new ErrorCodeException(CommonErrors.ILLEGAL_STATE)));
        final Invoker<?> secondInvoker = mock(Invoker.class);
        when(secondInvoker.getUrl()).thenReturn(URL.valueOf("dubbo://localhost/service?application=second-service"));
        when(secondInvoker.invoke(invocation))
                .thenReturn(new AppResponse(new ErrorCodeException(CommonErrors.ILLEGAL_STATE)));
        final DubboErrorProviderFilter provider = new DubboErrorProviderFilter();
        provider.setDubboProperties(properties);

        final Result firstResult = provider.invoke(firstInvoker, invocation);
        final Result secondResult = provider.invoke(secondInvoker, invocation);

        assertThat(firstResult.getAttachment(ErrorCodePropagator.CODE)).isEqualTo("FIRST-SERVICE.COMMON.ILLEGAL_STATE");
        assertThat(firstResult.getAttachment(ErrorCodePropagator.SOURCE_SERVICE))
                .isEqualTo("first-service");
        assertThat(secondResult.getAttachment(ErrorCodePropagator.CODE))
                .isEqualTo("SECOND-SERVICE.COMMON.ILLEGAL_STATE");
        assertThat(secondResult.getAttachment(ErrorCodePropagator.SOURCE_SERVICE))
                .isEqualTo("second-service");
    }

    /** 验证关闭错误传播后 Filter 不修改 Dubbo 原始结果。 */
    @Test
    void disabledErrorPropagationLeavesResultUntouched() {
        final DubboProperties properties = new DubboProperties();
        properties.getError().setEnabled(false);
        final Invocation invocation = mock(Invocation.class);
        final Result original = new AppResponse(new ErrorCodeException(CommonErrors.ILLEGAL_STATE));
        final Invoker<?> invoker = mock(Invoker.class);
        when(invoker.invoke(invocation)).thenReturn(original);
        final DubboErrorProviderFilter filter = new DubboErrorProviderFilter();
        filter.setDubboProperties(properties);

        assertThat(filter.invoke(invoker, invocation)).isSameAs(original);
        assertThat(original.getException()).isInstanceOf(ErrorCodeException.class);
    }
}
