package com.kjs.wuli3.redis.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.redis.RedisKey;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class HashRedisOperationsTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, String, String> hashOperations;

    private HashRedisOperations operations;

    @BeforeEach
    void setUp() {
        when(this.redisTemplate.<String, String>opsForHash()).thenReturn(this.hashOperations);
        this.operations = new HashRedisOperations(this.redisTemplate);
    }

    @Test
    void writesJsonFieldsAndRefreshesExpiration() {
        final Duration timeToLive = Duration.ofMinutes(5);
        final RedisKey key = RedisKey.expiring("orders", timeToLive);
        final Sample value = new Sample("created");

        this.operations.put(key, "1", value);
        this.operations.putAll(key, Map.of("2", value));

        verify(this.hashOperations).put(key.value(), "1", Jsons.toJson(value));
        verify(this.hashOperations).putAll(key.value(), Map.of("2", Jsons.toJson(value)));
        verify(this.redisTemplate, org.mockito.Mockito.times(2)).expire(key.value(), timeToLive);
    }

    @Test
    void onlyRefreshesExpirationWhenPutIfAbsentWrites() {
        final RedisKey key = RedisKey.expiring("orders", Duration.ofMinutes(5));
        final Sample value = new Sample("created");
        when(this.hashOperations.putIfAbsent(key.value(), "1", Jsons.toJson(value)))
                .thenReturn(Boolean.TRUE, Boolean.FALSE);

        assertThat(this.operations.putIfAbsent(key, "1", value)).isTrue();
        assertThat(this.operations.putIfAbsent(key, "1", value)).isFalse();

        verify(this.redisTemplate).expire(key.value(), Duration.ofMinutes(5));
    }

    @Test
    void readsConcreteAndGenericValues() {
        final RedisKey key = RedisKey.persistent("orders");
        final Sample value = new Sample("created");
        when(this.hashOperations.get(key.value(), "1")).thenReturn(Jsons.toJson(value), Jsons.toJson(List.of(value)));
        when(this.hashOperations.entries(key.value())).thenReturn(Map.of("1", Jsons.toJson(value)));

        assertThat(this.operations.get(key, "1", Sample.class)).contains(value);
        assertThat(this.operations.get(key, "1", new TypeReference<List<Sample>>() {}))
                .contains(List.of(value));
        assertThat(this.operations.entries(key, Sample.class)).containsEntry("1", value);
        assertThat(this.operations.entries(key, new TypeReference<Sample>() {})).containsEntry("1", value);
    }

    @Test
    void supportsFieldMetadataAndDeletion() {
        final RedisKey key = RedisKey.persistent("orders");
        when(this.hashOperations.delete(key.value(), "1", "2")).thenReturn(2L);
        when(this.hashOperations.hasKey(key.value(), "1")).thenReturn(Boolean.TRUE);
        when(this.hashOperations.size(key.value())).thenReturn(3L);

        assertThat(this.operations.delete(key, "1", "2")).isEqualTo(2L);
        assertThat(this.operations.hasField(key, "1")).isTrue();
        assertThat(this.operations.size(key)).isEqualTo(3L);
        verify(this.redisTemplate, never())
                .expire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    private record Sample(String state) {}
}
