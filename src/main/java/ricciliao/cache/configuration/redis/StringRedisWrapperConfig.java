package ricciliao.cache.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ricciliao.cache.component.RedisCacheProvider;
import ricciliao.cache.component.StringRedisTemplateWrapper;
import ricciliao.cache.configuration.CacheProviderProps;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.RedisCacheDto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;

public abstract class StringRedisWrapperConfig {

    private final RedisCacheProvider cacheProvider;
    private final ObjectMapper objectMapper;
    protected final CacheProviderProps cacheProviderProps;

    protected StringRedisWrapperConfig(RedisCacheProvider cacheProvider,
                                    ObjectMapper objectMapper,
                                    CacheProviderProps applicationProperties) {
        this.cacheProvider = cacheProvider;
        this.objectMapper = objectMapper;
        this.cacheProviderProps = applicationProperties;
        this.createWrappers();
    }

    public abstract void createWrappers();

    public <T extends RedisCacheDto> void createWrapper(Class<T> tClass,
                                                        RedisPropsBo props) {
        cacheProvider.getProviderMap().put(
                props.identifier,
                new StringRedisTemplateWrapper(
                        createRedisTemplate(tClass, props),
                        props.ttl
                )
        );
        cacheProvider.getCacheClass().put(
                props.identifier,
                tClass
        );
    }

    private <T extends RedisCacheDto> RedisTemplate<String, RedisCacheDto> createRedisTemplate(Class<T> tClass,
                                                                                               RedisPropsBo props) {
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, tClass);
        GenericObjectPoolConfig<RedisStandaloneConfiguration> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(props.maxIdle);
        poolConfig.setMaxTotal(props.maxTotal);
        poolConfig.setMinIdle(props.minIdle);
        poolConfig.setMaxWait(props.timeout);

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(props.host, props.port);
        configuration.setDatabase(props.database);
        configuration.setPassword(props.password);

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(poolConfig);
        builder.commandTimeout(poolConfig.getMaxWaitDuration());
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, builder.build());
        connectionFactory.setValidateConnection(true);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, RedisCacheDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    public record RedisPropsBo(
            String host,
            int port,
            String password,
            int database,
            Duration timeout,
            Duration ttl,
            int minIdle,
            int maxIdle,
            int maxTotal,
            ConsumerIdentifierDto identifier
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 5585834914615699994L;
    }

}
