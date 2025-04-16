package ricciliao.cache.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import ricciliao.cache.common.CacheConstants;
import ricciliao.x.cache.CacheQuery;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerOpBatchQueryDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class JedisProvider extends CacheProvider {

    private final JedisProviderConstruct constr;
    private final String upsertScript;
    private final Map<CacheQuery.Property, Field> query2FieldMap = new EnumMap<>(CacheQuery.Property.class);
    private static final String Q_NUMBER_LUA = " @%s: [%s %s] ";
    private static final String Q_TEXT_LUA = " @%s: %s ";

    public JedisProvider(JedisProviderConstruct jedisProviderConstruct) {
        super(jedisProviderConstruct);
        this.constr = jedisProviderConstruct;
        this.upsertScript = this.constr.jedisPooled.scriptLoad(this.constr.upsertLuaScript);
        Field[] fields = CacheDto.class.getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(CacheQuery.Support.class))
                .forEach(field -> {
                    CacheQuery.Support support = field.getAnnotation(CacheQuery.Support.class);
                    query2FieldMap.put(support.value(), field);
                });
    }

    @Override
    public boolean create(ConsumerOpDto.Single<CacheDto> operation) {
        try {

            return 1L ==
                    Long.parseLong(
                            this.constr.jedisPooled.evalsha(
                                    this.upsertScript,
                                    Collections.singletonList(this.buildRedisKey(operation.getData().getCacheKey())),
                                    Arrays.asList(
                                            this.constr.objectMapper.writeValueAsString(operation.getData()),
                                            String.valueOf(this.getStoreProps().getAddition().getTtl().getSeconds()),
                                            String.valueOf(1)
                                    )
                            ).toString()
                    );
        } catch (Exception e) {
            e.printStackTrace();
            //TODO

            return false;
        }
    }

    @Override
    public boolean update(ConsumerOpDto.Single<CacheDto> operation) {
        try {

            return 1L ==
                    Long.parseLong(
                            this.constr.jedisPooled.evalsha(
                                    this.upsertScript,
                                    Collections.singletonList(this.buildRedisKey(operation.getData().getCacheKey())),
                                    Arrays.asList(
                                            this.constr.objectMapper.writeValueAsString(operation.getData()),
                                            String.valueOf(this.getAdditionalProps().getTtl().getSeconds()),
                                            String.valueOf(0)
                                    )
                            ).toString()
                    );
        } catch (Exception e) {
            e.printStackTrace();
            //TODO

            return false;
        }
    }

    @Override
    public ConsumerOpDto.Single<CacheDto> get(String key) {

        return new ConsumerOpDto.Single<>(
                this.constr.objectMapper.convertValue(
                        this.constr.jedisPooled.jsonGet(this.buildRedisKey(key)),
                        this.getStoreProps().getStoreClassName()
                ),
                this.getStoreProps().getAddition().getTtl().toMillis()
        );
    }

    @Override
    public boolean delete(String key) {

        return this.constr.jedisPooled.jsonDel(this.buildRedisKey(key)) == 1L;
    }

    @Override
    public ConsumerOpDto.Batch<CacheDto> list(ConsumerOpBatchQueryDto query) {
        Query searchQ;
        if (MapUtils.isNotEmpty(query.getCriteriaMap())) {
            StringBuilder sbr = new StringBuilder();
            query.getCriteriaMap().entrySet()
                    .stream()
                    .filter(es -> query2FieldMap.containsKey(es.getKey()))
                    .forEach(es -> {
                        Field field = query2FieldMap.get(es.getKey());
                        if (Temporal.class.isAssignableFrom(field.getType())) {
                            sbr.append(String.format(Q_NUMBER_LUA, field.getName(), es.getValue(), es.getValue()));
                        } else if (String.class.isAssignableFrom(field.getType())) {
                            sbr.append(String.format(Q_TEXT_LUA, field.getName(), es.getValue()));
                        }
                    });
            searchQ = new Query(sbr.toString());
        } else {
            searchQ = new Query();
        }

        searchQ = searchQ.limit(0, Objects.nonNull(query.getLimit()) ? query.getLimit().intValue() : CacheConstants.DEFAULT_CACHE_OP_BATCH_LIMIT);

        if (Objects.nonNull(query.getSortBy())
                && Objects.nonNull(query.getSortDirection())
                && query2FieldMap.containsKey(query.getSortBy())) {
            searchQ.setSortBy(
                    query2FieldMap.get(query.getSortBy()).getName(),
                    CacheQuery.Sort.Direction.ASC.equals(query.getSortDirection())
            );
        }

        ConsumerOpDto.Batch<CacheDto> result = new ConsumerOpDto.Batch<>();
        result.setTtlOfMillis(this.constr.getStoreProps().getAddition().getTtl().toMillis());
        SearchResult sr = this.constr.jedisPooled.ftSearch(this.constr.indexName, searchQ);
        if (sr.getTotalResults() > 0) {
            try {
                for (Document document : sr.getDocuments()) {
                    result.getData().add(
                            this.constr.objectMapper.readValue(
                                    document.getString("$"),
                                    this.constr.getStoreProps().getStoreClassName()
                            )
                    );
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                //TODO
            }
        }

        return result;
    }

    @Override
    public ProviderInfoDto getProviderInfo() {

        return null;
    }

    private String buildRedisKey(String cacheKey) {

        return String.format(
                "%s:%s",
                this.constr.keyPrefix,
                cacheKey.replace("_", ":")
        );
    }

    public static class JedisProviderConstruct extends CacheProviderConstruct {
        private JedisPooled jedisPooled;
        private String keyPrefix;
        private ObjectMapper objectMapper;
        private String indexName;
        private String upsertLuaScript;

        public String getUpsertLuaScript() {
            return upsertLuaScript;
        }

        public void setUpsertLuaScript(String upsertLuaScript) {
            this.upsertLuaScript = upsertLuaScript;
        }

        public JedisPooled getJedisPooled() {
            return jedisPooled;
        }

        public void setJedisPooled(JedisPooled jedisPooled) {
            this.jedisPooled = jedisPooled;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public ObjectMapper getObjectMapper() {
            return objectMapper;
        }

        public void setObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }
    }

}
