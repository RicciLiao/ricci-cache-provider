package ricciliao.cache.service;

import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.CacheExtraOperationDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationBatchDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;
import ricciliao.x.component.cache.pojo.ProviderInfoDto;

import java.util.List;

public interface CacheService {

    String create(ConsumerIdentifierDto identifier,
                  ConsumerOperationDto<CacheDto> operation);

    boolean update(ConsumerIdentifierDto identifier,
                   ConsumerOperationDto<CacheDto> updating);

    boolean delete(ConsumerIdentifierDto identifier, String id);

    ConsumerOperationDto<CacheDto> get(ConsumerIdentifierDto identifier, String id);

    List<ConsumerOperationDto<CacheDto>> list(ConsumerIdentifierDto identifier, CacheExtraOperationDto operation);

    ProviderInfoDto getProviderInfo(ConsumerIdentifierDto identifier);

    boolean create(ConsumerIdentifierDto identifier,
                   ConsumerOperationBatchDto<CacheDto> operation);

}
