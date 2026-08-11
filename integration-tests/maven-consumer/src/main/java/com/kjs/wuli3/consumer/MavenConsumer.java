package com.kjs.wuli3.consumer;

import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.envelope.EventEnvelopeTemplate;
import com.kjs.wuli3.json.provider.JacksonProvider;
import com.kjs.wuli3.redis.RedisKey;
import com.kjs.wuli3.redis.RedisSupport;
import com.kjs.wuli3.redis.id.RedisMinuteIdGenerator;
import com.kjs.wuli3.redis.lock.RedisLockExecutor;
import com.kjs.wuli3.redis.lock.RedisLockRequest;
import com.kjs.wuli3.redis.operation.HashRedisOperations;
import com.kjs.wuli3.redis.operation.ObjectRedisOperations;
import com.kjs.wuli3.redis.operation.SetRedisOperations;
import com.kjs.wuli3.redis.operation.StringRedisOperations;
import java.time.Duration;
import java.time.Instant;

public final class MavenConsumer {
    private MavenConsumer() {}

    public static String createIdentifier() {
        JacksonProvider.newJsonMapper();
        final EventEnvelope<ConsumerEvent> envelope =
                EventEnvelopeTemplate.of("consumer-events", "consumer.event.v1").wrap(new ConsumerEvent(Instant.now()));
        envelope.eventId();
        EventPublisher.class.getName();
        RedisSupport.class.getName();
        RedisMinuteIdGenerator.class.getName();
        StringRedisOperations.class.getName();
        ObjectRedisOperations.class.getName();
        HashRedisOperations.class.getName();
        SetRedisOperations.class.getName();
        RedisLockExecutor.class.getName();
        RedisKey.expiring("consumer:test", Duration.ofMinutes(1));
        RedisLockRequest.watchdog("consumer:lock", Duration.ZERO);
        assertJavaClientPreviewDependencyIsNotPresent();
        return UuidStringIdGenerator.INSTANCE.nextId();
    }

    private static void assertJavaClientPreviewDependencyIsNotPresent() {
        try {
            Class.forName("org.apache.rocketmq.client.apis.ClientServiceProvider");
        } catch (ClassNotFoundException expected) {
            return;
        }
        throw new IllegalStateException("rocketmq-client-java must not be a published runtime dependency");
    }

    private record ConsumerEvent(Instant occurredOn) {}
}
