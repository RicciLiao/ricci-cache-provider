package ricciliao.cache.component;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import ricciliao.cache.configuration.mongo.MongoCacheAutoProperties;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;
import ricciliao.x.component.cache.pojo.ProviderInfoDto;

import java.util.List;
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
    public boolean create(ConsumerOperationDto<CacheDto> operation) {
        mongoTemplate.insert(operation.getData(), collectionName);

        return true;
    }

    @Override
    public boolean update(ConsumerOperationDto<CacheDto> operation) {
        UpdateResult result = mongoTemplate.replace(
                Query.query(Criteria.where("cacheId").is(operation.getData().getCacheId())),
                operation.getData(),
                collectionName
        );

        return result.getModifiedCount() == 1;
    }

    @Override
    public ConsumerOperationDto<CacheDto> get(String key) {
        CacheDto cache =
                mongoTemplate.findOne(
                        Query.query(Criteria.where("cacheId").is(key)),
                        storeClassName,
                        collectionName
                );
        if (Objects.nonNull(cache)) {

            return new ConsumerOperationDto<>(cache, this.getTtl().toMillis());
        }

        return null;
    }

    @Override
    public boolean delete(String key) {

        return Objects.nonNull(
                mongoTemplate.findAndRemove(
                        Query.query(Criteria.where("cacheId").is(key)),
                        storeClassName,
                        collectionName
                )
        );
    }

    @Override
    public List<ConsumerOperationDto<CacheDto>> list() {
        return null;
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
