package ricciliao.cache.component;

import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import ricciliao.cache.common.CacheConstants;
import ricciliao.x.cache.CacheKey;
import ricciliao.x.cache.pojo.CacheBatchQuery;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerOp;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.component.random.RandomGenerator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MongoTemplateProvider extends CacheProvider {

    private final MongoTemplateProviderConstruct constr;
    private final String cacheKeyName;

    public MongoTemplateProvider(MongoTemplateProviderConstruct mongoTemplateProviderConstruct) {
        super(mongoTemplateProviderConstruct);
        this.constr = mongoTemplateProviderConstruct;

        Field[] fields = CacheDto.class.getDeclaredFields();
        List<Field> keyFieldList =
                Arrays.stream(fields)
                        .filter(field -> field.isAnnotationPresent(CacheKey.class))
                        .toList();
        if (CollectionUtils.isEmpty(keyFieldList)) {
            keyFieldList =
                    Arrays.stream(this.getStoreProps().getStoreClassName().getSuperclass().getDeclaredFields())
                            .filter(field -> field.isAnnotationPresent(CacheKey.class))
                            .toList();
        }
        if (CollectionUtils.size(keyFieldList) != 1) {

            throw new BeanCreationException(
                    String.format(
                            "Initialize MongoTemplateProvider for collection: [%s] failed! Can not identify the CacheKey.",
                            this.getStoreProps().getStore()
                    )
            );
        }
        this.cacheKeyName = keyFieldList.get(0).getName();
    }

    @Override
    public boolean create(ConsumerOp.Single<CacheDto> operation) {
        operation.setId(RandomGenerator.nextString(12).allAtLeast(3).generate());
        this.constr.mongoTemplate.insert(operation.getData(), this.getStoreProps().getStore());

        return true;
    }

    @Override
    public boolean update(ConsumerOp.Single<CacheDto> operation) {
        UpdateResult result = this.constr.mongoTemplate.replace(
                Query.query(Criteria.where(this.cacheKeyName).is(operation.getData().getCacheKey())),
                operation.getData(),
                this.getStoreProps().getStore()
        );

        return result.getModifiedCount() == 1;
    }

    @Override
    public ConsumerOp.Single<CacheDto> get(String key) {
        CacheDto cache =
                this.constr.mongoTemplate.findOne(
                        Query.query(Criteria.where(this.cacheKeyName).is(key)),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                );
        if (Objects.nonNull(cache)) {

            return new ConsumerOp.Single<>(cache, this.getAdditionalProps().getTtl().toMillis());
        }

        return null;
    }

    @Override
    public boolean delete(String key) {

        return Objects.nonNull(
                this.constr.mongoTemplate.findAndRemove(
                        Query.query(Criteria.where(this.cacheKeyName).is(key)),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                )
        );
    }

    @Override
    public ConsumerOp.Batch<CacheDto> list(CacheBatchQuery query) {
        List<CacheDto> data =
                this.constr.mongoTemplate.find(
                        this.toQuery(query),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                );

        return new ConsumerOp.Batch<>(data, this.getStoreProps().getAddition().getTtl().toMillis());
    }

    @Override
    public boolean delete(CacheBatchQuery query) {
        this.constr.mongoTemplate.remove(this.toQuery(query), this.getStoreProps().getStoreClassName());

        return false;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        CacheDto maxUpdatedDtm =
                this.constr.mongoTemplate.findOne(
                        new Query().with(Sort.by(Sort.Order.desc("updatedDtm"))).limit(1),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                );
        ProviderInfo result = new ProviderInfo(this.getConsumerIdentifier());

        if (Objects.nonNull(maxUpdatedDtm)) {
            result.setMaxUpdatedDtm(maxUpdatedDtm.getUpdatedDtm());
            result.setCount(this.constr.mongoTemplate.count(new Query(), this.getStoreProps().getStore()));
        }

        return result;
    }

    protected Query toQuery(CacheBatchQuery query) {
        Query q = new Query();
        q.limit(Objects.nonNull(query.getLimit()) ? query.getLimit().intValue() : CacheConstants.DEFAULT_CACHE_OP_BATCH_LIMIT);

        if (Objects.nonNull(query.getSortBy())
                && Objects.nonNull(query.getSortDirection())) {
            q.with(Sort.by(Sort.Direction.fromString(query.getSortDirection().name()), query.getSortBy().name()));
        }
        if (MapUtils.isNotEmpty(query.getCriteriaMap())) {
            query.getCriteriaMap().entrySet()
                    .stream()
                    .filter(es -> this.getProperty2NameSortMap().containsKey(es.getKey()))
                    .forEach(es -> {
                        Field field = this.getProperty2NameSortMap().get(es.getKey());
                        String s = es.getValue().toString();
                        if (s.contains("*")) {
                            q.addCriteria(Criteria.where(field.getName()).regex(s.replace("*", ".*")));
                        } else {
                            q.addCriteria(Criteria.where(field.getName()).is(s));
                        }
                    });
        }

        return q;
    }

    public static class MongoTemplateProviderConstruct extends CacheProviderConstruct {
        private MongoTemplate mongoTemplate;

        public MongoTemplate getMongoTemplate() {
            return mongoTemplate;
        }

        public void setMongoTemplate(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

    }

}
