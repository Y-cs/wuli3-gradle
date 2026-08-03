package com.kjs.wuli3.web.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.json.datatype.resource.ResourcePath;
import com.kjs.wuli3.json.datatype.resource.ResourcePathResolver;
import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.RequestIds;
import com.kjs.wuli3.web.error.ErrorAlertContext;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import com.kjs.wuli3.web.error.WebErrors;
import com.kjs.wuli3.web.response.ApiResponse;
import com.kjs.wuli3.web.response.NativeResponse;
import com.kjs.wuli3.web.response.NativeResponseMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = {
            WebAutoConfigurationTest.TestApplication.class,
            WebAutoConfigurationTest.ControllerConfiguration.class,
        })
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
        assertThat(ImportCandidates.load(
                        AutoConfiguration.class, this.getClass().getClassLoader()))
                .contains(
                        WebContextAutoConfiguration.class.getName(),
                        WebJsonAutoConfiguration.class.getName(),
                        WebErrorAutoConfiguration.class.getName(),
                        WebResponseAutoConfiguration.class.getName());
    }

    @Test
    void requestContextBeansAreConfigured() {
        assertThat(applicationContext.getBeansOfType(RestClientCustomizer.class))
                .containsKey("wuli3InvocationContextRestClientCustomizer");
        assertThat(applicationContext.getBeansOfType(RestTemplateCustomizer.class))
                .containsKey("wuli3InvocationContextRestTemplateCustomizer");
        assertThat(RequestIds.HEADER_NAME).isEqualTo(InvocationContextEncoder.REQUEST_ID);
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
        mockMvc.perform(get("/ok")).andExpect(status().isOk()).andExpect(header().exists(RequestIds.HEADER_NAME));
    }

    @Test
    void contextIsAvailableThroughAccessors() throws Exception {
        mockMvc.perform(get("/context").header(RequestIds.HEADER_NAME, "rid-context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value("rid-context"));
    }

    @Test
    void webObjectMapperUsesResourcePathResolverBean() throws Exception {
        assertThat(this.applicationContext.getBean(ResourcePathResolver.class))
                .isInstanceOf(TestResourcePathResolver.class);
        mockMvc.perform(get("/resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.path").value("https://static.example.com/files/demo.png"));
    }

    @Test
    void webObjectMapperUsesBaseJsonConfiguration() throws Exception {
        mockMvc.perform(get("/json-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dateTime").value("2026-06-22 10:30:05"));
    }

    @Test
    void webObjectMapperKeepsBusinessModuleBeans() throws Exception {
        mockMvc.perform(get("/business-json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("business:kept"));
    }

    @Test
    void contextIsClearedAfterRequest() throws Exception {
        mockMvc.perform(get("/ok").header(RequestIds.HEADER_NAME, "rid-clear")).andExpect(status().isOk());

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
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
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
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.requestId").value("rid-native-all"));
    }

    @Test
    void frameworkExceptionsUseSemanticHttpStatus() throws Exception {
        mockMvc.perform(post("/ok"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("WEB.BAD_REQUEST"));

        mockMvc.perform(post("/json-only").contentType(MediaType.TEXT_PLAIN).content("payload"))
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
                .andExpect(jsonPath("$.data.errors[0].code").value("NotBlank"))
                .andExpect(jsonPath("$.data.errors[0].rejectedValue").doesNotExist())
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
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(SystemErrors.ILLEGAL_ARGUMENT.getMessage()))
                .andExpect(jsonPath("$.requestId").value("rid-2"));
    }

    @Test
    void errorAlertNotifierIsCalled() throws Exception {
        errorAlertNotifier.reset();
        failingErrorAlertNotifier.reset();

        mockMvc.perform(get("/critical").header(RequestIds.HEADER_NAME, "rid-alert"))
                .andExpect(status().isInternalServerError());

        assertThat(errorAlertNotifier.await()).isTrue();
        assertThat(failingErrorAlertNotifier.await()).isTrue();
        assertThat(errorAlertNotifier.error()).isInstanceOf(ErrorCodeException.class);
        assertThat(errorAlertNotifier.requestUri()).isEqualTo("/critical");
        assertThat(errorAlertNotifier.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorAlertNotifier.responseCode()).isEqualTo(WebErrors.INTERNAL_ERROR);
    }

    @Test
    void normalBusinessErrorDoesNotAlert() throws Exception {
        errorAlertNotifier.reset();
        failingErrorAlertNotifier.reset();

        mockMvc.perform(get("/boom")).andExpect(status().isBadRequest());

        assertThat(errorAlertNotifier.await(100)).isFalse();
        assertThat(failingErrorAlertNotifier.await(100)).isFalse();
    }

    @Test
    void illegalArgumentIsTreatedAsServerFailure() throws Exception {
        mockMvc.perform(get("/illegal-argument"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("WEB.INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(WebErrors.INTERNAL_ERROR.getMessage()));
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
    static class TestApplication {}

    @TestConfiguration
    static class ControllerConfiguration {
        @Bean
        TestController testController(
                InvocationContextAccessor invocationContextAccessor, AuthContextAccessor authContextAccessor) {
            return new TestController(invocationContextAccessor, authContextAccessor);
        }

        @Bean
        @Primary
        AuthContextResolver testSecurityContextResolver() {
            return request -> java.util.Optional.of(new AuthContext(42L, "alice"));
        }

        @Bean
        TestErrorAlertNotifier testErrorAlertNotifier() {
            return new TestErrorAlertNotifier();
        }

        @Bean
        FailingErrorAlertNotifier failingErrorAlertNotifier() {
            return new FailingErrorAlertNotifier();
        }

        @Bean
        TestResourcePathResolver testResourcePathResolver() {
            return new TestResourcePathResolver();
        }

        @Bean
        Module businessJacksonModule() {
            final SimpleModule module = new SimpleModule("business-test");
            module.addSerializer(BusinessValue.class, new JsonSerializer<>() {
                @Override
                public void serialize(
                        final BusinessValue value, final JsonGenerator generator, final SerializerProvider serializers)
                        throws IOException {
                    generator.writeString("business:" + value.value());
                }
            });
            return module;
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
            this.latch.get().countDown();
        }

        private void reset() {
            this.latch.set(new CountDownLatch(1));
        }

        private boolean await() throws InterruptedException {
            return this.latch.get().await(2, TimeUnit.SECONDS);
        }

        private boolean await(final long timeoutMillis) throws InterruptedException {
            return this.latch.get().await(timeoutMillis, TimeUnit.MILLISECONDS);
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
            this.latch.get().countDown();
            throw new IllegalStateException("alert failed");
        }

        private void reset() {
            this.latch.set(new CountDownLatch(1));
        }

        private boolean await() throws InterruptedException {
            return this.latch.get().await(2, TimeUnit.SECONDS);
        }

        private boolean await(final long timeoutMillis) throws InterruptedException {
            return this.latch.get().await(timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    @RestController
    static class TestController {
        private final InvocationContextAccessor invocationContextAccessor;
        private final AuthContextAccessor authContextAccessor;

        TestController(InvocationContextAccessor invocationContextAccessor, AuthContextAccessor authContextAccessor) {
            this.invocationContextAccessor = invocationContextAccessor;
            this.authContextAccessor = authContextAccessor;
        }

        @GetMapping("/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/context")
        ContextView context() {
            return new ContextView(invocationContextAccessor.requestId().orElse(""));
        }

        @GetMapping("/security")
        SecurityView security() {
            return new SecurityView(
                    authContextAccessor.userId().orElse(-1L),
                    authContextAccessor.username().orElse(""));
        }

        @GetMapping("/wrapped")
        ApiResponse<String> wrapped() {
            return ApiResponse.success("wrapped", null);
        }

        @GetMapping("/resource")
        ResourceView resource() {
            return new ResourceView("/files/demo.png");
        }

        @GetMapping("/json-time")
        TimeView jsonTime() {
            return new TimeView(LocalDateTime.of(2026, 6, 22, 10, 30, 5));
        }

        @GetMapping("/business-json")
        BusinessValue businessJson() {
            return new BusinessValue("kept");
        }

        @GetMapping("/entity")
        ResponseEntity<ContextView> entity() {
            return ResponseEntity.status(201).header("X-Custom", "yes").body(new ContextView("entity"));
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

        @GetMapping("/critical")
        String critical() {
            throw new ErrorCodeException(SystemErrors.INTERNAL_ERROR).severity(ErrorSeverity.CRITICAL);
        }

        @GetMapping("/illegal-argument")
        String illegalArgument() {
            throw new IllegalArgumentException("programming error");
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

    record ContextView(String requestId) {}

    record SecurityView(Long userId, String username) {}

    record ResourceView(
            @ResourcePath(type = TestResourcePathResolver.TYPE)
            String path) {}

    record TimeView(LocalDateTime dateTime) {}

    record BusinessValue(String value) {}

    record ValidatedRequest(@NotBlank(message = "不能为空") String name) {}

    static final class TestResourcePathResolver implements ResourcePathResolver {
        static final String TYPE = "web-test";
        private static final String DOMAIN = "https://static.example.com";

        @Override
        public boolean supports(final @NonNull String type) {
            return TestResourcePathResolver.TYPE.equals(type);
        }

        @Override
        public String serialize(final String type, final String path) {
            return TestResourcePathResolver.DOMAIN + path;
        }

        @Override
        public String deserialize(final String type, final String url) {
            return url.substring(TestResourcePathResolver.DOMAIN.length());
        }
    }
}
