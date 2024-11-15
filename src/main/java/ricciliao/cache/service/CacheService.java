package ricciliao.cache.service;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import ricciliao.common.component.cache.ConsumerIdentifierDto;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;
import ricciliao.common.component.exception.CmnException;

public interface CacheService {

    boolean create(@ModelAttribute ConsumerIdentifierDto identifier,
                   @RequestBody ConsumerOperationDto<? extends RedisCacheBo> operation) throws CmnException;

}
