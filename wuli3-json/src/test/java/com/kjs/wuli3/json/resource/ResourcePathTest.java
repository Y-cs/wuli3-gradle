package com.kjs.wuli3.json.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjs.wuli3.json.datatype.resource.DefaultResourcePathResolver;
import com.kjs.wuli3.json.datatype.resource.ResourcePath;
import com.kjs.wuli3.json.datatype.resource.ResourcePathResolver;
import com.kjs.wuli3.json.provider.JsonMapperFactory;
import com.kjs.wuli3.json.provider.JsonMapperResourcePathAssembly;
import org.junit.jupiter.api.Test;

class ResourcePathTest {
    @Test
    void defaultResolverKeepsValueUnchanged() throws Exception {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new DefaultResourcePathResolver());
        final DefaultSample sample = objectMapper.readValue("""
                        {"path":"https://static.example.com/files/demo.png"}
                        """, DefaultSample.class);

        assertThat(sample.path()).isEqualTo("https://static.example.com/files/demo.png");
        assertThat(objectMapper.writeValueAsString(new DefaultSample("files/demo.png")))
                .contains("\"path\":\"files/demo.png\"");
    }

    @Test
    void customResolverRestoresUrlAndResolvesPathOnRecordComponent() throws Exception {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new CdnResolver());
        final CdnSample sample = objectMapper.readValue("""
                        {"path":"https://static.example.com/files/demo.png"}
                        """, CdnSample.class);

        assertThat(sample.path()).isEqualTo("/files/demo.png");
        assertThat(objectMapper.writeValueAsString(sample))
                .contains("\"path\":\"https://static.example.com/files/demo.png\"");
    }

    @Test
    void customResolverKeepsBlankStringsAndExternalUrlsUnchanged() throws Exception {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new CdnResolver());

        assertThat(objectMapper.writeValueAsString(new CdnSample(" "))).contains("\"path\":\" \"");
        assertThat(objectMapper.readValue("""
                        {"path":"https://other.example.com/files/demo.png"}
                        """, CdnSample.class).path())
                .isEqualTo("https://other.example.com/files/demo.png");
        assertThat(objectMapper.writeValueAsString(new CdnSample("https://other.example.com/files/demo.png")))
                .contains("\"path\":\"https://other.example.com/files/demo.png\"");
    }

    @Test
    void unsupportedTypeDeserializesValueUnchanged() throws Exception {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new DefaultResourcePathResolver());
        final UnsupportedSample sample = objectMapper.readValue("""
                        {"path":"files/demo.png"}
                        """, UnsupportedSample.class);

        assertThat(sample.path()).isEqualTo("files/demo.png");
    }

    @Test
    void nullResourcePathValueIsPreserved() throws Exception {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new DefaultResourcePathResolver());

        assertThat(objectMapper.readValue("""
                        {"path":null}
                        """, DefaultSample.class).path()).isNull();
    }

    @Test
    void resourcePathModuleAppliesResourcePathOnFields() throws Exception {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new CdnResolver());
        final MutableCdnSample sample = objectMapper.readValue("""
                        {"path":"https://static.example.com/files/demo.png"}
                        """, MutableCdnSample.class);

        assertThat(sample.path).isEqualTo("/files/demo.png");
        assertThat(objectMapper.writeValueAsString(sample))
                .contains("\"path\":\"https://static.example.com/files/demo.png\"");
    }

    @Test
    void nonStringResourcePathFailsDuringMapping() {
        final ObjectMapper objectMapper = ResourcePathTest.resourcePathObjectMapper(new DefaultResourcePathResolver());

        assertThatThrownBy(() -> objectMapper.writeValueAsString(new NonStringSample(1)))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("@ResourcePath can only be used on String values");
    }

    record DefaultSample(@ResourcePath String path) {}

    record CdnSample(@ResourcePath(type = CdnResolver.TYPE) String path) {}

    record UnsupportedSample(@ResourcePath(type = "avatar") String path) {}

    record NonStringSample(@ResourcePath Integer path) {}

    static final class MutableCdnSample {
        @ResourcePath(type = CdnResolver.TYPE)
        public String path = "";
    }

    private static ObjectMapper resourcePathObjectMapper(final ResourcePathResolver resolver) {
        return JsonMapperFactory.standardJsonMapperFactory()
                .addAssemblyChain(new JsonMapperResourcePathAssembly(resolver))
                .create();
    }

    public static final class CdnResolver implements ResourcePathResolver {
        static final String TYPE = "image";
        private static final String DOMAIN = "https://static.example.com";

        @Override
        public boolean supports(final String type) {
            return CdnResolver.TYPE.equals(type);
        }

        @Override
        public String serialize(final String type, final String path) {
            if (path.isBlank() || ResourcePathTest.isAbsoluteUrl(path)) {
                return path;
            }
            final String normalizedPath = path.startsWith("/") ? path : "/" + path;
            return CdnResolver.DOMAIN + normalizedPath;
        }

        @Override
        public String deserialize(final String type, final String url) {
            if (url.isBlank() || !url.startsWith(CdnResolver.DOMAIN + "/")) {
                return url;
            }
            return url.substring(CdnResolver.DOMAIN.length());
        }
    }

    private static boolean isAbsoluteUrl(final String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }
}
