package ricciliao.cache.service;


import ricciliao.cache.pojo.ProviderOp;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.cache.query.CacheBatchQuery;

public interface CacheService {

    String create(ConsumerIdentifier identifier,
                  ProviderOp.Single operation);

    boolean update(ConsumerIdentifier identifier,
                   ProviderOp.Single updating);

    boolean delete(ConsumerIdentifier identifier, String id);

    ProviderOp.Single get(ConsumerIdentifier identifier, String id);

    ProviderOp.Batch list(ConsumerIdentifier identifier, CacheBatchQuery query);

    boolean delete(ConsumerIdentifier identifier, CacheBatchQuery query);

    ProviderInfo providerInfo(ConsumerIdentifier identifier);

    boolean create(ConsumerIdentifier identifier,
                   ProviderOp.Batch operation);

}
