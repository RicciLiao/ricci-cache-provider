package ricciliao.cache.component;


import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;

import java.time.Duration;
import java.util.List;

public abstract class CacheProvider {

    private final Duration ttl;

    protected CacheProvider(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    public abstract boolean create(ConsumerOperationDto<CacheDto> operation);

    public abstract boolean update(ConsumerOperationDto<CacheDto> operation);

    public abstract ConsumerOperationDto<CacheDto> get(String key);

    public abstract boolean delete(String key);

    public abstract List<ConsumerOperationDto<CacheDto>> list();

}
