package ricciliao.cache.component;


import jakarta.annotation.Nullable;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifier;

import java.util.HashMap;
import java.util.Map;

public class CacheProviderSelector {

    private final Map<ConsumerIdentifier, CacheProvider> cacheProviderMap;
    private final Map<ConsumerIdentifier, Class<? extends CacheDto>> cacheClassMap;
    private final Map<ConsumerIdentifier, Boolean> cacheStaticalMap;

    public CacheProviderSelector() {
        this.cacheProviderMap = new HashMap<>();
        this.cacheClassMap = new HashMap<>();
        this.cacheStaticalMap = new HashMap<>();
    }

    public Map<ConsumerIdentifier, CacheProvider> getCacheProviderMap() {
        return cacheProviderMap;
    }

    public Map<ConsumerIdentifier, Class<? extends CacheDto>> getCacheClassMap() {
        return cacheClassMap;
    }

    public Map<ConsumerIdentifier, Boolean> getCacheStaticalMap() {
        return cacheStaticalMap;
    }

    public CacheProvider selectProvider(ConsumerIdentifier identifier) {
        if (getCacheProviderMap().containsKey(identifier)) {

            return getCacheProviderMap().get(identifier);
        }

        return null;
    }

    public Class<? extends CacheDto> getCacheClass(ConsumerIdentifier identifier) {
        if (getCacheClassMap().containsKey(identifier)) {

            return getCacheClassMap().get(identifier);
        }

        return null;
    }

    @Nullable
    public Boolean isStatical(ConsumerIdentifier identifier) {
        if (getCacheClassMap().containsKey(identifier)) {

            return getCacheStaticalMap().get(identifier);
        }

        return null;
    }

}
