package ricciliao.cache.component;


import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;

import java.util.HashMap;
import java.util.Map;

public class CacheProviderSelector {

    private final Map<ConsumerIdentifierDto, CacheProvider> cacheProviderMap;
    private final Map<ConsumerIdentifierDto, Class<? extends CacheDto>> cacheClassMap;
    private final Map<ConsumerIdentifierDto, Boolean> stagnantMap;

    public CacheProviderSelector() {
        this.cacheProviderMap = new HashMap<>();
        this.cacheClassMap = new HashMap<>();
        this.stagnantMap = new HashMap<>();
    }

    public Map<ConsumerIdentifierDto, CacheProvider> getCacheProviderMap() {
        return cacheProviderMap;
    }

    public Map<ConsumerIdentifierDto, Class<? extends CacheDto>> getCacheClassMap() {
        return cacheClassMap;
    }

    public Map<ConsumerIdentifierDto, Boolean> getStagnantMap() {
        return stagnantMap;
    }

    public CacheProvider selectProvider(ConsumerIdentifierDto identifier) {
        if (getCacheProviderMap().containsKey(identifier)) {

            return getCacheProviderMap().get(identifier);
        }

        return null;
    }

    public Class<? extends CacheDto> getCacheClass(ConsumerIdentifierDto identifier) {
        if (getCacheClassMap().containsKey(identifier)) {

            return getCacheClassMap().get(identifier);
        }

        return null;
    }

    public Boolean getStagnant(ConsumerIdentifierDto identifier) {
        if (getCacheClassMap().containsKey(identifier)) {

            return getStagnantMap().get(identifier);
        }

        return null;
    }

}
