package ricciliao.cache.service;


import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpBatchQueryDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;

public interface CacheService {

    String create(ConsumerIdentifierDto identifier,
                  ConsumerOpDto.Single<CacheDto> operation);

    boolean update(ConsumerIdentifierDto identifier,
                   ConsumerOpDto.Single<CacheDto> updating);

    boolean delete(ConsumerIdentifierDto identifier, String id);

    ConsumerOpDto.Single<CacheDto> get(ConsumerIdentifierDto identifier, String id);

    ConsumerOpDto.Batch<CacheDto> list(ConsumerIdentifierDto identifier, ConsumerOpBatchQueryDto query);

    ProviderInfoDto providerInfo(ConsumerIdentifierDto identifier);

    boolean create(ConsumerIdentifierDto identifier,
                   ConsumerOpDto.Batch<CacheDto> operation);

}
