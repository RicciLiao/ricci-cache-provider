package ricciliao.cache.component;

import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import ricciliao.cache.common.CacheConstants;
import ricciliao.x.cache.CacheKey;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerOpBatchQueryDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;
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
    public boolean create(ConsumerOpDto.Single<CacheDto> operation) {
        operation.setId(RandomGenerator.nextString(12).allAtLeast(3).generate());
        this.constr.mongoTemplate.insert(operation.getData(), this.getStoreProps().getStore());

        return true;
    }

    @Override
    public boolean update(ConsumerOpDto.Single<CacheDto> operation) {
        UpdateResult result = constr.mongoTemplate.replace(
                Query.query(Criteria.where(this.cacheKeyName).is(operation.getData().getCacheKey())),
                operation.getData(),
                this.getStoreProps().getStore()
        );

        return result.getModifiedCount() == 1;
    }

    @Override
    public ConsumerOpDto.Single<CacheDto> get(String key) {
        CacheDto cache =
                constr.mongoTemplate.findOne(
                        Query.query(Criteria.where(this.cacheKeyName).is(key)),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                );
        if (Objects.nonNull(cache)) {

            return new ConsumerOpDto.Single<>(cache, this.getAdditionalProps().getTtl().toMillis());
        }

        return null;
    }

    @Override
    public boolean delete(String key) {

        return Objects.nonNull(
                constr.mongoTemplate.findAndRemove(
                        Query.query(Criteria.where(this.cacheKeyName).is(key)),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                )
        );
    }

    @Override
    public ConsumerOpDto.Batch<CacheDto> list(ConsumerOpBatchQueryDto query) {
        Query q = new Query();
        q.limit(Objects.nonNull(query.getLimit()) ? query.getLimit().intValue() : CacheConstants.DEFAULT_CACHE_OP_BATCH_LIMIT);

        if (Objects.nonNull(query.getSortBy())
                && Objects.nonNull(query.getSortDirection())) {
            q.with(Sort.by(Sort.Direction.fromString(query.getSortDirection().name()), query.getSortBy().name()));
        }

        List<CacheDto> data = constr.mongoTemplate.find(q, this.getStoreProps().getStoreClassName(), this.getStoreProps().getStore());

        return new ConsumerOpDto.Batch<>(data, this.getStoreProps().getAddition().getTtl().toMillis());
    }

    @Override
    public ProviderInfoDto getProviderInfo() {
        CacheDto maxUpdatedDtm =
                constr.mongoTemplate.findOne(
                        new Query().with(Sort.by(Sort.Order.desc("updatedDtm"))).limit(1),
                        this.getStoreProps().getStoreClassName(),
                        this.getStoreProps().getStore()
                );
        ProviderInfoDto result = new ProviderInfoDto(this.getConsumerIdentifier());

        if (Objects.nonNull(maxUpdatedDtm)) {
            result.setMaxUpdatedDtm(maxUpdatedDtm.getUpdatedDtm());
            result.setCount(constr.mongoTemplate.count(new Query(), this.getStoreProps().getStore()));
        }

        return result;
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
