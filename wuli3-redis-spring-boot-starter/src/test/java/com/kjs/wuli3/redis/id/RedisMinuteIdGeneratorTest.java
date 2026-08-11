package com.kjs.wuli3.redis.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.redis.error.RedisErrors;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class RedisMinuteIdGeneratorTest {

    private static final Instant FIRST_MINUTE = Instant.parse("2026-08-11T00:00:30Z");

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private Clock clock;

    @Test
    void combinesEpochMinuteAndRedisSequence() {
        when(this.redisTemplate.execute(
                        org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(1L, 2L);
        final RedisMinuteIdGenerator generator = new RedisMinuteIdGenerator(
                this.redisTemplate, "order", Clock.fixed(FIRST_MINUTE, ZoneOffset.UTC), Duration.ofHours(24), 22);
        final long epochMinute = Math.floorDiv(FIRST_MINUTE.getEpochSecond(), 60L);

        assertThat(generator.nextId()).isEqualTo((epochMinute << 22) | 1L);
        assertThat(generator.nextId()).isEqualTo((epochMinute << 22) | 2L);
    }

    @Test
    void usesMinuteScopedCounterKeyAndAtomicTtlScript() {
        when(this.redisTemplate.execute(
                        org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(1L);
        final RedisMinuteIdGenerator generator = new RedisMinuteIdGenerator(
                this.redisTemplate, "order", Clock.fixed(FIRST_MINUTE, ZoneOffset.UTC), Duration.ofHours(24), 22);
        final long epochMinute = Math.floorDiv(FIRST_MINUTE.getEpochSecond(), 60L);

        generator.nextId();

        final ArgumentCaptor<List<String>> keysCaptor = listCaptor();
        final ArgumentCaptor<Object[]> argumentsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(this.redisTemplate)
                .execute(
                        org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                        keysCaptor.capture(),
                        argumentsCaptor.capture());
        assertThat(keysCaptor.getValue()).containsExactly("wuli3:id:{order}:" + epochMinute);
        assertThat(argumentsCaptor.getValue()).containsExactly("86400000");
    }

    @Test
    void separatesEqualSequencesFromDifferentMinutes() {
        when(this.clock.instant()).thenReturn(FIRST_MINUTE, FIRST_MINUTE.plus(Duration.ofMinutes(1)));
        when(this.redisTemplate.execute(
                        org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(1L, 1L);
        final RedisMinuteIdGenerator generator =
                new RedisMinuteIdGenerator(this.redisTemplate, "order", this.clock, Duration.ofHours(24), 22);

        final Long first = generator.nextId();
        final Long second = generator.nextId();

        assertThat(second).isEqualTo(first + (1L << 22));
    }

    @Test
    void rejectsSequenceExhaustion() {
        when(this.redisTemplate.execute(
                        org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(4L);
        final RedisMinuteIdGenerator generator = new RedisMinuteIdGenerator(
                this.redisTemplate, "order", Clock.fixed(FIRST_MINUTE, ZoneOffset.UTC), Duration.ofHours(24), 2);

        assertThatThrownBy(generator::nextId)
                .isInstanceOfSatisfying(
                        RedisIdGenerationException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RedisErrors.ID_SEQUENCE_EXHAUSTED));
    }

    @Test
    void rejectsClockRollbackWithinGeneratorInstance() {
        when(this.clock.instant()).thenReturn(FIRST_MINUTE, FIRST_MINUTE.minus(Duration.ofMinutes(1)));
        when(this.redisTemplate.execute(
                        org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(1L);
        final RedisMinuteIdGenerator generator =
                new RedisMinuteIdGenerator(this.redisTemplate, "order", this.clock, Duration.ofHours(24), 22);

        generator.nextId();

        assertThatThrownBy(generator::nextId)
                .isInstanceOfSatisfying(
                        RedisIdGenerationException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RedisErrors.ID_CLOCK_ROLLBACK));
    }

    @Test
    void rejectsMissingRedisResult() {
        final RedisMinuteIdGenerator generator = new RedisMinuteIdGenerator(
                this.redisTemplate, "order", Clock.fixed(FIRST_MINUTE, ZoneOffset.UTC), Duration.ofHours(24), 22);

        assertThatThrownBy(generator::nextId)
                .isInstanceOfSatisfying(
                        RedisIdGenerationException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RedisErrors.ID_ALLOCATION_FAILED));
    }

    @Test
    void validatesNamespaceTtlAndSequenceBits() {
        assertThatThrownBy(() -> new RedisMinuteIdGenerator(this.redisTemplate, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RedisMinuteIdGenerator(
                        this.redisTemplate, "order}", Clock.systemUTC(), Duration.ofHours(24), 22))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RedisMinuteIdGenerator(
                        this.redisTemplate, "order", Clock.systemUTC(), Duration.ofMinutes(1), 22))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RedisMinuteIdGenerator(
                        this.redisTemplate, "order", Clock.systemUTC(), Duration.ofHours(24), 31))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<String>> listCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
