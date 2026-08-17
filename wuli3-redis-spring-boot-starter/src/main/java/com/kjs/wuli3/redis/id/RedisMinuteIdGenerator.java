package com.kjs.wuli3.redis.id;

import com.kjs.wuli3.core.id.IdGenerator;
import com.kjs.wuli3.redis.RedisSupport;
import com.kjs.wuli3.redis.error.RedisErrors;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/** 使用“UTC 分钟时间片 + Redis 分钟内序列”生成趋势递增的 Long ID。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RedisMinuteIdGenerator implements IdGenerator<Long> {

    public static final Duration DEFAULT_COUNTER_TTL = Duration.ofHours(1);
    public static final int DEFAULT_SEQUENCE_BITS = 22;

    private static final Duration MINIMUM_COUNTER_TTL = Duration.ofMinutes(2);
    private static final String KEY_PREFIX = "wuli3:id:";
    private static final RedisScript<Long> ALLOCATE_SEQUENCE_SCRIPT = new DefaultRedisScript<>("""
            local sequence = redis.call('INCR', KEYS[1])
            if redis.call('PTTL', KEYS[1]) < 0 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return sequence
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final String namespace;
    private final Clock clock;
    private final long counterTtlMillis;
    private final int sequenceBits;
    private final long maximumSequence;
    private final AtomicLong latestEpochMinute = new AtomicLong(-1L);

    /** 使用默认 24 小时计数器 TTL 和 22 位分钟内序列创建生成器。 */
    public RedisMinuteIdGenerator(final StringRedisTemplate redisTemplate, final String namespace) {
        this(redisTemplate, namespace, Clock.systemUTC(), DEFAULT_COUNTER_TTL, DEFAULT_SEQUENCE_BITS);
    }

    public RedisMinuteIdGenerator(final RedisSupport redisSupport, final String namespace) {
        this(redisSupport.redisTemplate(), namespace, Clock.systemUTC(), DEFAULT_COUNTER_TTL, DEFAULT_SEQUENCE_BITS);
    }

    /** 使用可配置时钟、计数器 TTL 和序列位数创建生成器。 */
    public RedisMinuteIdGenerator(
            final StringRedisTemplate redisTemplate,
            final String namespace,
            final Clock clock,
            final Duration counterTtl,
            final int sequenceBits) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.namespace = RedisMinuteIdGenerator.validateNamespace(namespace);
        this.clock = Objects.requireNonNull(clock, "clock");
        this.counterTtlMillis = RedisMinuteIdGenerator.validateCounterTtl(counterTtl);
        this.sequenceBits = RedisMinuteIdGenerator.validateSequenceBits(sequenceBits);
        this.maximumSequence = (1L << this.sequenceBits) - 1L;
    }

    /** 分配下一个 ID；当前分钟序列耗尽或检测到本地时钟回拨时拒绝生成。 */
    @Override
    public Long nextId() {
        final long epochMinute = this.currentEpochMinute();
        final long sequence = this.allocateSequence(epochMinute);
        if (sequence <= 0L || sequence > this.maximumSequence) {
            throw new RedisIdGenerationException(
                    RedisErrors.ID_SEQUENCE_EXHAUSTED,
                    "Redis ID 分钟内序列超出范围: namespace=" + this.namespace + ", epochMinute=" + epochMinute + ", sequence="
                            + sequence + ", maximumSequence=" + this.maximumSequence);
        }
        if (epochMinute > (Long.MAX_VALUE >> this.sequenceBits)) {
            throw new RedisIdGenerationException(
                    RedisErrors.ID_ALLOCATION_FAILED, "Redis ID 分钟时间片超出 Long 可编码范围: epochMinute=" + epochMinute);
        }
        return (epochMinute << this.sequenceBits) | sequence;
    }

    private long allocateSequence(final long epochMinute) {
        final String counterKey = KEY_PREFIX + "{" + this.namespace + "}:" + epochMinute;
        final Long sequence = this.redisTemplate.execute(
                ALLOCATE_SEQUENCE_SCRIPT, List.of(counterKey), Long.toString(this.counterTtlMillis));
        if (sequence == null) {
            throw new RedisIdGenerationException(
                    RedisErrors.ID_ALLOCATION_FAILED,
                    "Redis ID 分配未返回序列: namespace=" + this.namespace + ", epochMinute=" + epochMinute);
        }
        return sequence;
    }

    private long currentEpochMinute() {
        final Instant now = this.clock.instant();
        final long epochMinute = Math.floorDiv(now.getEpochSecond(), 60L);
        if (epochMinute < 0L) {
            throw new RedisIdGenerationException(
                    RedisErrors.ID_ALLOCATION_FAILED, "Redis ID 不支持 Unix epoch 之前的时间: " + now);
        }
        while (true) {
            final long latest = this.latestEpochMinute.get();
            if (epochMinute < latest) {
                throw new RedisIdGenerationException(
                        RedisErrors.ID_CLOCK_ROLLBACK,
                        "Redis ID 生成器检测到时钟回拨: namespace=" + this.namespace + ", latestEpochMinute=" + latest
                                + ", currentEpochMinute=" + epochMinute);
            }
            if (epochMinute == latest || this.latestEpochMinute.compareAndSet(latest, epochMinute)) {
                return epochMinute;
            }
        }
    }

    private static String validateNamespace(final String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("Redis ID namespace 不能为空");
        }
        if (!namespace.equals(namespace.trim())) {
            throw new IllegalArgumentException("Redis ID namespace 首尾不能包含空白字符");
        }
        if (namespace.indexOf('{') >= 0 || namespace.indexOf('}') >= 0) {
            throw new IllegalArgumentException("Redis ID namespace 不能包含大括号");
        }
        return namespace;
    }

    private static long validateCounterTtl(final Duration counterTtl) {
        Objects.requireNonNull(counterTtl, "counterTtl");
        if (counterTtl.compareTo(MINIMUM_COUNTER_TTL) < 0) {
            throw new IllegalArgumentException("Redis ID 分钟计数器 TTL 不能小于 2 分钟");
        }
        try {
            return counterTtl.toMillis();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Redis ID 分钟计数器 TTL 超出毫秒范围", exception);
        }
    }

    private static int validateSequenceBits(final int sequenceBits) {
        if (sequenceBits < 1 || sequenceBits > 30) {
            throw new IllegalArgumentException("Redis ID sequenceBits 必须在 1 到 30 之间");
        }
        return sequenceBits;
    }
}
