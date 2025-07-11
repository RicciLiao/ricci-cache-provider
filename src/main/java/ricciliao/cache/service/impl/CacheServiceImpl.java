package ricciliao.cache.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.cache.pojo.ProviderOp;
import ricciliao.cache.service.CacheService;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.cache.query.CacheBatchQuery;
import ricciliao.x.component.random.RandomGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

@Service("cacheService")
public class CacheServiceImpl implements CacheService {

    private CacheProviderSelector providerSelector;

    @Autowired
    public void setProviderSelector(CacheProviderSelector providerSelector) {
        this.providerSelector = providerSelector;
    }

    @Override
    public String create(ConsumerIdentifier identifier, ProviderOp.Single operation) {
        LocalDateTime now = LocalDateTime.now();
        operation.getData().setEffectedDtm(now);
        if (Boolean.TRUE.equals(providerSelector.isStatical(identifier))) {
            operation.getData().setCreatedDtm(now);
            operation.getData().setUpdatedDtm(operation.getData().getCreatedDtm());
        } else {
            operation.getData().setCacheKey(RandomGenerator.nextString(12).allAtLeast(3).generate());
            operation.getData().setCreatedDtm(now);
            operation.getData().setUpdatedDtm(now);
        }
        providerSelector.selectProvider(identifier).create(operation);

        return operation.getData().getCacheKey();
    }

    @Override
    public boolean update(ConsumerIdentifier identifier, ProviderOp.Single updating) {
        ProviderOp.Single existing = this.get(identifier, updating.getData().getCacheKey());
        if (Objects.isNull(existing.getData())) {

            return false;
        }
        updating.setTtlOfSeconds(existing.getTtlOfSeconds());
        updating.getData().setEffectedDtm(existing.getData().getEffectedDtm());
        updating.getData().setCreatedDtm(existing.getData().getCreatedDtm());
        if (Boolean.FALSE.equals(providerSelector.isStatical(identifier))) {
            updating.getData().setUpdatedDtm(LocalDateTime.now());
        }

        return providerSelector.selectProvider(identifier).update(updating);
    }

    @Override
    public boolean delete(ConsumerIdentifier identifier, String id) {
        ProviderOp.Single operationDto = this.get(identifier, id);
        if (Objects.isNull(operationDto.getData())) {

            return false;
        }

        return providerSelector.selectProvider(identifier).delete(id);
    }

    @Override
    public ProviderOp.Single get(ConsumerIdentifier identifier, String id) {

        return providerSelector.selectProvider(identifier).get(id);
    }

    @Override
    public ProviderOp.Batch list(ConsumerIdentifier identifier, CacheBatchQuery query) {

        return providerSelector.selectProvider(identifier).list(query);
    }

    @Override
    public boolean delete(ConsumerIdentifier identifier, CacheBatchQuery query) {

        return providerSelector.selectProvider(identifier).delete(query);
    }

    @Override
    public ProviderInfo providerInfo(ConsumerIdentifier identifier) {

        return providerSelector.selectProvider(identifier).getProviderInfo();
    }

    @Override
    public boolean create(ConsumerIdentifier identifier, ProviderOp.Batch operation) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.FALSE.equals(providerSelector.isStatical(identifier))) {
            for (ProviderCacheStore cache : operation.getData()) {
                cache.setCacheKey(RandomGenerator.nextString(12).allAtLeast(3).generate());
                cache.setCreatedDtm(now);
                cache.setUpdatedDtm(cache.getCreatedDtm());
                cache.setEffectedDtm(now);
            }
        } else {
            for (ProviderCacheStore cache : operation.getData()) {
                cache.setEffectedDtm(now);
            }
        }

        return providerSelector.selectProvider(identifier).create(operation);
    }

}
