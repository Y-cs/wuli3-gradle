package com.kjs.wuli3.web.autoconfigure;

import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.codec.InvocationContextCodec;
import com.kjs.wuli3.propagation.codec.PropagationContextCodec;
import com.kjs.wuli3.propagation.context.PropagationContext;
import com.kjs.wuli3.propagation.snapshot.ContextPropagator;
import com.kjs.wuli3.propagation.snapshot.DefaultContextPropagator;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.propagation.transmission.ContextTransmitter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.WebContextAccessor;
import com.kjs.wuli3.web.internal.context.DefaultAuthContextResolver;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ContextPropagationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ContextStore contextStore() {
        return new ContextStore();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextPropagator contextPropagator(final ContextWriter contextWriter) {
        return new DefaultContextPropagator(contextWriter);
    }

    @Bean
    @ConditionalOnMissingBean
    InvocationContextCodec invocationContextCodec() {
        return new InvocationContextCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextTransmitter contextTransmitter(
            final ContextReader contextReader,
            final ContextWriter contextWriter,
            final ObjectProvider<PropagationContextCodec<? extends PropagationContext>> codecs) {
        final List<PropagationContextCodec<? extends PropagationContext>> orderedCodecs =
                codecs.orderedStream().toList();
        return new ContextTransmitter(contextReader, contextWriter, orderedCodecs);
    }

    @Bean
    @ConditionalOnMissingBean
    InvocationContextAccessor invocationContextAccessor(final ContextReader contextReader) {
        return new InvocationContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    AuthContextAccessor authContextAccessor(final ContextReader contextReader) {
        return new AuthContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    WebContextAccessor webContextAccessor(final ContextReader contextReader) {
        return new WebContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    AuthContextResolver securityContextResolver() {
        return new DefaultAuthContextResolver();
    }
}
