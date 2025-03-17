package ricciliao.cache.component;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class MongoTemplateProvider extends CacheProvider {

    private final MongoTemplate mongoTemplate;
    private final String collectionName;
    private final Class<? extends CacheDto> storeClassName;

    public MongoTemplateProvider(MongoTemplate mongoTemplate,
                                 Duration ttl,
                                 String collectionName,
                                 Class<? extends CacheDto> storeClassName) {
        super(ttl);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.storeClassName = storeClassName;
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

            return new ConsumerOperationDto<>(key, this.getTtl().toMillis(), cache);
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
}
