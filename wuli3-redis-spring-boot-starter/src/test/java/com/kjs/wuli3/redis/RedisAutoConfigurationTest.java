package com.kjs.wuli3.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.redis.autoconfigure.RedisAutoConfiguration;
import com.kjs.wuli3.redis.autoconfigure.RedisProperties;
import com.kjs.wuli3.redis.lock.RedisLockExecutor;
import com.kjs.wuli3.redis.operation.HashRedisOperations;
import com.kjs.wuli3.redis.operation.ObjectRedisOperations;
import com.kjs.wuli3.redis.operation.SetRedisOperations;
import com.kjs.wuli3.redis.operation.StringRedisOperations;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class));

    @Test
    void createsDefaultInfrastructureBeansWhenClientsAreAvailable() {
        this.contextRunner
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisProperties.class);
                    assertThat(context).hasSingleBean(RedisSupport.class);
                    assertThat(context).doesNotHaveBean(StringRedisOperations.class);
                    assertThat(context).doesNotHaveBean(ObjectRedisOperations.class);
                    assertThat(context).doesNotHaveBean(HashRedisOperations.class);
                    assertThat(context).doesNotHaveBean(SetRedisOperations.class);
                    final RedisSupport redisSupport = context.getBean(RedisSupport.class);
                    assertThat(redisSupport.stringOperations()).isInstanceOf(StringRedisOperations.class);
                    assertThat(redisSupport.objectOperations()).isInstanceOf(ObjectRedisOperations.class);
                    assertThat(redisSupport.hashOperations()).isInstanceOf(HashRedisOperations.class);
                    assertThat(redisSupport.setOperations()).isInstanceOf(SetRedisOperations.class);
                    assertThat(context).hasSingleBean(RedisLockExecutor.class);
                });
    }

    @Test
    void doesNotCreateInfrastructureBeansWithoutRequiredClients() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RedisProperties.class);
            assertThat(context).doesNotHaveBean(RedisSupport.class);
            assertThat(context).doesNotHaveBean(StringRedisOperations.class);
            assertThat(context).doesNotHaveBean(ObjectRedisOperations.class);
            assertThat(context).doesNotHaveBean(HashRedisOperations.class);
            assertThat(context).doesNotHaveBean(SetRedisOperations.class);
            assertThat(context).doesNotHaveBean(RedisLockExecutor.class);
        });
    }

    @Test
    void allowsOperationsAndLockFeaturesToBeDisabledIndependently() {
        this.contextRunner
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .withPropertyValues("wuli3.redis.operations.enabled=false", "wuli3.redis.lock.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RedisSupport.class);
                    assertThat(context).doesNotHaveBean(StringRedisOperations.class);
                    assertThat(context).doesNotHaveBean(ObjectRedisOperations.class);
                    assertThat(context).doesNotHaveBean(HashRedisOperations.class);
                    assertThat(context).doesNotHaveBean(SetRedisOperations.class);
                    assertThat(context).doesNotHaveBean(RedisLockExecutor.class);
                    final RedisProperties properties = context.getBean(RedisProperties.class);
                    assertThat(properties.getOperations().isEnabled()).isFalse();
                    assertThat(properties.getLock().isEnabled()).isFalse();
                });
    }

    @Test
    void backsOffForApplicationProvidedBeans() {
        final RedisSupport redisSupport = mock(RedisSupport.class);
        final RedisLockExecutor lockExecutor = mock(RedisLockExecutor.class);
        this.contextRunner
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .withBean(RedisSupport.class, () -> redisSupport)
                .withBean(RedisLockExecutor.class, () -> lockExecutor)
                .run(context -> {
                    assertThat(context).getBean(RedisSupport.class).isSameAs(redisSupport);
                    assertThat(context).getBean(RedisLockExecutor.class).isSameAs(lockExecutor);
                });
    }
}
