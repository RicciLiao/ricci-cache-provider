package ricciliao.cache.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.RedisCacheProvider;
import ricciliao.cache.service.CacheService;
import ricciliao.common.component.cache.ConsumerIdentifierDto;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;
import ricciliao.common.component.exception.CmnException;

@Service("redisCacheService")
public class RedisCacheServiceImpl implements CacheService {

    private RedisCacheProvider cacheProvider;

    @Autowired
    public void setCacheProvider(RedisCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }


    @Override
    public boolean create(ConsumerIdentifierDto identifier, ConsumerOperationDto<? extends RedisCacheBo> operation) throws CmnException {
        /*StringRedisTemplateWrapper<? extends RedisCacheBo> wrapper =
                cacheProvider.getWrapper(new WrapperIdentifierBo(identifier.getC(), identifier.getI()));*/
        //wrapper.set(RandomGenerator.nextString(12).allAtLeast(3).generate(), operation.getData());
        return false;
    }

}
