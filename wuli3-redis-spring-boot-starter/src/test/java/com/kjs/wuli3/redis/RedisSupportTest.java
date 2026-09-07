package com.kjs.wuli3.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.redis.codec.JsonRedisCodec;
import com.kjs.wuli3.redis.codec.RedisCodec;
import com.kjs.wuli3.redis.operation.HashRedisOperations;
import com.kjs.wuli3.redis.operation.ObjectRedisOperations;
import com.kjs.wuli3.redis.operation.SetRedisOperations;
import com.kjs.wuli3.redis.operation.StringRedisOperations;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisSupportTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisSupport redisSupport;

    @BeforeEach
    void setUp() {
        this.redisSupport = new RedisSupport(this.redisTemplate);
    }

    @Test
    void exposesAllDataStructureOperations() {
        assertThat(this.redisSupport.stringOperations()).isInstanceOf(StringRedisOperations.class);
        assertThat(this.redisSupport.objectOperations()).isInstanceOf(ObjectRedisOperations.class);
        assertThat(this.redisSupport.hashOperations()).isInstanceOf(HashRedisOperations.class);
        assertThat(this.redisSupport.setOperations()).isInstanceOf(SetRedisOperations.class);
    }

    @Test
    void propagatesTheConfiguredCodecToStructuredOperations() {
        final RedisCodec codec = org.mockito.Mockito.mock(RedisCodec.class);
        when(this.redisTemplate.opsForValue()).thenReturn(this.valueOperations);
        final RedisSupport support = new RedisSupport(this.redisTemplate, codec);

        assertThat(support.codec()).isSameAs(codec);
        assertThat(support.objectOperations()).isNotNull();
        assertThat(support.hashOperations()).isNotNull();
        assertThat(support.setOperations()).isNotNull();

        when(codec.encode("value")).thenReturn("encoded-value");
        support.objectOperations().set(RedisKey.persistent("codec:key"), "value");
        verify(codec).encode("value");
        verify(this.valueOperations).set("codec:key", "encoded-value");
    }

    @Test
    void usesJsonCodecByDefault() {
        assertThat(this.redisSupport.codec()).isSameAs(JsonRedisCodec.INSTANCE);
    }

    @Test
    void deletesSingleAndMultipleKeys() {
        final RedisKey first = RedisKey.persistent("orders:1");
        final RedisKey second = RedisKey.persistent("orders:2");
        when(this.redisTemplate.delete(first.value())).thenReturn(Boolean.TRUE);
        when(this.redisTemplate.delete(List.of(first.value(), second.value()))).thenReturn(2L);

        assertThat(this.redisSupport.delete(first)).isTrue();
        assertThat(this.redisSupport.delete(List.of(first, second))).isEqualTo(2L);
    }

    @Test
    void checksExistenceAndUpdatesExpiration() {
        final Duration timeToLive = Duration.ofMinutes(5);
        final RedisKey key = RedisKey.expiring("orders:1", timeToLive);
        when(this.redisTemplate.hasKey(key.value())).thenReturn(Boolean.TRUE);
        when(this.redisTemplate.expire(key.value(), timeToLive)).thenReturn(Boolean.TRUE);

        assertThat(this.redisSupport.exists(key)).isTrue();
        assertThat(this.redisSupport.expire(key)).isTrue();
        assertThat(this.redisSupport.expire(key.value(), timeToLive)).isTrue();

        verify(this.redisTemplate, org.mockito.Mockito.times(2)).expire(key.value(), timeToLive);
    }

    @Test
    void rejectsExpiringPersistentKeys() {
        assertThatThrownBy(() -> this.redisSupport.expire(RedisKey.persistent("orders:1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
