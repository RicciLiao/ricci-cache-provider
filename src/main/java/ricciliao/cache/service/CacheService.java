package ricciliao.cache.service;


import ricciliao.x.cache.pojo.CacheBatchQuery;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.pojo.ConsumerOp;
import ricciliao.x.cache.pojo.ProviderInfo;

public interface CacheService {

    String create(ConsumerIdentifier identifier,
                  ConsumerOp.Single<CacheDto> operation);

    boolean update(ConsumerIdentifier identifier,
                   ConsumerOp.Single<CacheDto> updating);

    boolean delete(ConsumerIdentifier identifier, String id);

    ConsumerOp.Single<CacheDto> get(ConsumerIdentifier identifier, String id);

    ConsumerOp.Batch<CacheDto> list(ConsumerIdentifier identifier, CacheBatchQuery query);

    boolean delete(ConsumerIdentifier identifier, CacheBatchQuery query);

    ProviderInfo providerInfo(ConsumerIdentifier identifier);

    boolean create(ConsumerIdentifier identifier,
                   ConsumerOp.Batch<CacheDto> operation);

}
