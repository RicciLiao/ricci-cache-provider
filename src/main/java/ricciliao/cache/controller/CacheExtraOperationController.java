package ricciliao.cache.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ricciliao.cache.service.CacheService;
import ricciliao.x.component.cache.consumer.ConsumerIdentifier;
import ricciliao.x.component.cache.pojo.CacheExtraOperationDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.response.ResponseCollectionData;
import ricciliao.x.component.response.ResponseData;
import ricciliao.x.component.response.ResponseUtils;
import ricciliao.x.component.response.ResponseVo;

@Tag(name = "Cache Extra Operation Controller")
@RestController
@RequestMapping("/operation/extra")
public class CacheExtraOperationController {

    private CacheService redisCacheService;

    @Autowired
    public void setRedisCacheService(CacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Operation(description = "Retrieve list of existed record for consumer.")
    @GetMapping("/list")
    public ResponseVo<ResponseData> list(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                         @ModelAttribute CacheExtraOperationDto operation) {

        return ResponseUtils.successResponse(ResponseCollectionData.data(redisCacheService.list(identifier, operation)));
    }

}
