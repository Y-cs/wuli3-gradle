package com.kjs.wuli3.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.web.accessor.WebContextAccessor;
import com.kjs.wuli3.web.wrapper.RequestId;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    private InvocationContextAccessor invocationContextAccessor;

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
    void requestIdIsGenerated() throws Exception {
        mockMvc.perform(get("/ok"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestId.HEADER_NAME));
    }

    @Test
    void contextIsAvailableThroughAccessors() throws Exception {
        mockMvc.perform(get("/context")
                        .header(RequestId.HEADER_NAME, "rid-context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("rid-context"));
    }

    @Test
    void webContextContainsRequestMetadata() throws Exception {
        mockMvc.perform(get("/web-context?keyword=java")
                        .header(RequestId.HEADER_NAME, "rid-web")
                        .header("X-Test", "test-header"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("rid-web"))
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.requestUri").value("/web-context"))
                .andExpect(jsonPath("$.queryString").value("keyword=java"))
                .andExpect(jsonPath("$.header").value("test-header"))
                .andExpect(jsonPath("$.parameter").value("java"));
    }

    @Test
    void requestBodyCanBeReadMoreThanOnce() throws Exception {
        mockMvc.perform(post("/body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("payload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first").value("payload"))
                .andExpect(jsonPath("$.second").value("payload"));
    }

    @Test
    void contextIsClearedAfterRequest() throws Exception {
        mockMvc.perform(get("/ok").header(RequestId.HEADER_NAME, "rid-clear"))
                .andExpect(status().isOk());

        assertThat(invocationContextAccessor.requestId()).isEmpty();
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
                InvocationContextAccessor invocationContextAccessor,
                AuthContextAccessor authContextAccessor,
                WebContextAccessor webContextAccessor
        ) {
            return new TestController(invocationContextAccessor, authContextAccessor, webContextAccessor);
        }

        @Bean
        @Primary
        AuthContextResolver testSecurityContextResolver() {
            return request -> new AuthContext(42L, "alice");
        }
    }

    @RestController
    static class TestController {
        private final InvocationContextAccessor invocationContextAccessor;
        private final AuthContextAccessor authContextAccessor;
        private final WebContextAccessor webContextAccessor;

        TestController(
                InvocationContextAccessor invocationContextAccessor,
                AuthContextAccessor authContextAccessor,
                WebContextAccessor webContextAccessor
        ) {
            this.invocationContextAccessor = invocationContextAccessor;
            this.authContextAccessor = authContextAccessor;
            this.webContextAccessor = webContextAccessor;
        }

        @GetMapping("/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/context")
        ContextView context() {
            return new ContextView(
                    invocationContextAccessor.requestId()
                            .orElse("")
            );
        }

        @GetMapping("/web-context")
        WebContextView webContext() {
            return new WebContextView(
                    webContextAccessor.requestId()
                            .orElse(""),
                    webContextAccessor.method()
                            .orElse(""),
                    webContextAccessor.requestUri()
                            .orElse(""),
                    webContextAccessor.queryString()
                            .orElse(""),
                    webContextAccessor.header("X-Test")
                            .orElse(""),
                    webContextAccessor.parameter("keyword")
                            .orElse("")
            );
        }

        @PostMapping("/body")
        BodyView body(HttpServletRequest request) throws IOException {
            return new BodyView(readBody(request), readBody(request));
        }

        @GetMapping("/security")
        SecurityView security() {
            return new SecurityView(
                    authContextAccessor.userId()
                            .orElse(-1L),
                    authContextAccessor.username()
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

        private static String readBody(HttpServletRequest request) throws IOException {
            return new String(request.getInputStream()
                    .readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    record ContextView(String requestId) {
    }

    record SecurityView(Long userId, String username) {
    }

    record WebContextView(
            String requestId,
            String method,
            String requestUri,
            String queryString,
            String header,
            String parameter
    ) {
    }

    record BodyView(String first, String second) {
    }
}
