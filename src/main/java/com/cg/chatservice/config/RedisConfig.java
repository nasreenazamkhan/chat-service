package com.cg.chatservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Duration SESSION_TTL = Duration.ofHours(1);
    private static final Duration MESSAGES_TTL = Duration.ofMinutes(30);

    /**
     * ObjectMapper configured for Redis serialization.
     * - JavaTimeModule handles LocalDateTime serialization
     * - Default typing preserves concrete types when deserializing
     * - WRITE_DATES_AS_TIMESTAMPS disabled for ISO-8601 date strings
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    /**
     * Custom serializer — no deprecated classes.
     * Directly implements RedisSerializer<Object> using ObjectMapper.
     */
    @Bean
    public CustomJacksonRedisSerializer customJacksonRedisSerializer(
            ObjectMapper redisObjectMapper) {
        return new CustomJacksonRedisSerializer(redisObjectMapper);
    }

    /**
     * General-purpose RedisTemplate with String keys and JSON values.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            CustomJacksonRedisSerializer serializer) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setDefaultSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * CacheManager with per-cache TTL configuration.
     * - SESSIONS cache : 1 hour TTL
     * - MESSAGES cache : 30 minutes TTL
     */
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            CustomJacksonRedisSerializer serializer) {

        RedisCacheConfiguration defaultConfig = buildCacheConfig(SESSION_TTL, serializer);

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.SESSIONS, buildCacheConfig(SESSION_TTL, serializer),
                        CacheNames.MESSAGES, buildCacheConfig(MESSAGES_TTL, serializer)
                ))
                .transactionAware()
                .build();
    }

    /**
     * Builds a RedisCacheConfiguration with the given TTL and serializer.
     */
    private RedisCacheConfiguration buildCacheConfig(
            Duration ttl,
            RedisSerializer<Object> serializer) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer));
    }
}