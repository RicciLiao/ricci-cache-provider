package ricciliao.cache.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.component.StringRedisTemplateProvider;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ProviderInfoDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.starter.PropsAutoConfiguration;

@PropsAutoConfiguration(
        properties = RedisCacheAutoProperties.class,
        conditionProperties = "cache-provider.redis.consumer-list[0].consumer"
)
public class RedisCacheAutoConfiguration {

    public RedisCacheAutoConfiguration(@Autowired ObjectMapper objectMapper,
                                       @Autowired RedisCacheAutoProperties props,
                                       @Autowired CacheProviderSelector providerSelector) {
        for (RedisCacheAutoProperties.ConsumerProperties consumerProps : props.getConsumerList()) {
            for (RedisCacheAutoProperties.ConsumerProperties.StoreProperties storeProps : consumerProps.getStoreList()) {
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
                              RedisCacheAutoProperties.ConsumerProperties.StoreProperties props,
                              CacheProviderSelector providerSelector) {
        providerSelector.getCacheProviderMap().put(
                identifier,
                new StringRedisTemplateProvider(
                        identifier,
                        props,
                        redisTemplate(objectMapper, props)
                )
        );
        providerSelector.getCacheClass().put(identifier, props.getStoreClassName());
    }

    private RedisTemplate<String, CacheDto> redisTemplate(ObjectMapper objectMapper,
                                                          RedisCacheAutoProperties.ConsumerProperties.StoreProperties props) {
        Jackson2JsonRedisSerializer<? extends CacheDto> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, props.getStoreClassName());

        RedisTemplate<String, CacheDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory(props, false));
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    private LettuceConnectionFactory lettuceConnectionFactory(RedisCacheAutoProperties.ConsumerProperties.StoreProperties props, boolean info) {

        GenericObjectPoolConfig<RedisStandaloneConfiguration> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(props.getAddition().getMaxIdle());
        poolConfig.setMaxTotal(props.getAddition().getMaxTotal());
        poolConfig.setMinIdle(props.getAddition().getMinIdle());
        poolConfig.setMaxWait(props.getAddition().getTimeout());

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(props.getHost(), props.getPort());
        configuration.setDatabase(info ? 0 : props.getDatabase());
        configuration.setPassword(props.getPassword());

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(poolConfig);
        builder.commandTimeout(poolConfig.getMaxWaitDuration());
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, builder.build());
        connectionFactory.setValidateConnection(true);
        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }

}
