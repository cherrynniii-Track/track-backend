package com.track.track.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.dto.dashboard.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 기반 캐시 사용을 위한 설정 클래스
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final ObjectMapper objectMapper;

    /**
     * Redis 캐시의 직렬화 방식과 만료 시간 관리하는 RedisCacheManager 생성
     * @param connectionFactory Redis 서버와의 연결을 제공하는 객체
     * @return 캐시별 설정이 적용된 RedisCacheManager
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 일반 캐시용 ObjectMapper
        ObjectMapper defaultRedisObjectMapper = objectMapper.copy();

        // JSON에 클래스 타입 정보를 포함해 역직렬화 시 원래 객체 타입 복원
        defaultRedisObjectMapper.activateDefaultTyping(
                defaultRedisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 일반 캐시 값을 JSON 형식으로 직렬화하고 역직렬화
        GenericJackson2JsonRedisSerializer defaultValueSerializer =
                new GenericJackson2JsonRedisSerializer(defaultRedisObjectMapper);

        // 별도 설정이 없는 캐시에 공통으로 적용할 기본 설정 생성
        RedisCacheConfiguration defaultConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        // 캐시 데이터는 저장된 시점부터 10분 동안 유지
                        .entryTtl(Duration.ofMinutes(10))
                        // null 반환값은 Redis에 저장하지 않는다
                        .disableCachingNullValues()
                        // 캐시 키를 문자열 형식으로 저장
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        // 캐시 값을 타입 정보가 포함된 JSON 형식으로 저장
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(defaultValueSerializer)
                        );

        // 대시보드 캐시용 ObjectMapper (default typing을 활성화하지 않음)
        ObjectMapper dashboardObjectMapper = objectMapper.copy();

        // 대시보드 캐시 값을 DashboardResponse 타입으로 직렬화하고 역직렬화
        Jackson2JsonRedisSerializer<DashboardResponse> dashboardSerializer =
                new Jackson2JsonRedisSerializer<>(
                        dashboardObjectMapper,
                        DashboardResponse.class
                );

        // dashboard 이름의 캐시에만 적용할 전용 설정 생성
        RedisCacheConfiguration dashboardConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        // DashboardResponse 전용 JSON Serializer로 저장
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(dashboardSerializer)
                        );

        // dashboard 캐시에만 전용 설정 적용
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withCacheConfiguration("dashboard", dashboardConfiguration)
                .build();
    }
}