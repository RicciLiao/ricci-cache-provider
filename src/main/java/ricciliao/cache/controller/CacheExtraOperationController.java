package ricciliao.cache.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ricciliao.cache.service.CacheService;
import ricciliao.x.cache.annotation.ConsumerId;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.component.response.ResponseData;
import ricciliao.x.component.response.ResponseUtils;
import ricciliao.x.component.response.ResponseVo;

@Tag(name = "Cache Extra Operation Controller")
@RestController
@RequestMapping("/operation/extra")
public class CacheExtraOperationController {

    private CacheService cacheService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Operation(description = "Retrieve provider information for the consumer(with identifier).")
    @GetMapping("/providerInfo")
    public ResponseVo<ResponseData> providerInfo(@ConsumerId ConsumerIdentifier identifier) {

        return ResponseUtils.successResponse(cacheService.providerInfo(identifier));
    }

}
