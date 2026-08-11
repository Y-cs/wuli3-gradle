package com.kjs.wuli3.redis.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.redis.RedisKey;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class StringRedisOperationsTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private StringRedisOperations operations;

    @BeforeEach
    void setUp() {
        when(this.redisTemplate.opsForValue()).thenReturn(this.valueOperations);
        this.operations = new StringRedisOperations(this.redisTemplate);
    }

    @Test
    void writesAndReadsPersistentValues() {
        final RedisKey key = RedisKey.persistent("counter:state");
        when(this.valueOperations.get(key.value())).thenReturn("ready", (String) null);

        this.operations.set(key, "ready");

        verify(this.valueOperations).set(key.value(), "ready");
        assertThat(this.operations.get(key)).contains("ready");
        assertThat(this.operations.get(key)).isEmpty();
    }

    @Test
    void writesExpiringValuesAndSupportsSetIfAbsent() {
        final Duration timeToLive = Duration.ofMinutes(5);
        final RedisKey key = RedisKey.expiring("counter:state", timeToLive);
        when(this.valueOperations.setIfAbsent(key.value(), "ready", timeToLive)).thenReturn(Boolean.TRUE);

        this.operations.set(key, "ready");

        verify(this.valueOperations).set(key.value(), "ready", timeToLive);
        assertThat(this.operations.setIfAbsent(key, "ready")).isTrue();
    }

    @Test
    void incrementsAndRefreshesExpiration() {
        final Duration timeToLive = Duration.ofMinutes(5);
        final RedisKey key = RedisKey.expiring("counter:value", timeToLive);
        when(this.valueOperations.increment(key.value(), 2L)).thenReturn(3L);

        assertThat(this.operations.increment(key, 2L)).isEqualTo(3L);

        verify(this.redisTemplate).expire(key.value(), timeToLive);
    }
}
