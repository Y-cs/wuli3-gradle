package com.kjs.wuli3.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class BomConsumerTest {
    @Test
    void resolvesAndUsesVersionlessComponents() {
        assertNotNull(UuidStringIdGenerator.INSTANCE.nextId());
        assertNotNull(JacksonProvider.newJsonMapper());
        final EventEnvelope<ConsumerEvent> envelope =
                EventEnvelopeTemplate.of("consumer-events", "consumer.event.v1").wrap(new ConsumerEvent(Instant.now()));
        assertNotNull(envelope.eventId());
        assertNotNull(EventPublisher.class);
        assertNotNull(RabbitTemplate.class);
        assertNotNull(RedisSupport.class);
        assertNotNull(RedisMinuteIdGenerator.class);
        assertNotNull(StringRedisOperations.class);
        assertNotNull(ObjectRedisOperations.class);
        assertNotNull(HashRedisOperations.class);
        assertNotNull(SetRedisOperations.class);
        assertNotNull(RedisLockExecutor.class);
        assertNotNull(RedisKey.expiring("consumer:test", Duration.ofMinutes(1)));
        assertNotNull(RedisLockRequest.watchdog("consumer:lock", Duration.ZERO));
        org.junit.jupiter.api.Assertions.assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("org.apache.rocketmq.client.apis.ClientServiceProvider"));
    }

    private record ConsumerEvent(Instant occurredOn) {}
}
