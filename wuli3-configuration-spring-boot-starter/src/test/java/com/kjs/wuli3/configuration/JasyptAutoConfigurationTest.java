package com.kjs.wuli3.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringBootAutoConfiguration;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JasyptAutoConfigurationTest {

    @Test
    void decryptsEncryptedEnvironmentProperty() {
        final String password = "test-only-password";
        final BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(password);
        final String encryptedValue = encryptor.encrypt("plain-value");

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JasyptSpringBootAutoConfiguration.class))
                .withPropertyValues(
                        "jasypt.encryptor.password=" + password,
                        "jasypt.encryptor.algorithm=PBEWithMD5AndDES",
                        "jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator",
                        "sample.secret=ENC(" + encryptedValue + ")")
                .run(context -> assertThat(context.getEnvironment().getProperty("sample.secret"))
                        .isEqualTo("plain-value"));
    }
}
