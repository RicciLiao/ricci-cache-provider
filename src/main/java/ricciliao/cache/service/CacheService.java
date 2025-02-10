package ricciliao.cache.service;

import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.ConsumerOperationDto;
import ricciliao.common.component.cache.pojo.RedisCacheDto;

public interface CacheService {

    boolean create(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<RedisCacheDto> operation);

    boolean update(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<RedisCacheDto> operation);

    boolean delete(ConsumerIdentifierDto identifier, String id);

    ConsumerOperationDto<RedisCacheDto> get(ConsumerIdentifierDto identifier, String id);

}
