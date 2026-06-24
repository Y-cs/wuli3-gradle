package com.kjs.wuli3.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjs.wuli3.core.error.BizException;
import com.kjs.wuli3.core.exception.CommonErrorCode;
import com.kjs.wuli3.web.autoconfigure.WebAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
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
    void exceptionIsMapped() throws Exception {
        mockMvc.perform(get("/boom").header(RequestId.HEADER_NAME, "rid-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.BAD_REQUEST.code()))
                .andExpect(jsonPath("$.requestId").value("rid-2"));
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @TestConfiguration
    static class ControllerConfiguration {
        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/boom")
        String boom() {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }
}
