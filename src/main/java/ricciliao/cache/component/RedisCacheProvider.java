package ricciliao.cache.component;

import org.springframework.stereotype.Component;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.RedisCacheDto;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedisCacheProvider {

    private final Map<ConsumerIdentifierDto, StringRedisTemplateWrapper> providerMap = new HashMap<>();
    private final Map<ConsumerIdentifierDto, Class<? extends RedisCacheDto>> cacheClass = new HashMap<>();

    public Map<ConsumerIdentifierDto, StringRedisTemplateWrapper> getProviderMap() {
        return providerMap;
    }

    public Map<ConsumerIdentifierDto, Class<? extends RedisCacheDto>> getCacheClass() {
        return cacheClass;
    }

    public StringRedisTemplateWrapper getProvider(ConsumerIdentifierDto identifier) {
        if (getProviderMap().containsKey(identifier)) {

            return getProviderMap().get(identifier);
        }

        return null;
    }

    public Class<? extends RedisCacheDto> getCacheClass(ConsumerIdentifierDto identifier) {
        if (getCacheClass().containsKey(identifier)) {

            return getCacheClass().get(identifier);
        }

        return null;
    }

}
