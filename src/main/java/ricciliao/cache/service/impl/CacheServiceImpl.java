package ricciliao.cache.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.service.CacheService;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.CacheExtraOperationDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;
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
    public String create(ConsumerIdentifierDto identifier, ConsumerOpDto.Single<CacheDto> operation) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.FALSE.equals(providerSelector.getStagnant(identifier))) {
            operation.getData().setKey(RandomGenerator.nextString(12).allAtLeast(3).generate());
            operation.getData().setCreatedDtm(now);
            operation.getData().setUpdatedDtm(operation.getData().getCreatedDtm());
        }

        operation.getData().setEffectedDtm(now);
        providerSelector.selectProvider(identifier).create(operation);

        return operation.getData().getKey();
    }

    @Override
    public boolean update(ConsumerIdentifierDto identifier, ConsumerOpDto.Single<CacheDto> updating) {
        ConsumerOpDto.Single<CacheDto> existing = this.get(identifier, updating.getData().getKey());
        if (Objects.isNull(existing.getData())) {

            return false;
        }
        updating.setTtlOfMillis(existing.getTtlOfMillis());
        updating.getData().setEffectedDtm(existing.getData().getEffectedDtm());
        updating.getData().setCreatedDtm(existing.getData().getCreatedDtm());
        if (Boolean.FALSE.equals(providerSelector.getStagnant(identifier))) {
            updating.getData().setUpdatedDtm(LocalDateTime.now());
        }

        return providerSelector.selectProvider(identifier).update(updating);
    }

    @Override
    public boolean delete(ConsumerIdentifierDto identifier, String id) {
        ConsumerOpDto.Single<CacheDto> operationDto = this.get(identifier, id);
        if (Objects.isNull(operationDto.getData())) {

            return false;
        }

        return providerSelector.selectProvider(identifier).delete(id);
    }

    @Override
    public ConsumerOpDto.Single<CacheDto> get(ConsumerIdentifierDto identifier, String id) {

        return providerSelector.selectProvider(identifier).get(id);
    }

    @Override
    public ConsumerOpDto.Batch<CacheDto> list(ConsumerIdentifierDto identifier, CacheExtraOperationDto operation) {

        return null;
    }

    @Override
    public ProviderInfoDto providerInfo(ConsumerIdentifierDto identifier) {

        return providerSelector.selectProvider(identifier).getProviderInfo();
    }

    @Override
    public boolean create(ConsumerIdentifierDto identifier, ConsumerOpDto.Batch<CacheDto> operation) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.FALSE.equals(providerSelector.getStagnant(identifier))) {
            for (CacheDto cache : operation.getData()) {
                cache.setKey(RandomGenerator.nextString(12).allAtLeast(3).generate());
                cache.setCreatedDtm(now);
                cache.setUpdatedDtm(cache.getCreatedDtm());
                cache.setEffectedDtm(now);
            }
        } else {
            for (CacheDto cache : operation.getData()) {
                cache.setEffectedDtm(now);
            }
        }

        return providerSelector.selectProvider(identifier).create(operation);
    }

}
