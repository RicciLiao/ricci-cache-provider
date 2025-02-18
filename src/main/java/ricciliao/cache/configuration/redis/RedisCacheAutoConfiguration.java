package ricciliao.cache.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import ricciliao.cache.component.StringRedisTemplateProvider;
import ricciliao.common.component.cache.pojo.CacheDto;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.provider.CacheProviderSelector;

import java.util.Objects;


@AutoConfiguration
@Conditional(RedisCacheAutoConfiguration.ConfigurationCondition.class)
@EnableConfigurationProperties({RedisCacheProperties.class})
public class RedisCacheAutoConfiguration {

    public RedisCacheAutoConfiguration(@Autowired ObjectMapper objectMapper,
                                       @Autowired RedisCacheProperties props,
                                       @Autowired CacheProviderSelector providerSelector) {
        for (RedisCacheProperties.ConsumerProperties consumerProps : props.getConsumerList()) {
            for (RedisCacheProperties.ConsumerProperties.StoreProperties storeProps : consumerProps.getStoreList()) {
                this.createWrapper(
                        new ConsumerIdentifierDto(consumerProps.getConsumer(), storeProps.getStore()),
                        objectMapper,
                        storeProps,
                        providerSelector
                );
            }
        }
    }

    public void createWrapper(ConsumerIdentifierDto identifier,
                              ObjectMapper objectMapper,
                              RedisCacheProperties.ConsumerProperties.StoreProperties props,
                              CacheProviderSelector providerSelector) {
        providerSelector.getCacheProviderMap().put(
                identifier,
                new StringRedisTemplateProvider(createRedisTemplate(objectMapper, props), props.getAddition().getTtl())
        );
        providerSelector.getCacheClass().put(identifier, props.getStoreClassName());
    }

    private RedisTemplate<String, CacheDto> createRedisTemplate(ObjectMapper objectMapper,
                                                                RedisCacheProperties.ConsumerProperties.StoreProperties props) {
        Jackson2JsonRedisSerializer<? extends CacheDto> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, props.getStoreClassName());
        GenericObjectPoolConfig<RedisStandaloneConfiguration> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(props.getAddition().getMaxIdle());
        poolConfig.setMaxTotal(props.getAddition().getMaxTotal());
        poolConfig.setMinIdle(props.getAddition().getMinIdle());
        poolConfig.setMaxWait(props.getAddition().getTimeout());

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(props.getHost(), props.getPort());
        configuration.setDatabase(props.getDatabase());
        configuration.setPassword(props.getPassword());

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(poolConfig);
        builder.commandTimeout(poolConfig.getMaxWaitDuration());
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, builder.build());
        connectionFactory.setValidateConnection(true);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, CacheDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    static class ConfigurationCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {

            return Objects.nonNull(context.getEnvironment().getProperty("cache-provider.redis.consumer-list[0].consumer"));
        }

    }

}
