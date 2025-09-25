package ricciliao.cache.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import ricciliao.cache.common.CacheConstants;
import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.cache.pojo.ProviderOp;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.cache.query.CacheBatchQuery;
import ricciliao.x.cache.query.CacheQuery;
import ricciliao.x.log.api.XLogger;
import ricciliao.x.log.api.XLoggerFactory;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class JedisProvider extends CacheProvider {

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
    public boolean create(ProviderOp.Single operation) {
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
            logger.error(OP_FAILED, this.getConsumerIdentifier(), e);

            return false;
        }
    }

    @Override
    public boolean update(ProviderOp.Single operation) {
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
            logger.error(OP_FAILED + this.getConsumerIdentifier(), e);

            return false;
        }
    }

    @Override
    public ProviderOp.Single get(String key) {

        return new ProviderOp.Single(
                this.getStoreProps().getAddition().getTtl().toSeconds(),
                this.constr.objectMapper.convertValue(
                        this.constr.jedisPooled.jsonGet(this.buildRedisKey(key)),
                        ProviderCacheStore.class
                )
        );
    }

    @Override
    public boolean delete(String key) {

        return this.constr.jedisPooled.del(this.buildRedisKey(key)) == 1L;
    }

    @Override
    public ProviderOp.Batch list(CacheBatchQuery query) {
        ProviderOp.Batch result = new ProviderOp.Batch();
        result.setTtlOfSeconds(this.constr.getStoreProps().getAddition().getTtl().toSeconds());
        SearchResult sr = this.constr.jedisPooled.ftSearch(this.constr.indexName, this.toQuery(query));
        if (sr.getTotalResults() > 0) {
            ProviderCacheStore[] stores = new ProviderCacheStore[Math.toIntExact(sr.getTotalResults())];
            try {
                for (int i = 0; i < sr.getDocuments().size(); i++) {
                    stores[i] =
                            this.constr.objectMapper.readValue(
                                    sr.getDocuments().get(i).getString("$"),
                                    ProviderCacheStore.class
                            );
                }
            } catch (JsonProcessingException e) {
                logger.error(OP_FAILED, this.getConsumerIdentifier(), e);
            }
            result.setData(stores);
        }

        return result;
    }

    @Override
    public boolean delete(CacheBatchQuery query) {
        boolean finish = false;
        while (!finish) {
            ProviderOp.Batch batch = this.list(query);
            if (ArrayUtils.isNotEmpty(batch.getData())) {
                this.constr.jedisPooled.del(
                        Arrays.stream(batch.getData())
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

            ProviderOp.Batch dto = this.list(query);
            result.setCreatedDtm(dto.getData()[0].getEffectedDtm());
            result.setMaxUpdatedDtm(dto.getData()[0].getUpdatedDtm());
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
