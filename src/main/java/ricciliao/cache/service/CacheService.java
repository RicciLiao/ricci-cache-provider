package ricciliao.cache.service;


import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.cache.query.CacheBatchQuery;

public interface CacheService {

    String create(ConsumerIdentifier identifier,
                  ProviderCacheStore store);

    boolean update(ConsumerIdentifier identifier,
                   ProviderCacheStore store);

    boolean delete(ConsumerIdentifier identifier, String id);

    ProviderCacheStore get(ConsumerIdentifier identifier, String id);

    ProviderCacheStore.Batch list(ConsumerIdentifier identifier, CacheBatchQuery query);

    boolean delete(ConsumerIdentifier identifier, CacheBatchQuery query);

    ProviderInfo providerInfo(ConsumerIdentifier identifier);

    boolean create(ConsumerIdentifier identifier,
                   ProviderCacheStore.Batch storeList);

}
