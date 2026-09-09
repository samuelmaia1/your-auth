package com.samuelmaia1_github.yourauth.infra.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.PlanCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.ProjectApiKeyCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.UserCacheNames;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.KotlinDetector;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.ClassUtils;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;

import java.time.Duration;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            CacheProperties cacheProperties
    ) {
        RedisSerializer<String> keySerializer =
                RedisSerializer.string();

        RedisSerializer<Object> valueSerializer =
                valueSerializer();

        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(keySerializer)
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(valueSerializer)
                        );

        return applyRedisProperties(
                cacheConfiguration,
                cacheProperties.getRedis()
        );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration cacheConfiguration
    ) {
        return builder -> builder
                .transactionAware()
                .withInitialCacheConfigurations(customCacheConfigurations(cacheConfiguration));
    }

    Map<String, RedisCacheConfiguration> customCacheConfigurations(
            RedisCacheConfiguration cacheConfiguration
    ) {
        return Map.of(
                PlanCacheNames.PLANS_CACHE,
                cacheConfiguration.entryTtl(Duration.ofHours(24)),
                AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID,
                cacheConfiguration.entryTtl(Duration.ofMinutes(1)),
                AccountCacheNames.ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID,
                cacheConfiguration.entryTtl(Duration.ofMinutes(1)),
                UserCacheNames.USER_BY_ID,
                cacheConfiguration.entryTtl(Duration.ofMinutes(2)),
                UserCacheNames.USERS_BY_PROJECT_ID,
                cacheConfiguration.entryTtl(Duration.ofMinutes(2)),
                ProjectApiKeyCacheNames.PROJECT_API_KEY_BY_ID,
                cacheConfiguration.entryTtl(Duration.ofMinutes(2)),
                ProjectApiKeyCacheNames.PROJECT_API_KEYS_BY_PROJECT_ID,
                cacheConfiguration.entryTtl(Duration.ofMinutes(2))
        );
    }

    private RedisCacheConfiguration applyRedisProperties(
            RedisCacheConfiguration cacheConfiguration,
            CacheProperties.Redis redisProperties
    ) {
        RedisCacheConfiguration configuredCache = cacheConfiguration;

        if (redisProperties.getTimeToLive() != null) {
            configuredCache = configuredCache.entryTtl(
                    redisProperties.getTimeToLive()
            );
        }

        if (redisProperties.getKeyPrefix() != null) {
            configuredCache = configuredCache.prefixCacheNameWith(
                    redisProperties.getKeyPrefix()
            );
        }

        if (!redisProperties.isCacheNullValues()) {
            configuredCache = configuredCache.disableCachingNullValues();
        }

        if (!redisProperties.isUseKeyPrefix()) {
            configuredCache = configuredCache.disableKeyPrefix();
        }

        return configuredCache;
    }

    RedisSerializer<Object> valueSerializer() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false
                )
                .setDefaultTyping(
                        new RedisCacheTypeResolverBuilder(typeValidator())
                )
                .build();

        return new GenericJacksonJsonRedisSerializer(objectMapper);
    }

    private PolymorphicTypeValidator typeValidator() {
        return BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.samuelmaia1_github.yourauth")
                .allowIfSubType("java.util")
                .allowIfSubType("org.springframework.data.domain")
                .build();
    }

    private static class RedisCacheTypeResolverBuilder
            extends DefaultTypeResolverBuilder {

        RedisCacheTypeResolverBuilder(
                PolymorphicTypeValidator subtypeValidator
        ) {
            super(
                    subtypeValidator,
                    DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.WRAPPER_ARRAY
            );
        }

        @Override
        public DefaultTypeResolverBuilder withDefaultImpl(
                Class<?> defaultImpl
        ) {
            return this;
        }

        @Override
        public boolean useForType(JavaType javaType) {
            if (javaType.isPrimitive()) {
                return false;
            }

            if (javaType.isJavaLangObject()) {
                return true;
            }

            JavaType resolvedType = resolveArrayOrReference(javaType);
            Class<?> rawClass = resolvedType.getRawClass();

            if (resolvedType.isEnumType()
                    || ClassUtils.isPrimitiveOrWrapper(rawClass)
                    || CharSequence.class.isAssignableFrom(rawClass)
                    || TreeNode.class.isAssignableFrom(rawClass)) {
                return false;
            }

            String packageName = rawClass.getPackageName();

            if (packageName.startsWith("com.samuelmaia1_github.yourauth")
                    || packageName.startsWith("org.springframework.data.domain")) {
                return true;
            }

            if (java.util.Collection.class.isAssignableFrom(rawClass)
                    || java.util.Map.class.isAssignableFrom(rawClass)) {
                return true;
            }

            return !resolvedType.isFinal()
                    || KotlinDetector.isKotlinType(rawClass);
        }

        private JavaType resolveArrayOrReference(JavaType type) {
            JavaType resolvedType = type;

            while (resolvedType.isArrayType()) {
                resolvedType = resolvedType.getContentType();
                if (resolvedType.isReferenceType()) {
                    resolvedType = resolveArrayOrReference(resolvedType);
                }
            }

            while (resolvedType.isReferenceType()) {
                resolvedType = resolvedType.getReferencedType();
                if (resolvedType.isArrayType()) {
                    resolvedType = resolveArrayOrReference(resolvedType);
                }
            }

            return resolvedType;
        }
    }
}
