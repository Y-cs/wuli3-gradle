package com.kjs.wuli3.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategy;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationTypes;
import com.kjs.wuli3.json.datatype.desensitization.Desensitized;
import com.kjs.wuli3.web.autoconfigure.WebAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = {
            WebDesensitizationAutoConfigurationTest.TestApplication.class,
            WebAutoConfiguration.class,
            WebDesensitizationAutoConfigurationTest.ControllerConfiguration.class,
        })
@AutoConfigureMockMvc
class WebDesensitizationAutoConfigurationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void webObjectMapperDesensitizesAnnotatedValuesAndUsesStrategyBeans() throws Exception {
        this.mockMvc
                .perform(get("/sensitive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("web-custom"))
                .andExpect(jsonPath("$.data.email").value("a****@example.com"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {}

    @TestConfiguration
    static class ControllerConfiguration {
        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        DesensitizationStrategy phoneDesensitizationStrategy() {
            return DesensitizationStrategy.of(DesensitizationTypes.PHONE, value -> "web-custom");
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/sensitive")
        SensitiveView sensitive() {
            return new SensitiveView("13812345678", "alice@example.com");
        }
    }

    record SensitiveView(
            @Desensitized(type = DesensitizationTypes.PHONE) String phone,
            @Desensitized(type = DesensitizationTypes.EMAIL) String email) {}
}
