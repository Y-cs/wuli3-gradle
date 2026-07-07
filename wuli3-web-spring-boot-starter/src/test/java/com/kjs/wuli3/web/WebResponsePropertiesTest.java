package com.kjs.wuli3.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.autoconfigure.WebAutoConfiguration;
import com.kjs.wuli3.web.context.RequestIds;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

class WebResponsePropertiesTest {

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.response.wrapper-enabled=false")
    @AutoConfigureMockMvc
    @Nested
    class WrapperDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void wrapperCanBeDisabled() throws Exception {
            mockMvc.perform(get("/ok"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("ok"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.response.wrap-response-entity-body=false")
    @AutoConfigureMockMvc
    @Nested
    class ResponseEntityBodyDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void responseEntityBodyWrappingCanBeDisabled() throws Exception {
            mockMvc.perform(get("/entity"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("X-Custom", "yes"))
                    .andExpect(jsonPath("$.requestId").value("entity"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.response.success-message=success")
    @AutoConfigureMockMvc
    @Nested
    class SuccessMessageTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void successMessageCanBeConfigured() throws Exception {
            mockMvc.perform(get("/context").header(RequestIds.HEADER_NAME, "rid-message"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("success"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.response.exception-handler-enabled=false")
    @AutoConfigureMockMvc
    @Nested
    class ExceptionHandlerDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void exceptionHandlerCanBeDisabled() throws Exception {
            assertThatThrownBy(() -> mockMvc.perform(get("/boom"))).hasCauseInstanceOf(ErrorCodeException.class);
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.context.request-id-max-length=5")
    @AutoConfigureMockMvc
    @Nested
    class RequestIdValidationTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void invalidRequestIdIsRegeneratedByDefault() throws Exception {
            mockMvc.perform(get("/context").header(RequestIds.HEADER_NAME, "too-long-request-id"))
                    .andExpect(status().isOk())
                    .andExpect(result -> assertThat(result.getResponse().getHeader(RequestIds.HEADER_NAME))
                            .isNotEqualTo("too-long-request-id"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.context.accept-external-request-id=false")
    @AutoConfigureMockMvc
    @Nested
    class ExternalRequestIdDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void externalRequestIdCanBeIgnored() throws Exception {
            mockMvc.perform(get("/context").header(RequestIds.HEADER_NAME, "rid-external"))
                    .andExpect(status().isOk())
                    .andExpect(result -> assertThat(result.getResponse().getHeader(RequestIds.HEADER_NAME))
                            .isNotEqualTo("rid-external"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.context.trusted-proxy-enabled=true")
    @AutoConfigureMockMvc
    @Nested
    class TrustedProxyEnabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void trustedProxyHeaderIsUsedWhenEnabled() throws Exception {
            mockMvc.perform(get("/origin-ip")
                            .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                            .with(request -> {
                                request.setRemoteAddr("10.0.0.1");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("203.0.113.10"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            })
    @AutoConfigureMockMvc
    @Nested
    class TrustedProxyDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void forwardedHeaderIsIgnoredByDefault() throws Exception {
            mockMvc.perform(get("/origin-ip")
                            .header("X-Forwarded-For", "203.0.113.10")
                            .with(request -> {
                                request.setRemoteAddr("10.0.0.1");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("10.0.0.1"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.context.request-body-cache-enabled=false")
    @AutoConfigureMockMvc
    @Nested
    class BodyCacheDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void bodyCacheCanBeDisabled() throws Exception {
            mockMvc.perform(post("/body").contentType(MediaType.TEXT_PLAIN).content("payload"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.first").value("payload"))
                    .andExpect(jsonPath("$.data.second").value(""));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.context.max-cache-body-size=4B")
    @AutoConfigureMockMvc
    @Nested
    class BodyCacheLimitTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void bodyCacheLimitReturnsPayloadTooLarge() throws Exception {
            mockMvc.perform(post("/body").contentType(MediaType.TEXT_PLAIN).content("payload"))
                    .andExpect(status().isPayloadTooLarge())
                    .andExpect(jsonPath("$.code").value("WEB.PAYLOAD_TOO_LARGE"));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
            },
            properties = "wuli3.web.context.enabled=false")
    @AutoConfigureMockMvc
    @Nested
    class ContextDisabledTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void contextFilterCanBeDisabled() throws Exception {
            mockMvc.perform(get("/context").header(RequestIds.HEADER_NAME, "rid-disabled"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(RequestIds.HEADER_NAME))
                    .andExpect(jsonPath("$.requestId").doesNotExist())
                    .andExpect(jsonPath("$.data.requestId").value(""));
        }
    }

    @SpringBootTest(
            webEnvironment = MOCK,
            classes = {
                TestApplication.class,
                WebAutoConfiguration.class,
                ControllerConfiguration.class,
                CustomStatusResolverConfiguration.class,
            })
    @AutoConfigureMockMvc
    @Nested
    class CustomStatusResolverTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void customStatusResolverCanOverrideDefaultStatus() throws Exception {
            mockMvc.perform(get("/boom"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("SYSTEM.ILLEGAL_ARGUMENT"));
        }
    }

    @TestConfiguration
    static class CustomStatusResolverConfiguration {
        @Bean
        @Primary
        WebErrorStatusResolver customWebErrorStatusResolver() {
            return (error, responseCode) -> HttpStatus.CONFLICT;
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {}

    @TestConfiguration
    static class ControllerConfiguration {
        @Bean
        TestController testController(InvocationContextAccessor invocationContextAccessor) {
            return new TestController(invocationContextAccessor);
        }

        @Bean
        @Primary
        AuthContextResolver testSecurityContextResolver() {
            return request -> new AuthContext(1L, "tester");
        }
    }

    @RestController
    static class TestController {
        private final InvocationContextAccessor invocationContextAccessor;

        TestController(InvocationContextAccessor invocationContextAccessor) {
            this.invocationContextAccessor = invocationContextAccessor;
        }

        @GetMapping("/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/context")
        ContextView context() {
            return new ContextView(invocationContextAccessor.requestId().orElse(""));
        }

        @GetMapping("/origin-ip")
        String originIp() {
            return invocationContextAccessor.originIp().orElse("");
        }

        @GetMapping("/entity")
        ResponseEntity<ContextView> entity() {
            return ResponseEntity.status(201).header("X-Custom", "yes").body(new ContextView("entity"));
        }

        @GetMapping("/boom")
        String boom() {
            throw new ErrorCodeException(SystemErrors.ILLEGAL_ARGUMENT);
        }

        @PostMapping("/body")
        BodyView body(HttpServletRequest request) throws IOException {
            return new BodyView(
                    WebResponsePropertiesTest.readBody(request), WebResponsePropertiesTest.readBody(request));
        }
    }

    record ContextView(String requestId) {}

    record BodyView(String first, String second) {}

    private static String readBody(final HttpServletRequest request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
