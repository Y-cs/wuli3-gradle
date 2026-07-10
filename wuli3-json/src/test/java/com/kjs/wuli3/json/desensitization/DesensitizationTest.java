package com.kjs.wuli3.json.desensitization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategy;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategyRegistry;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationTypes;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationVisibilityPolicy;
import com.kjs.wuli3.json.datatype.desensitization.Desensitized;
import com.kjs.wuli3.json.datatype.resource.ResourcePath;
import com.kjs.wuli3.json.provider.JsonMapperDesensitizationAssembly;
import com.kjs.wuli3.json.provider.JsonMapperFactory;
import org.junit.jupiter.api.Test;

class DesensitizationTest {

    @Test
    void masksBuiltInSensitiveDataTypes() throws Exception {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        final String json = objectMapper.writeValueAsString(
                new SensitiveSample("13812345678", "alice@example.com", "110101199001011234", "6222021234567890123"));

        assertThat(json)
                .contains("\"phone\":\"138****5678\"")
                .contains("\"email\":\"a****@example.com\"")
                .contains("\"idCard\":\"110101********1234\"")
                .contains("\"bankCard\":\"6222 **** **** **** 123\"");
    }

    @Test
    void fullyMasksShortAndMalformedValues() throws Exception {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        final String json = objectMapper.writeValueAsString(new ShortSample("1234567", "invalid"));

        assertThat(json).contains("\"phone\":\"*******\"").contains("\"email\":\"*******\"");
    }

    @Test
    void keepsNullAndEmptyValues() throws Exception {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        final NullableSample sample = objectMapper.readValue("""
                        {"phone":null,"email":""}
                        """, NullableSample.class);
        final String json = objectMapper.writeValueAsString(sample);

        assertThat(json).contains("\"phone\":null").contains("\"email\":\"\"");
    }

    @Test
    void supportsFieldsAndGetterAnnotations() throws Exception {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());
        final MutableSample sample = new MutableSample();
        sample.phone = "13812345678";

        assertThat(objectMapper.writeValueAsString(sample))
                .contains("\"phone\":\"138****5678\"")
                .contains("\"email\":\"a****@example.com\"");
    }

    @Test
    void customStrategiesOverrideBuiltInStrategies() throws Exception {
        final DesensitizationStrategy override =
                DesensitizationStrategy.of(DesensitizationTypes.PHONE, value -> "custom");
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standardWithOverrides(java.util.List.of(override)),
                DesensitizationVisibilityPolicy.alwaysMask());

        assertThat(objectMapper.writeValueAsString(new PhoneSample("13812345678")))
                .contains("\"phone\":\"custom\"");
    }

    @Test
    void visibilityPolicyCanExposeRawValue() throws Exception {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), annotation -> true);

        assertThat(objectMapper.writeValueAsString(new PhoneSample("13812345678")))
                .contains("\"phone\":\"13812345678\"");
    }

    @Test
    void unknownTypeFailsSerialization() {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        assertThatThrownBy(() -> objectMapper.writeValueAsString(new UnknownSample("secret")))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("No desensitization strategy found for type 'unknown'");
    }

    @Test
    void strategyFailureFailsSerializationWithoutExposingValue() {
        final DesensitizationStrategy strategy = DesensitizationStrategy.of("failing", value -> {
            throw new IllegalStateException("failed");
        });
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standardWithOverrides(java.util.List.of(strategy)),
                DesensitizationVisibilityPolicy.alwaysMask());

        assertThatThrownBy(() -> objectMapper.writeValueAsString(new FailingSample("raw-secret")))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Failed to desensitize JSON value for type 'failing'")
                .hasMessageNotContaining("raw-secret");
    }

    @Test
    void nonStringValueFailsSerialization() {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        assertThatThrownBy(() -> objectMapper.writeValueAsString(new NonStringSample(13812345678L)))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("@Desensitized can only be used on String values");
    }

    @Test
    void conflictingResourcePathAnnotationFailsSerialization() {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        assertThatThrownBy(() -> objectMapper.writeValueAsString(new ResourcePathConflictSample("13812345678")))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("@Desensitized cannot be combined with @ResourcePath");
    }

    @Test
    void conflictingJsonSerializerFailsSerialization() {
        final ObjectMapper objectMapper = DesensitizationTest.desensitizationObjectMapper(
                DesensitizationStrategyRegistry.standard(), DesensitizationVisibilityPolicy.alwaysMask());

        assertThatThrownBy(() -> objectMapper.writeValueAsString(new SerializerConflictSample("13812345678")))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("@Desensitized cannot be combined with another property serializer");
    }

    record SensitiveSample(
            @Desensitized(type = DesensitizationTypes.PHONE) String phone,
            @Desensitized(type = DesensitizationTypes.EMAIL) String email,

            @Desensitized(type = DesensitizationTypes.ID_CARD)
            String idCard,

            @Desensitized(type = DesensitizationTypes.BANK_CARD)
            String bankCard) {}

    record ShortSample(
            @Desensitized(type = DesensitizationTypes.PHONE) String phone,
            @Desensitized(type = DesensitizationTypes.EMAIL) String email) {}

    record NullableSample(
            @Desensitized(type = DesensitizationTypes.PHONE) String phone,
            @Desensitized(type = DesensitizationTypes.EMAIL) String email) {}

    record PhoneSample(
            @Desensitized(type = DesensitizationTypes.PHONE) String phone) {}

    record UnknownSample(@Desensitized(type = "unknown") String value) {}

    record FailingSample(@Desensitized(type = "failing") String value) {}

    record NonStringSample(
            @Desensitized(type = DesensitizationTypes.PHONE) Long phone) {}

    record ResourcePathConflictSample(
            @Desensitized(type = DesensitizationTypes.PHONE) @ResourcePath
            String phone) {}

    record SerializerConflictSample(
            @Desensitized(type = DesensitizationTypes.PHONE) @JsonSerialize(using = ToStringSerializer.class)
            String phone) {}

    static final class MutableSample {
        @Desensitized(type = DesensitizationTypes.PHONE)
        public String phone = "";

        @Desensitized(type = DesensitizationTypes.EMAIL)
        public String getEmail() {
            return "alice@example.com";
        }
    }

    private static ObjectMapper desensitizationObjectMapper(
            final DesensitizationStrategyRegistry registry, final DesensitizationVisibilityPolicy visibilityPolicy) {
        return JsonMapperFactory.standardJsonMapperFactory()
                .addAssemblyChain(new JsonMapperDesensitizationAssembly(registry, visibilityPolicy))
                .create();
    }
}
