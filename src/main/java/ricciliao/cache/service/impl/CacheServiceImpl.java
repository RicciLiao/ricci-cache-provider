package ricciliao.cache.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.CacheProvider;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.service.CacheService;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.CacheExtraOperationDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationBatchDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;
import ricciliao.x.component.cache.pojo.ProviderInfoDto;
import ricciliao.x.component.random.RandomGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service("cacheService")
public class CacheServiceImpl implements CacheService {

    private CacheProviderSelector providerSelector;

    @Autowired
    public void setProviderSelector(CacheProviderSelector providerSelector) {
        this.providerSelector = providerSelector;
    }

    @Override
    public String create(ConsumerIdentifierDto identifier, ConsumerOperationDto<CacheDto> operation) {
        LocalDateTime now = LocalDateTime.now();
        CacheDto cache = operation.getData();
        cache.setCacheId(RandomGenerator.nextString(12).allAtLeast(3).generate());
        cache.setCreatedDtm(Objects.nonNull(cache.getCreatedDtm()) ? cache.getCreatedDtm() : now);
        cache.setUpdatedDtm(Objects.nonNull(cache.getUpdatedDtm()) ? cache.getUpdatedDtm() : cache.getCreatedDtm());
        cache.setEffectedDtm(now);
        providerSelector.selectProvider(identifier).create(operation);

        return operation.getData().getCacheId();
    }

    @Override
    public boolean update(ConsumerIdentifierDto identifier, ConsumerOperationDto<CacheDto> updating) {
        ConsumerOperationDto<CacheDto> existing = this.get(identifier, updating.getData().getCacheId());
        if (Objects.isNull(existing.getData())) {

            return false;
        }
        updating.getData().setCreatedDtm(Objects.nonNull(updating.getData().getCreatedDtm()) ?
                updating.getData().getCreatedDtm() : existing.getData().getCreatedDtm());
        updating.getData().setUpdatedDtm(Objects.nonNull(updating.getData().getUpdatedDtm()) ?
                updating.getData().getUpdatedDtm() : LocalDateTime.now());
        updating.getData().setEffectedDtm(existing.getData().getEffectedDtm());

        return providerSelector.selectProvider(identifier).update(updating);
    }

    @Override
    public boolean delete(ConsumerIdentifierDto identifier, String id) {
        ConsumerOperationDto<CacheDto> operationDto = this.get(identifier, id);
        if (Objects.isNull(operationDto.getData())) {

            return false;
        }

        return providerSelector.selectProvider(identifier).delete(id);
    }

    @Override
    public ConsumerOperationDto<CacheDto> get(ConsumerIdentifierDto identifier, String id) {

        return providerSelector.selectProvider(identifier).get(id);
    }

    @Override
    public List<ConsumerOperationDto<CacheDto>> list(ConsumerIdentifierDto identifier, CacheExtraOperationDto operation) {

        return null;
    }

    @Override
    public ProviderInfoDto getProviderInfo(ConsumerIdentifierDto identifier) {

        return providerSelector.selectProvider(identifier).getProviderInfo();
    }

    @Override
    public boolean create(ConsumerIdentifierDto identifier, ConsumerOperationBatchDto<CacheDto> operation) {
        LocalDateTime now = LocalDateTime.now();
        CacheProvider provider = providerSelector.selectProvider(identifier);
        for (CacheDto cache : operation.getData()) {
            cache.setCacheId(RandomGenerator.nextString(12).allAtLeast(3).generate());
            cache.setCreatedDtm(Objects.nonNull(cache.getCreatedDtm()) ? cache.getCreatedDtm() : now);
            cache.setUpdatedDtm(Objects.nonNull(cache.getUpdatedDtm()) ? cache.getUpdatedDtm() : cache.getCreatedDtm());
            cache.setEffectedDtm(now);
            provider.create(new ConsumerOperationDto<>(cache, operation.getTtlOfMillis()));
        }

        return false;
    }

}
