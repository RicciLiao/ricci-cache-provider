package ricciliao.cache.service;

import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.CacheExtraOperationDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;

import java.util.List;

public interface CacheService {

    boolean create(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<CacheDto> operation);

    boolean update(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<CacheDto> operation);

    boolean delete(ConsumerIdentifierDto identifier, String id);

    ConsumerOperationDto<CacheDto> get(ConsumerIdentifierDto identifier, String id);

    List<ConsumerOperationDto<CacheDto>> list(ConsumerIdentifierDto identifier, CacheExtraOperationDto operation);

}
