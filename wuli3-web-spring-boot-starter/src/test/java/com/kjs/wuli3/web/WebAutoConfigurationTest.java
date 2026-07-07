package com.kjs.wuli3.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.codec.InvocationContextCodec;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.snapshot.ContextPropagator;
import com.kjs.wuli3.propagation.transmission.ContextTransmitter;
import com.kjs.wuli3.web.annotation.NativeResponse;
import com.kjs.wuli3.web.annotation.NativeResponseMode;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.autoconfigure.WebAutoConfiguration;
import com.kjs.wuli3.web.context.RequestIds;
import com.kjs.wuli3.web.context.WebContextAccessor;
import com.kjs.wuli3.web.error.ErrorAlertContext;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

    @Autowired
    private TestErrorAlertNotifier errorAlertNotifier;

    @Autowired
    private FailingErrorAlertNotifier failingErrorAlertNotifier;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void autoConfigurationIsListedInBootImports() {
        assertThat(ImportCandidates.load(AutoConfiguration.class, this.getClass()
                .getClassLoader())).contains(WebAutoConfiguration.class.getName());
    }

    @Test
    void contextPropagationBeansAreConfigured() {
        assertThat(applicationContext.getBean(ContextPropagator.class)).isNotNull();
        assertThat(applicationContext.getBean(InvocationContextCodec.class)).isNotNull();
        assertThat(applicationContext.getBean(ContextTransmitter.class)).isNotNull();
        assertThat(RequestIds.HEADER_NAME).isEqualTo(InvocationContextCodec.REQUEST_ID);
    }

    @Test
    void requestIdIsPropagated() throws Exception {
        mockMvc.perform(get("/ok").header(RequestIds.HEADER_NAME, "rid-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIds.HEADER_NAME, "rid-1"))
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.data").value("ok"));
    }

    @Test
    void requestIdIsGenerated() throws Exception {
        mockMvc.perform(get("/ok"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIds.HEADER_NAME));
    }

    @Test
    void contextIsAvailableThroughAccessors() throws Exception {
        mockMvc.perform(get("/context")
                        .header(RequestIds.HEADER_NAME, "rid-context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value("rid-context"));
    }

    @Test
    void webContextContainsRequestMetadata() throws Exception {
        mockMvc.perform(get("/web-context?keyword=java")
                        .header(RequestIds.HEADER_NAME, "rid-web")
                        .header("X-Test", "test-header"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("rid-web"))
                .andExpect(jsonPath("$.data.method").value("GET"))
                .andExpect(jsonPath("$.data.requestUri").value("/web-context"))
                .andExpect(jsonPath("$.data.queryString").value("keyword=java"))
                .andExpect(jsonPath("$.data.header").value("test-header"))
                .andExpect(jsonPath("$.data.parameter").value("java"));
    }

    @Test
    void requestBodyCanBeReadMoreThanOnce() throws Exception {
        mockMvc.perform(post("/body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("payload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.first").value("payload"))
                .andExpect(jsonPath("$.data.second").value("payload"));
    }

    @Test
    void contextIsClearedAfterRequest() throws Exception {
        mockMvc.perform(get("/ok").header(RequestIds.HEADER_NAME, "rid-clear"))
                .andExpect(status().isOk());

        assertThat(invocationContextAccessor.requestId()).isEmpty();
    }

    @Test
    void customSecurityResolverIsUsed() throws Exception {
        mockMvc.perform(get("/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(42))
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void alreadyWrappedResponseIsNotWrappedAgain() throws Exception {
        mockMvc.perform(get("/wrapped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.data").value("wrapped"));
    }

    @Test
    void responseEntityKeepsStatusAndHeader() throws Exception {
        mockMvc.perform(get("/entity"))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Custom", "yes"))
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.data.requestId").value("entity"));
    }

    @Test
    void nativeResponseSkipsSuccessWrappingByDefault() throws Exception {
        mockMvc.perform(get("/native-success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("native"));
    }

    @Test
    void nativeResponseStillWrapsErrorByDefault() throws Exception {
        mockMvc.perform(get("/native-default-error").header(RequestIds.HEADER_NAME, "rid-native-default"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM.ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.requestId").value("rid-native-default"));
    }

    @Test
    void nativeResponseAllSkipsSuccessAndErrorWrapping() throws Exception {
        mockMvc.perform(get("/native-all-success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("native-all"));

        mockMvc.perform(get("/native-all-error").header(RequestIds.HEADER_NAME, "rid-native-all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(SystemErrors.ILLEGAL_ARGUMENT.getMessage()))
                .andExpect(jsonPath("$.code").value("SYSTEM.ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.requestId").value("rid-native-all"));
    }

    @Test
    void frameworkExceptionsUseSemanticHttpStatus() throws Exception {
        mockMvc.perform(post("/ok"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("WEB.BAD_REQUEST"));

        mockMvc.perform(post("/json-only")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("WEB.BAD_REQUEST"));
    }

    @Test
    void validationExceptionIncludesFieldDetail() throws Exception {
        mockMvc.perform(post("/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEB.BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors[0].field").value("name"))
                .andExpect(jsonPath("$.data.errors[0].message").value("不能为空"));
    }

    @Test
    void jsonParseExceptionIsMapped() throws Exception {
        mockMvc.perform(post("/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEB.BAD_REQUEST"));
    }

    @Test
    void missingResourceIsMappedToNotFound() throws Exception {
        mockMvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WEB.NOT_FOUND"));
    }

    @Test
    void exceptionIsMapped() throws Exception {
        mockMvc.perform(get("/boom").header(RequestIds.HEADER_NAME, "rid-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM.ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.message").value(SystemErrors.ILLEGAL_ARGUMENT.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-2"));
    }

    @Test
    void errorAlertNotifierIsCalled() throws Exception {
        errorAlertNotifier.reset();
        failingErrorAlertNotifier.reset();

        mockMvc.perform(get("/boom").header(RequestIds.HEADER_NAME, "rid-alert"))
                .andExpect(status().isBadRequest());

        assertThat(errorAlertNotifier.await()).isTrue();
        assertThat(failingErrorAlertNotifier.await()).isTrue();
        assertThat(errorAlertNotifier.error()).isInstanceOf(ErrorCodeException.class);
        assertThat(errorAlertNotifier.requestUri()).isEqualTo("/boom");
        assertThat(errorAlertNotifier.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorAlertNotifier.responseCode()).isEqualTo(SystemErrors.ILLEGAL_ARGUMENT);
    }

    @Test
    void codeOnlyExceptionHidesMessage() throws Exception {
        mockMvc.perform(get("/code-only").header(RequestIds.HEADER_NAME, "rid-3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM.UNSUPPORTED_OPERATION"))
                .andExpect(jsonPath("$.message").value(WebErrors.INTERNAL_ERROR.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-3"));
    }

    @Test
    void messageOnlyExceptionHidesCode() throws Exception {
        mockMvc.perform(get("/message-only").header(RequestIds.HEADER_NAME, "rid-4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("visible message"))
                .andExpect(jsonPath("$.requestId").value("rid-4"));
    }

    @Test
    void internalExceptionHidesCodeAndMessage() throws Exception {
        mockMvc.perform(get("/internal").header(RequestIds.HEADER_NAME, "rid-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(WebErrors.INTERNAL_ERROR.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-5"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
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

        @Bean
        TestErrorAlertNotifier testErrorAlertNotifier() {
            return new TestErrorAlertNotifier();
        }

        @Bean
        FailingErrorAlertNotifier failingErrorAlertNotifier() {
            return new FailingErrorAlertNotifier();
        }
    }

    static final class TestErrorAlertNotifier implements ErrorAlertNotifier {
        private final AtomicReference<Throwable> error = new AtomicReference<>();
        private final AtomicReference<String> requestUri = new AtomicReference<>();
        private final AtomicReference<HttpStatus> status = new AtomicReference<>();
        private final AtomicReference<ErrorCode> responseCode = new AtomicReference<>();
        private final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        @Override
        public void alert(final ErrorAlertContext context) {
            this.error.set(context.error());
            this.requestUri.set(context.requestUri());
            this.status.set(context.status());
            this.responseCode.set(context.responseCode());
            this.latch.get()
                    .countDown();
        }

        private void reset() {
            this.latch.set(new CountDownLatch(1));
        }

        private boolean await() throws InterruptedException {
            return this.latch.get()
                    .await(2, TimeUnit.SECONDS);
        }

        private @Nullable Throwable error() {
            return this.error.get();
        }

        private @Nullable String requestUri() {
            return this.requestUri.get();
        }

        private @Nullable HttpStatus status() {
            return this.status.get();
        }

        private @Nullable ErrorCode responseCode() {
            return this.responseCode.get();
        }
    }

    static final class FailingErrorAlertNotifier implements ErrorAlertNotifier {
        private final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));

        @Override
        public void alert(final ErrorAlertContext context) {
            this.latch.get()
                    .countDown();
            throw new IllegalStateException("alert failed");
        }

        private void reset() {
            this.latch.set(new CountDownLatch(1));
        }

        private boolean await() throws InterruptedException {
            return this.latch.get()
                    .await(2, TimeUnit.SECONDS);
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

        @GetMapping("/wrapped")
        ApiResponse<String> wrapped() {
            return ApiResponse.success("wrapped", null);
        }

        @GetMapping("/entity")
        ResponseEntity<ContextView> entity() {
            return ResponseEntity.status(201)
                    .header("X-Custom", "yes")
                    .body(new ContextView("entity"));
        }

        @NativeResponse
        @GetMapping("/native-success")
        ContextView nativeSuccess() {
            return new ContextView("native");
        }

        @NativeResponse
        @GetMapping("/native-default-error")
        String nativeDefaultError() {
            throw new ErrorCodeException(SystemErrors.ILLEGAL_ARGUMENT);
        }

        @NativeResponse(NativeResponseMode.ALL)
        @GetMapping("/native-all-success")
        ContextView nativeAllSuccess() {
            return new ContextView("native-all");
        }

        @NativeResponse(NativeResponseMode.ALL)
        @GetMapping("/native-all-error")
        String nativeAllError() {
            throw new ErrorCodeException(SystemErrors.ILLEGAL_ARGUMENT);
        }

        @PostMapping(value = "/json-only", consumes = MediaType.APPLICATION_JSON_VALUE)
        ContextView jsonOnly() {
            return new ContextView("json");
        }

        @PostMapping("/validated")
        ContextView validated(@Valid @RequestBody ValidatedRequest request) {
            return new ContextView(request.name());
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

    record ValidatedRequest(@NotBlank(message = "不能为空") String name) {
    }
}
