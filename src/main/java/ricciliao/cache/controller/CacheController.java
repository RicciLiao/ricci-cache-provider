package ricciliao.cache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import ricciliao.cache.service.CacheService;
import ricciliao.common.component.cache.ConsumerIdentifierDto;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;
import ricciliao.common.component.exception.CmnException;
import ricciliao.common.component.response.ResponseData;
import ricciliao.common.component.response.ResponseSimpleData;
import ricciliao.common.component.response.ResponseUtils;
import ricciliao.common.component.response.ResponseVo;

@Tag(name = "BsmController")
@RestController
public class CacheController {

    private CacheService redisCacheService;

    @Autowired
    public void setRedisCacheService(CacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Operation(description = "Will create a new record for consumer.")
    @PostMapping("/operation")
    public ResponseVo<ResponseData> create(@ModelAttribute ConsumerIdentifierDto identifier,
                                           ConsumerOperationDto<RedisCacheBo> operation) throws CmnException {

        return ResponseUtils.successResponse(
                new ResponseSimpleData.BooleanResult(redisCacheService.create(identifier, operation))
        );
    }

    @Operation(description = "Will update a existed record for consumer.")
    @PutMapping("/operation")
    public ResponseVo<ResponseData> update(@ModelAttribute ConsumerIdentifierDto identifier,
                                           ConsumerOperationDto<? extends RedisCacheBo> operation) {

        return ResponseUtils.successResponse();
    }

    @Operation(description = "Will delete a existed record for consumer.")
    @DeleteMapping("/operation")
    public ResponseVo<ResponseData> delete(@ModelAttribute ConsumerIdentifierDto identifier,
                                           ConsumerOperationDto<? extends RedisCacheBo> operation) {

        return ResponseUtils.successResponse();
    }

    @Operation(description = "Will retrieve a existed record for consumer.")
    @GetMapping("/operation")
    public ResponseVo<ResponseData> get(@ModelAttribute ConsumerIdentifierDto identifier,
                                        ConsumerOperationDto<? extends RedisCacheBo> operation) {

        return ResponseUtils.successResponse();
    }

}
