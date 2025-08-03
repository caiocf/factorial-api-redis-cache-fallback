package br.com.mkcf.config.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;


@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class RedisCacheCustomizer {
    // https://medium.com/javarevisited/caching-with-spring-boot-3-lettuce-and-redis-sentinel-5f6fab7e58f8


    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,CacheProperties cacheProperties) {
        RedisCacheConfiguration redisCacheConfiguration = createRedisCacheConfiguration(cacheProperties);

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();

        NoOpCacheManager fallbackCacheManager = new NoOpCacheManager();
        return new SafeFallbackCacheManager(redisCacheManager, fallbackCacheManager);
    }
    /*
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceCustomizer(@Value("${spring.data.redis.lettuce.command-timeout}" ) Duration commandTimeout,
                                                                         @Value("${spring.data.redis.lettuce.shutdown-timeout}" ) Duration shutdownTimeout,
                                                                         @Value("${spring.data.redis.lettuce.fixed-timeout}" ) Duration fixedTimeout) {

        return builder -> builder
                .commandTimeout(commandTimeout)
                .shutdownTimeout(shutdownTimeout)
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .timeoutOptions(TimeoutOptions.builder()
                                .fixedTimeout(fixedTimeout)
                                .build())
                        .build());
    }*/


    private RedisCacheConfiguration createRedisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new org.springframework.data.redis.serializer.StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProps = cacheProperties.getRedis();

        if (redisProps.getTimeToLive() != null) {
            config = config.entryTtl(redisProps.getTimeToLive());
        }

        if (Boolean.FALSE.equals(redisProps.isCacheNullValues())) {
            config = config.disableCachingNullValues();
        }

        if (Boolean.FALSE.equals(redisProps.isUseKeyPrefix())) {
            config = config.disableKeyPrefix();
        }
        if (Boolean.TRUE.equals(redisProps.isUseKeyPrefix()) && redisProps.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProps.getKeyPrefix());
        }

        return config;
    }
}

