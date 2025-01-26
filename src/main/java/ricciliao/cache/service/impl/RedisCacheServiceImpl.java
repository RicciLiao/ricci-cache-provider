package ricciliao.cache.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ricciliao.cache.component.RedisCacheProvider;
import ricciliao.cache.component.StringRedisTemplateWrapper;
import ricciliao.cache.pojo.bo.WrapperIdentifierBo;
import ricciliao.cache.service.CacheService;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;
import ricciliao.common.component.exception.CmnException;
import ricciliao.common.component.random.RandomGenerator;

@Service("redisCacheService")
public class RedisCacheServiceImpl implements CacheService {

    private RedisCacheProvider cacheProvider;

    @Autowired
    public void setCacheProvider(RedisCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }


    @Override
    public boolean create(WrapperIdentifierBo identifier, ConsumerOperationDto<? extends RedisCacheBo> operation) throws CmnException {
        StringRedisTemplateWrapper wrapper = cacheProvider.getProvider(identifier);
        wrapper.set(RandomGenerator.nextString(12).allAtLeast(3).generate(), operation.getData());

        return true;
    }

}
