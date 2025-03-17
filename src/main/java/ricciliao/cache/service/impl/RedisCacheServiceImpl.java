package ricciliao.cache.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.service.CacheService;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.CacheExtraOperationDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;
import ricciliao.x.component.random.RandomGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service("redisCacheService")
public class RedisCacheServiceImpl implements CacheService {

    private CacheProviderSelector providerSelector;

    @Autowired
    public void setProviderSelector(CacheProviderSelector providerSelector) {
        this.providerSelector = providerSelector;
    }

    @Override
    public boolean create(ConsumerIdentifierDto identifier, ConsumerOperationDto<CacheDto> operation) {
        operation.getData().setCacheId(RandomGenerator.nextString(12).allAtLeast(3).generate());
        operation.getData().setCreatedDtm(LocalDateTime.now());
        operation.getData().setUpdatedDtm(operation.getData().getCreatedDtm());
        operation.getData().setEffectedDtm(operation.getData().getCreatedDtm());

        return providerSelector.selectProvider(identifier).create(operation);
    }

    @Override
    public boolean update(ConsumerIdentifierDto identifier, ConsumerOperationDto<CacheDto> operation) {
        ConsumerOperationDto<CacheDto> operationDto = this.get(identifier, operation.getData().getCacheId());
        if (Objects.isNull(operationDto.getData())) {

            return false;
        }
        CacheDto data = operationDto.getData();
        operation.getData().setCreatedDtm(data.getCreatedDtm());
        operation.getData().setUpdatedDtm(LocalDateTime.now());

        return providerSelector.selectProvider(identifier).update(operation);
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

}
