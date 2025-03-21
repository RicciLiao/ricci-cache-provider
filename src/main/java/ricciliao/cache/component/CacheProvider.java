package ricciliao.cache.component;


import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;
import ricciliao.x.component.cache.pojo.ProviderInfoDto;

import java.time.Duration;
import java.util.List;

public abstract class CacheProvider {

    private final ConsumerIdentifierDto consumerIdentifier;
    private final Duration ttl;

    protected CacheProvider(ConsumerIdentifierDto consumerIdentifier,
                            Duration ttl) {
        this.consumerIdentifier = consumerIdentifier;
        this.ttl = ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    public ConsumerIdentifierDto getConsumerIdentifier() {
        return consumerIdentifier;
    }

    public abstract boolean create(ConsumerOperationDto<CacheDto> operation);

    public abstract boolean update(ConsumerOperationDto<CacheDto> operation);

    public abstract ConsumerOperationDto<CacheDto> get(String key);

    public abstract boolean delete(String key);

    public abstract List<ConsumerOperationDto<CacheDto>> list();

    public abstract ProviderInfoDto getProviderInfo();

}
