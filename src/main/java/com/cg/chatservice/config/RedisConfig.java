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


    private ObjectMapper buildRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Default typing adds @class info so Redis can reconstruct
        // the correct concrete type when deserializing cached objects
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }


    @Bean
    public CustomJacksonRedisSerializer customJacksonRedisSerializer() {
        return new CustomJacksonRedisSerializer(buildRedisObjectMapper());
    }


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

    
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            CustomJacksonRedisSerializer serializer) {

        return RedisCacheManager.builder(factory)
                .cacheDefaults(buildCacheConfig(SESSION_TTL, serializer))
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.SESSIONS, buildCacheConfig(SESSION_TTL, serializer),
                        CacheNames.MESSAGES, buildCacheConfig(MESSAGES_TTL, serializer)
                ))
                .transactionAware()
                .build();
    }

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