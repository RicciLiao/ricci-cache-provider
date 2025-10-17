package ricciliao.cache.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import ricciliao.cache.common.CacheConstants;
import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.cache.query.CacheBatchQuery;
import ricciliao.x.cache.query.CacheQuery;
import ricciliao.x.log.api.XLogger;
import ricciliao.x.log.api.XLoggerFactory;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JedisProvider extends AbstractCacheProvider {

    private static final XLogger logger = XLoggerFactory.getLogger(JedisProvider.class);
    private static final String Q_NUMBER_LUA = " @%s: [%s %s] ";
    private static final String Q_TEXT_LUA = " @%s: %s ";
    private static final String OP_FAILED = "Cannot operate cache for {}";
    private final JedisProviderConstruct constr;
    private final String upsertScript;

    public JedisProvider(JedisProviderConstruct jedisProviderConstruct) {
        super(jedisProviderConstruct);
        this.constr = jedisProviderConstruct;
        this.upsertScript = this.constr.jedisPooled.scriptLoad(this.constr.upsertLuaScript);
    }

    @Override
    public boolean create(ProviderCacheStore store) {
        try {

            return 1L ==
                    Long.parseLong(
                            this.constr.jedisPooled.evalsha(
                                    this.upsertScript,
                                    Collections.singletonList(this.buildRedisKey(store.getCacheKey())),
                                    Arrays.asList(
                                            this.constr.objectMapper.writeValueAsString(store),
                                            String.valueOf(this.getStoreProps().getAddition().getTtl().getSeconds()),
                                            String.valueOf(1)
                                    )
                            ).toString()
                    );
        } catch (Exception e) {
            logger.error(OP_FAILED, this.getConsumerIdentifier(), e);

            return false;
        }
    }

    @Override
    public boolean update(ProviderCacheStore store) {
        try {

            return 1L ==
                    Long.parseLong(
                            this.constr.jedisPooled.evalsha(
                                    this.upsertScript,
                                    Collections.singletonList(this.buildRedisKey(store.getCacheKey())),
                                    Arrays.asList(
                                            this.constr.objectMapper.writeValueAsString(store),
                                            String.valueOf(this.getStoreProps().getAddition().getTtl().getSeconds()),
                                            String.valueOf(0)
                                    )
                            ).toString()
                    );
        } catch (Exception e) {
            logger.error(OP_FAILED, this.getConsumerIdentifier(), e);

            return false;
        }
    }

    @Override
    public ProviderCacheStore get(String key) {

        return this.constr.objectMapper.convertValue(
                this.constr.jedisPooled.jsonGet(this.buildRedisKey(key)),
                ProviderCacheStore.class
        );
    }

    @Override
    public boolean delete(String key) {

        return this.constr.jedisPooled.del(this.buildRedisKey(key)) == 1L;
    }

    @Override
    public ProviderCacheStore.Batch list(CacheBatchQuery query) {
        SearchResult sr = this.constr.jedisPooled.ftSearch(this.constr.indexName, this.toQuery(query));
        if (sr.getTotalResults() == 0) {

            return new ProviderCacheStore.Batch(Collections.emptyList());
        }

        List<ProviderCacheStore> storeList = new ArrayList<>(Math.toIntExact(sr.getTotalResults()));
        try {
            for (Document document : sr.getDocuments()) {
                storeList.add(this.constr.objectMapper.readValue(document.getString("$"), ProviderCacheStore.class));
            }
        } catch (JsonProcessingException e) {
            logger.error(OP_FAILED, this.getConsumerIdentifier(), e);
        }

        return new ProviderCacheStore.Batch(storeList);
    }

    @Override
    public boolean delete(CacheBatchQuery query) {
        boolean finish = false;
        while (!finish) {
            ProviderCacheStore.Batch batch = this.list(query);
            if (CollectionUtils.isNotEmpty(batch.batch())) {
                this.constr.jedisPooled.del(
                        batch.batch()
                                .stream()
                                .map(dto -> this.buildRedisKey(dto.getCacheKey()))
                                .toArray(String[]::new)
                );
            } else {
                finish = true;
            }
        }

        return true;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        ProviderInfo result = new ProviderInfo(this.getConsumerIdentifier());
        long count = this.constr.jedisPooled
                .ftSearch(
                        this.constr.indexName,
                        new Query("*").limit(0, 0)
                )
                .getTotalResults();
        result.setCount(count);
        if (count > 0) {
            CacheBatchQuery query = new CacheBatchQuery();
            query.setSortBy(CacheQuery.Property.UPDATED_DTM);
            query.setSortDirection(CacheQuery.Sort.Direction.DESC);
            query.setLimit(1L);

            ProviderCacheStore.Batch batch = this.list(query);
            result.setCreatedDtm(batch.batch().getFirst().getCreatedDtm());
            result.setMaxUpdatedDtm(batch.batch().getFirst().getUpdatedDtm());
        }

        return result;
    }

    protected Query toQuery(CacheBatchQuery query) {
        Query searchQ;
        if (MapUtils.isNotEmpty(query.getCriteriaMap())) {
            StringBuilder sbr = new StringBuilder();
            query.getCriteriaMap().entrySet()
                    .stream()
                    .filter(es -> this.getProperty2NameSortMap().containsKey(es.getKey()))
                    .forEach(es -> {
                        Field field = this.getProperty2NameSortMap().get(es.getKey());
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
                && this.getProperty2NameSortMap().containsKey(query.getSortBy())) {
            searchQ.setSortBy(
                    this.getProperty2NameSortMap().get(query.getSortBy()).getName(),
                    CacheQuery.Sort.Direction.ASC.equals(query.getSortDirection())
            );
        }

        return searchQ;
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
