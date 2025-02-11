package ricciliao.cache.service;

import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.ConsumerOperationDto;
import ricciliao.common.component.cache.pojo.CacheDto;

public interface CacheService {

    boolean create(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<CacheDto> operation);

    boolean update(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<CacheDto> operation);

    boolean delete(ConsumerIdentifierDto identifier, String id);

    ConsumerOperationDto<CacheDto> get(ConsumerIdentifierDto identifier, String id);

}
