package ricciliao.cache.component;


import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;

import java.time.Duration;

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

    public abstract boolean create(ConsumerOpDto.Single<CacheDto> operation);

    public abstract boolean update(ConsumerOpDto.Single<CacheDto> operation);

    public abstract ConsumerOpDto.Single<CacheDto> get(String key);

    public abstract boolean delete(String key);

    public abstract ConsumerOpDto.Batch<CacheDto> list();

    public abstract boolean create(ConsumerOpDto.Batch<CacheDto> operation);

    public abstract ProviderInfoDto getProviderInfo();

}
