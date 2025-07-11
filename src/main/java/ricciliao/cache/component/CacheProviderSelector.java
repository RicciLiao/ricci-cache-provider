package ricciliao.cache.component;


import jakarta.annotation.Nullable;
import ricciliao.x.cache.pojo.ConsumerIdentifier;

import java.util.HashMap;
import java.util.Map;

public class CacheProviderSelector {

    private final Map<ConsumerIdentifier, CacheProvider> cacheProviderMap;
    private final Map<ConsumerIdentifier, Boolean> cacheStaticalMap;

    public CacheProviderSelector() {
        this.cacheProviderMap = new HashMap<>();
        this.cacheStaticalMap = new HashMap<>();
    }

    public Map<ConsumerIdentifier, CacheProvider> getCacheProviderMap() {
        return cacheProviderMap;
    }

    public Map<ConsumerIdentifier, Boolean> getCacheStaticalMap() {
        return cacheStaticalMap;
    }

    public CacheProvider selectProvider(@Nullable ConsumerIdentifier identifier) {

        return getCacheProviderMap().get(identifier);
    }

    public Boolean isStatical(@Nullable ConsumerIdentifier identifier) {

        return getCacheStaticalMap().get(identifier);
    }

}
