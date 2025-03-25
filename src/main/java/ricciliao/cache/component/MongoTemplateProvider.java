package ricciliao.cache.component;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import ricciliao.cache.configuration.mongo.MongoCacheAutoProperties;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;
import ricciliao.x.component.random.RandomGenerator;

import java.util.Objects;

public class MongoTemplateProvider extends CacheProvider {

    private final MongoTemplate mongoTemplate;
    private final String collectionName;
    private final Class<? extends CacheDto> storeClassName;

    public MongoTemplateProvider(ConsumerIdentifierDto identifier,
                                 MongoCacheAutoProperties.ConsumerProperties.StoreProperties props,
                                 MongoTemplate mongoTemplate) {

        super(identifier, props.getAddition().getTtl());
        this.mongoTemplate = mongoTemplate;
        this.collectionName = props.getStore();
        this.storeClassName = props.getStoreClassName();
    }

    @Override
    public boolean create(ConsumerOpDto.Single<CacheDto> operation) {
        operation.setId(RandomGenerator.nextString(12).allAtLeast(3).generate());
        mongoTemplate.insert(operation.getData(), collectionName);

        return true;
    }

    @Override
    public boolean update(ConsumerOpDto.Single<CacheDto> operation) {
        UpdateResult result = mongoTemplate.replace(
                Query.query(Criteria.where("key").is(operation.getData().getId())),
                operation.getData(),
                collectionName
        );

        return result.getModifiedCount() == 1;
    }

    @Override
    public ConsumerOpDto.Single<CacheDto> get(String key) {
        CacheDto cache =
                mongoTemplate.findOne(
                        Query.query(Criteria.where("key").is(key)),
                        storeClassName,
                        collectionName
                );
        if (Objects.nonNull(cache)) {

            return new ConsumerOpDto.Single<>(cache, this.getTtl().toMillis());
        }

        return null;
    }

    @Override
    public boolean delete(String key) {

        return Objects.nonNull(
                mongoTemplate.findAndRemove(
                        Query.query(Criteria.where("key").is(key)),
                        storeClassName,
                        collectionName
                )
        );
    }

    @Override
    public ConsumerOpDto.Batch<CacheDto> list() {
        return null;
    }

    @Override
    public boolean create(ConsumerOpDto.Batch<CacheDto> operation) {

        return false;
    }

    @Override
    public ProviderInfoDto getProviderInfo() {
        CacheDto maxUpdatedDtm =
                mongoTemplate.findOne(
                        new Query().with(Sort.by(Sort.Order.desc("updatedDtm"))).limit(1),
                        storeClassName,
                        collectionName
                );
        ProviderInfoDto result = new ProviderInfoDto(this.getConsumerIdentifier());

        if (Objects.nonNull(maxUpdatedDtm)) {
            result.setMaxUpdatedDtm(maxUpdatedDtm.getUpdatedDtm());
            result.setCount(mongoTemplate.count(new Query(), collectionName));
        }

        return result;
    }

}
