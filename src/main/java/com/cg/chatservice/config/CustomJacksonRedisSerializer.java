package com.cg.chatservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Custom Redis serializer using ObjectMapper directly.
 * Replaces deprecated GenericJackson2JsonRedisSerializer
 * and Jackson2JsonRedisSerializer in Spring Boot 4.0+
 */
public class CustomJacksonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper objectMapper;

    public CustomJacksonRedisSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not serialize object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize JSON to object: " + e.getMessage(), e);
        }
    }
}