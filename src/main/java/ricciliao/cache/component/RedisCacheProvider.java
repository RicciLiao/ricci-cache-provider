package ricciliao.cache.component;

import org.springframework.stereotype.Component;
import ricciliao.cache.pojo.bo.WrapperIdentifierBo;
import ricciliao.common.component.cache.RedisCacheBo;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedisCacheProvider {

    private final Map<WrapperIdentifierBo, StringRedisTemplateWrapper> providerMap = new HashMap<>();
    private final Map<WrapperIdentifierBo, Class<? extends RedisCacheBo>> cacheClass = new HashMap<>();

    public Map<WrapperIdentifierBo, StringRedisTemplateWrapper> getProviderMap() {
        return providerMap;
    }

    public Map<WrapperIdentifierBo, Class<? extends RedisCacheBo>> getCacheClass() {
        return cacheClass;
    }

    public StringRedisTemplateWrapper getProvider(WrapperIdentifierBo identifier) {
        if (getProviderMap().containsKey(identifier)) {

            return getProviderMap().get(identifier);
        }

        return null;
    }

    public Class<? extends RedisCacheBo> getCacheClass(WrapperIdentifierBo identifier) {
        if (getCacheClass().containsKey(identifier)) {

            return getCacheClass().get(identifier);
        }

        return RedisCacheBo.class;
    }

}
