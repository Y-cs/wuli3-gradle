package com.kjs.wuli3.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.propagation.accessor.RequestContextAccessor;
import com.kjs.wuli3.propagation.accessor.SecurityContextAccessor;
import com.kjs.wuli3.propagation.accessor.TraceContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.resolver.SecurityContextResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = {
                WebAutoConfigurationTest.TestApplication.class,
                WebAutoConfiguration.class,
                WebAutoConfigurationTest.ControllerConfiguration.class,
        }
)
@AutoConfigureMockMvc
class WebAutoConfigurationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestContextAccessor requestContextAccessor;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void requestIdIsPropagated() throws Exception {
        mockMvc.perform(get("/ok").header(RequestId.HEADER_NAME, "rid-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestId.HEADER_NAME, "rid-1"));
    }

    @Test
    void traceIdIsPropagated() throws Exception {
        mockMvc.perform(get("/ok").header(TraceId.HEADER_NAME, "tid-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceId.HEADER_NAME, "tid-1"));
    }

    @Test
    void requestAndTraceIdsAreGenerated() throws Exception {
        mockMvc.perform(get("/ok"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestId.HEADER_NAME))
                .andExpect(header().exists(TraceId.HEADER_NAME));
    }

    @Test
    void contextIsAvailableThroughAccessors() throws Exception {
        mockMvc.perform(get("/context")
                        .header(RequestId.HEADER_NAME, "rid-context")
                        .header(TraceId.HEADER_NAME, "tid-context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("rid-context"))
                .andExpect(jsonPath("$.traceId").value("tid-context"));
    }

    @Test
    void contextIsClearedAfterRequest() throws Exception {
        mockMvc.perform(get("/ok").header(RequestId.HEADER_NAME, "rid-clear"))
                .andExpect(status().isOk());

        assertThat(requestContextAccessor.requestId()).isEmpty();
    }

    @Test
    void customSecurityResolverIsUsed() throws Exception {
        mockMvc.perform(get("/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void exceptionIsMapped() throws Exception {
        mockMvc.perform(get("/boom").header(RequestId.HEADER_NAME, "rid-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM.ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.message").value(SystemErrors.ILLEGAL_ARGUMENT.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-2"));
    }

    @Test
    void codeOnlyExceptionHidesMessage() throws Exception {
        mockMvc.perform(get("/code-only").header(RequestId.HEADER_NAME, "rid-3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM.UNSUPPORTED_OPERATION"))
                .andExpect(jsonPath("$.message").value(WebErrors.INTERNAL_ERROR.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-3"));
    }

    @Test
    void messageOnlyExceptionHidesCode() throws Exception {
        mockMvc.perform(get("/message-only").header(RequestId.HEADER_NAME, "rid-4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("visible message"))
                .andExpect(jsonPath("$.requestId").value("rid-4"));
    }

    @Test
    void internalExceptionHidesCodeAndMessage() throws Exception {
        mockMvc.perform(get("/internal").header(RequestId.HEADER_NAME, "rid-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(WebErrors.INTERNAL_ERROR.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-5"));
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @TestConfiguration
    static class ControllerConfiguration {
        @Bean
        TestController testController(
                RequestContextAccessor requestContextAccessor,
                TraceContextAccessor traceContextAccessor,
                SecurityContextAccessor securityContextAccessor
        ) {
            return new TestController(requestContextAccessor, traceContextAccessor, securityContextAccessor);
        }

        @Bean
        @Primary
        SecurityContextResolver testSecurityContextResolver() {
            return request -> new AuthContext(42L, "alice");
        }
    }

    @RestController
    static class TestController {
        private final RequestContextAccessor requestContextAccessor;
        private final TraceContextAccessor traceContextAccessor;
        private final SecurityContextAccessor securityContextAccessor;

        TestController(
                RequestContextAccessor requestContextAccessor,
                TraceContextAccessor traceContextAccessor,
                SecurityContextAccessor securityContextAccessor
        ) {
            this.requestContextAccessor = requestContextAccessor;
            this.traceContextAccessor = traceContextAccessor;
            this.securityContextAccessor = securityContextAccessor;
        }

        @GetMapping("/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/context")
        ContextView context() {
            return new ContextView(
                    requestContextAccessor.requestId()
                            .orElse(""),
                    traceContextAccessor.traceId()
                            .orElse("")
            );
        }

        @GetMapping("/security")
        SecurityView security() {
            return new SecurityView(
                    securityContextAccessor.userId()
                            .orElse(-1L),
                    securityContextAccessor.username()
                            .orElse("")
            );
        }

        @GetMapping("/boom")
        String boom() {
            throw new ErrorCodeException(SystemErrors.ILLEGAL_ARGUMENT);
        }

        @GetMapping("/code-only")
        String codeOnly() {
            throw new ErrorCodeException(SystemErrors.UNSUPPORTED_OPERATION, "hidden message")
                    .visibility(ErrorVisibility.CODE_ONLY);
        }

        @GetMapping("/message-only")
        String messageOnly() {
            throw new ErrorCodeException(SystemErrors.NOT_IMPLEMENTED, "visible message")
                    .visibility(ErrorVisibility.MESSAGE_ONLY);
        }

        @GetMapping("/internal")
        String internal() {
            throw new ErrorCodeException(SystemErrors.ILLEGAL_STATE, "hidden message")
                    .visibility(ErrorVisibility.INTERNAL);
        }
    }

    record ContextView(String requestId, String traceId) {
    }

    record SecurityView(Long userId, String username) {
    }
}
