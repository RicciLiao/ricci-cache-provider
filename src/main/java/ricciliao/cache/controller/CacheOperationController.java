package ricciliao.cache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ricciliao.cache.annotation.ConsumerIdentifier;
import ricciliao.cache.service.CacheService;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.cache.pojo.ConsumerOperationDto;
import ricciliao.common.component.cache.pojo.CacheDto;
import ricciliao.common.component.response.ResponseData;
import ricciliao.common.component.response.ResponseSimpleData;
import ricciliao.common.component.response.ResponseUtils;
import ricciliao.common.component.response.ResponseVo;

@Tag(name = "BsmController")
@RestController
public class CacheOperationController {

    private CacheService redisCacheService;

    @Autowired
    public void setRedisCacheService(CacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Operation(description = "Will create a new record for consumer.")
    @PostMapping("/operation")
    public ResponseVo<ResponseData> create(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                           @RequestBody ConsumerOperationDto<CacheDto> operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(redisCacheService.create(identifier, operation)));
    }

    @Operation(description = "Will update a existed record for consumer.")
    @PutMapping("/operation")
    public ResponseVo<ResponseData> update(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                           @RequestBody ConsumerOperationDto<CacheDto> operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(redisCacheService.update(identifier, operation)));
    }

    @Operation(description = "Will delete a existed record for consumer.")
    @DeleteMapping("/operation/{id}")
    public ResponseVo<ResponseData> delete(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                           @PathVariable String id) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(redisCacheService.delete(identifier, id)));
    }

    @Operation(description = "Will retrieve a existed record for consumer.")
    @GetMapping("/operation/{id}")
    public ResponseVo<ResponseData> get(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                        @PathVariable(name = "id") String id) {

        return ResponseUtils.successResponse(redisCacheService.get(identifier, id));
    }

}
