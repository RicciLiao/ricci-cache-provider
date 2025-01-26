package ricciliao.cache.service;

import ricciliao.cache.pojo.bo.WrapperIdentifierBo;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;
import ricciliao.common.component.exception.CmnException;

public interface CacheService {

    boolean create(WrapperIdentifierBo identifier,
                   ConsumerOperationDto<? extends RedisCacheBo> operation) throws CmnException;

}
