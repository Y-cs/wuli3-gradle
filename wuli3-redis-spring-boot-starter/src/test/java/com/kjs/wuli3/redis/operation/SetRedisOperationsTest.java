package com.kjs.wuli3.redis.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kjs.wuli3.json.core.Jsons;
import com.kjs.wuli3.redis.RedisKey;
import com.kjs.wuli3.redis.codec.RedisCodec;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class SetRedisOperationsTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private RedisCodec codec;

    private SetRedisOperations operations;

    @BeforeEach
    void setUp() {
        when(this.redisTemplate.opsForSet()).thenReturn(this.setOperations);
        this.operations = new SetRedisOperations(this.redisTemplate);
    }

    @Test
    void addsAndRemovesJsonMembers() {
        final Duration timeToLive = Duration.ofMinutes(5);
        final RedisKey key = RedisKey.expiring("order:states", timeToLive);
        final Sample value = new Sample("created");
        when(this.setOperations.add(key.value(), Jsons.toJson(value))).thenReturn(1L);
        when(this.setOperations.remove(key.value(), Jsons.toJson(value))).thenReturn(1L);

        assertThat(this.operations.add(key, value)).isEqualTo(1L);
        assertThat(this.operations.remove(key, value)).isEqualTo(1L);

        verify(this.redisTemplate).expire(key.value(), timeToLive);
    }

    @Test
    void checksMembershipAndReadsMembers() {
        final RedisKey key = RedisKey.persistent("order:states");
        final Sample value = new Sample("created");
        when(this.setOperations.isMember(key.value(), Jsons.toJson(value))).thenReturn(Boolean.TRUE);
        when(this.setOperations.members(key.value()))
                .thenReturn(Set.of(Jsons.toJson(value)))
                .thenReturn(Set.of(Jsons.toJson(List.of(value))));

        assertThat(this.operations.contains(key, value)).isTrue();
        assertThat(this.operations.members(key, Sample.class)).containsExactly(value);
        assertThat(this.operations.members(key, new TypeReference<List<Sample>>() {}))
                .containsExactly(List.of(value));
    }

    @Test
    void ignoresJsonNullMembers() {
        final RedisKey key = RedisKey.persistent("order:states:null");
        when(this.setOperations.members(key.value())).thenReturn(Set.of("null"));

        assertThat(this.operations.members(key, Sample.class)).isEmpty();
    }

    @Test
    void reportsSetSize() {
        final RedisKey key = RedisKey.persistent("order:states");
        when(this.setOperations.size(key.value())).thenReturn(2L);

        assertThat(this.operations.size(key)).isEqualTo(2L);
    }

    @Test
    void usesTheConfiguredCodec() {
        final RedisKey key = RedisKey.persistent("order:states:codec");
        final Sample value = new Sample("created");
        when(this.codec.encode(value)).thenReturn("encoded-value");
        when(this.codec.decode("encoded-value", Sample.class)).thenReturn(value);
        final SetRedisOperations codecOperations = new SetRedisOperations(this.redisTemplate, this.codec);

        codecOperations.add(key, value);

        verify(this.setOperations).add(key.value(), "encoded-value");
        when(this.setOperations.members(key.value())).thenReturn(Set.of("encoded-value"));
        assertThat(codecOperations.members(key, Sample.class)).containsExactly(value);
    }

    private record Sample(String state) {}
}
