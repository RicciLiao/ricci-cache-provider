package ricciliao.cache.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.RedisCacheProvider;
import ricciliao.cache.service.CacheService;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.ConsumerOperationDto;
import ricciliao.common.component.cache.pojo.RedisCacheDto;

import java.time.LocalDateTime;
import java.util.Objects;

@Service("redisCacheService")
public class RedisCacheServiceImpl implements CacheService {

    private RedisCacheProvider cacheProvider;

    @Autowired
    public void setCacheProvider(RedisCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }


    @Override
    public boolean create(ConsumerIdentifierDto identifier, ConsumerOperationDto<RedisCacheDto> operation) {
        operation.getData().setCreatedDtm(LocalDateTime.now());
        operation.getData().setUpdatedDtm(operation.getData().getCreatedDtm());

        return cacheProvider.getProvider(identifier).create(operation);
    }

    @Override
    public boolean update(ConsumerIdentifierDto identifier, ConsumerOperationDto<RedisCacheDto> operation) {
        ConsumerOperationDto<RedisCacheDto> operationDto = this.get(identifier, operation.getData().getId());
        if (Objects.isNull(operationDto.getData())) {

            return false;
        }
        RedisCacheDto data = operationDto.getData();
        operation.getData().setCreatedDtm(data.getCreatedDtm());
        operation.getData().setUpdatedDtm(LocalDateTime.now());

        return cacheProvider.getProvider(identifier).update(operation);
    }

    @Override
    public boolean delete(ConsumerIdentifierDto identifier, String id) {

        return cacheProvider.getProvider(identifier).delete(id);
    }

    @Override
    public ConsumerOperationDto<RedisCacheDto> get(ConsumerIdentifierDto identifier, String id) {

        return cacheProvider.getProvider(identifier).get(id);
    }

}
