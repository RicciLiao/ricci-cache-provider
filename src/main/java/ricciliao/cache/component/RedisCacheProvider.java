package ricciliao.cache.component;

import org.springframework.stereotype.Component;
import ricciliao.cache.pojo.bo.WrapperIdentifierBo;
import ricciliao.common.component.cache.RedisCacheBo;
import ricciliao.common.component.exception.CmnException;
import ricciliao.common.component.response.ResponseCodeEnum;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedisCacheProvider {

    private final Map<WrapperIdentifierBo, StringRedisTemplateWrapper<? extends RedisCacheBo>> wrapperMap = new HashMap<>();
    private final Map<WrapperIdentifierBo, Class<? extends RedisCacheBo>> classMap = new HashMap<>();

    public Map<WrapperIdentifierBo, StringRedisTemplateWrapper<? extends RedisCacheBo>> getWrapperMap() {
        return wrapperMap;
    }

    public Map<WrapperIdentifierBo, Class<? extends RedisCacheBo>> getClassMap() {
        return classMap;
    }

    public StringRedisTemplateWrapper<? extends RedisCacheBo> getWrapper(WrapperIdentifierBo identifier) throws CmnException {
        if (getWrapperMap().containsKey(identifier)) {

            return getWrapperMap().get(identifier);
        }

        throw new CmnException(ResponseCodeEnum.SYSTEM_ERROR);
    }

    public Class<? extends RedisCacheBo> getClass(WrapperIdentifierBo identifier) throws CmnException {
        if (getClassMap().containsKey(identifier)) {

            return getClassMap().get(identifier);
        }

        throw new CmnException(ResponseCodeEnum.SYSTEM_ERROR);
    }

}
