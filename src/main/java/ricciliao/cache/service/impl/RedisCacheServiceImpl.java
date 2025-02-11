package ricciliao.cache.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.service.CacheService;
import ricciliao.common.component.cache.CacheProviderSelector;
import ricciliao.common.component.cache.pojo.CacheDto;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.ConsumerOperationDto;

import java.time.LocalDateTime;
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
        operation.getData().setCreatedDtm(LocalDateTime.now());
        operation.getData().setUpdatedDtm(operation.getData().getCreatedDtm());

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

        return providerSelector.selectProvider(identifier).delete(id);
    }

    @Override
    public ConsumerOperationDto<CacheDto> get(ConsumerIdentifierDto identifier, String id) {

        return providerSelector.selectProvider(identifier).get(id);
    }

}
