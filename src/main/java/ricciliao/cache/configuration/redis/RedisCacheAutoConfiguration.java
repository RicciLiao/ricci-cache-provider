/*
package ricciliao.cache.configuration.redis;*/
package ricciliao.cache.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import redis.clients.jedis.Connection;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.component.JedisProvider;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.starter.PropsAutoConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@PropsAutoConfiguration(
        properties = RedisCacheAutoProperties.class,
        conditionalOnProperties = "cache-provider.redis.consumer-list[0].consumer"
)
public class RedisCacheAutoConfiguration {

    public static final String FT_RESULT_OK = "OK";

    public RedisCacheAutoConfiguration(@Autowired ObjectMapper objectMapper,
                                       @Autowired RedisCacheAutoProperties props,
                                       @Autowired CacheProviderSelector providerSelector) throws IOException {
        for (RedisCacheAutoProperties.ConsumerProperties consumerProps : props.getConsumerList()) {
            for (RedisCacheAutoProperties.ConsumerProperties.StoreProperties storeProps : consumerProps.getStoreList()) {
                this.createWrapper(
                        new ConsumerIdentifier(consumerProps.getConsumer(), storeProps.getStore()),
                        objectMapper,
                        storeProps,
                        providerSelector
                );
            }
        }
    }

    private void createWrapper(ConsumerIdentifier identifier,
                               ObjectMapper objectMapper,
                               RedisCacheAutoProperties.ConsumerProperties.StoreProperties props,
                               CacheProviderSelector providerSelector) throws IOException {
        String keyPrefix = "db" + props.getDatabase();
        String indexName = keyPrefix + "_" + props.getStore() + "_index";
        String upsertLua = this.loadLuaScript("redis_upsert.lua");
        JedisPooled jedisPool = this.jedisPool(props);

        JedisProvider.JedisProviderConstruct construct = new JedisProvider.JedisProviderConstruct();
        construct.setUpsertLuaScript(upsertLua);
        construct.setJedisPooled(this.jedisPool(props));
        construct.setKeyPrefix(keyPrefix);
        construct.setObjectMapper(objectMapper);
        construct.setIndexName(indexName);
        construct.setConsumerIdentifier(identifier);
        construct.setStoreProps(props);

        providerSelector.getCacheProviderMap().put(identifier, new JedisProvider(construct));
        providerSelector.getCacheStaticalMap().put(identifier, props.getAddition().getStatical());
        this.createIndex(jedisPool, indexName, keyPrefix);
    }

    private JedisPooled jedisPool(RedisCacheAutoProperties.ConsumerProperties.StoreProperties props) {
        HostAndPort hostAndPort = new HostAndPort(props.getHost(), props.getPort());

        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(props.getAddition().getMaxIdle());
        poolConfig.setMaxTotal(props.getAddition().getMaxTotal());
        poolConfig.setMinIdle(props.getAddition().getMinIdle());
        poolConfig.setMaxWait(props.getAddition().getTimeout());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        JedisClientConfig clientConfig =
                DefaultJedisClientConfig
                        .builder()
                        .connectionTimeoutMillis((int) poolConfig.getMaxWaitDuration().toMillis())
                        .socketTimeoutMillis((int) poolConfig.getMaxWaitDuration().toMillis())
                        .password(props.getPassword())
                        .database(0)
                        .build();

        return new JedisPooled(poolConfig, hostAndPort, clientConfig);
    }

    public boolean createIndex(JedisPooled jedisPool,
                               String indexName,
                               String keyPrefix) {
        if (this.indexExists(indexName, jedisPool)) {

            return true;
        }
        Schema.Field cacheKey = new Schema.Field("$.cacheKey", Schema.FieldType.TEXT, true);
        cacheKey.as("cacheKey");
        Schema.Field createdDtm = new Schema.Field("$.createdDtm", Schema.FieldType.NUMERIC, true);
        createdDtm.as("createdDtm");
        Schema.Field updatedDtm = new Schema.Field("$.updatedDtm", Schema.FieldType.NUMERIC, true);
        updatedDtm.as("updatedDtm");

        String result = jedisPool.ftCreate(
                indexName,
                IndexOptions.defaultOptions().setDefinition(new IndexDefinition(IndexDefinition.Type.JSON).setPrefixes(keyPrefix)),
                Schema.from(cacheKey, createdDtm, updatedDtm)
        );

        return FT_RESULT_OK.equalsIgnoreCase(result);
    }

    public boolean indexExists(String indexName, JedisPooled jedisPool) {

        return CollectionUtils.isNotEmpty(
                jedisPool.ftList()
                        .stream()
                        .filter(index -> index.equalsIgnoreCase(indexName))
                        .toList()
        );
    }

    public String loadLuaScript(String fileName) throws IOException {

        return Files.readString(Paths.get(ResourceUtils.getFile("classpath:" + fileName).getPath()));
    }

}

