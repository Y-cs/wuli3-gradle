package com.kjs.wuli3.redis.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.redis.RedisKey;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ObjectRedisOperationsTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectRedisOperations operations;

    @BeforeEach
    void setUp() {
        when(this.redisTemplate.opsForValue()).thenReturn(this.valueOperations);
        this.operations = new ObjectRedisOperations(this.redisTemplate);
    }

    @Test
    void storesPlainJsonAndReadsConcreteTypes() {
        final RedisKey key = RedisKey.persistent("orders:1");
        final Sample value = new Sample("created", Instant.parse("2026-08-11T00:00:00Z"));
        final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);

        this.operations.set(key, value);
        verify(this.valueOperations).set(org.mockito.ArgumentMatchers.eq(key.value()), jsonCaptor.capture());
        assertThat(jsonCaptor.getValue()).doesNotContain("@class", Sample.class.getName());

        when(this.valueOperations.get(key.value())).thenReturn(jsonCaptor.getValue());
        assertThat(this.operations.get(key, Sample.class)).contains(value);
    }

    @Test
    void readsGenericTypesAndMapsMissingKeysToEmpty() {
        final RedisKey key = RedisKey.persistent("orders:list");
        final List<Sample> values = List.of(new Sample("created", Instant.EPOCH));
        when(this.valueOperations.get(key.value())).thenReturn(Jsons.toJson(values), (String) null);

        assertThat(this.operations.get(key, new TypeReference<List<Sample>>() {}))
                .contains(values);
        assertThat(this.operations.get(key, Sample.class)).isEmpty();
    }

    @Test
    void appliesExpirationDuringSetAndSetIfAbsent() {
        final Duration timeToLive = Duration.ofMinutes(5);
        final RedisKey key = RedisKey.expiring("orders:1", timeToLive);
        final Sample value = new Sample("created", Instant.EPOCH);
        when(this.valueOperations.setIfAbsent(
                        org.mockito.ArgumentMatchers.eq(key.value()),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.eq(timeToLive)))
                .thenReturn(Boolean.TRUE, (Boolean) null);

        this.operations.set(key, value);
        assertThat(this.operations.setIfAbsent(key, value)).isTrue();
        assertThat(this.operations.setIfAbsent(key, value)).isFalse();

        verify(this.valueOperations)
                .set(
                        org.mockito.ArgumentMatchers.eq(key.value()),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.eq(timeToLive));
    }

    @Test
    void mapsSerializationFailures() {
        final RedisKey key = RedisKey.persistent("orders:1");
        final Map<String, Object> cyclicValue = new HashMap<>();
        cyclicValue.put("self", cyclicValue);

        assertThatThrownBy(() -> this.operations.set(key, cyclicValue)).isInstanceOf(ErrorCodeException.class);
    }

    private record Sample(String state, Instant occurredOn) {}
}
