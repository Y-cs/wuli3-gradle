package com.kjs.wuli3.web.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.web.error.ErrorAlertContext;
import java.lang.reflect.RecordComponent;
import org.junit.jupiter.api.Test;

class WebContextSecurityTest {

    @Test
    void contextTypesOnlyKeepSafeRequestMetadata() {
        assertThat(InvocationContext.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactlyInAnyOrder("originIp", "requestId")
                .doesNotContain("headers", "parameters", "queryString", "requestUrl", "remoteAddr");
        assertThat(AuthContext.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactlyInAnyOrder("userId", "username")
                .doesNotContain("headers", "parameters", "queryString", "requestUrl", "remoteAddr");
        assertThat(ErrorAlertContext.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .doesNotContain("queryString");
    }

    @Test
    void invocationContextToStringOnlyContainsItsSafeFields() {
        final String summary = new InvocationContext("203.0.113.8", "rid-safe").toString();

        assertThat(summary)
                .contains("requestId=rid-safe", "originIp=203.0.113.8")
                .doesNotContain("Authorization", "secret-token", "password", "plain-secret");
    }
}
