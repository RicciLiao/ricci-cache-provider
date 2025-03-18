package ricciliao.cache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ricciliao.cache.service.CacheService;
import ricciliao.x.component.cache.consumer.ConsumerIdentifier;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;
import ricciliao.x.component.response.ResponseData;
import ricciliao.x.component.response.ResponseSimpleData;
import ricciliao.x.component.response.ResponseUtils;
import ricciliao.x.component.response.ResponseVo;

@Tag(name = "Cache Operation Controller")
@RestController
@RequestMapping("/operation")
public class CacheOperationController {

    private CacheService redisCacheService;

    @Autowired
    public void setRedisCacheService(CacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Operation(description = "Create a new record for consumer.")
    @PostMapping("")
    public ResponseVo<ResponseData> create(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                           @RequestBody ConsumerOperationDto<CacheDto> operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Str(redisCacheService.create(identifier, operation)));
    }

    @Operation(description = "Update a existed record for consumer.")
    @PutMapping("")
    public ResponseVo<ResponseData> update(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                           @RequestBody ConsumerOperationDto<CacheDto> operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(redisCacheService.update(identifier, operation)));
    }

    @Operation(description = "Delete a existed record for consumer.")
    @DeleteMapping("/{id}")
    public ResponseVo<ResponseData> delete(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                           @PathVariable String id) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(redisCacheService.delete(identifier, id)));
    }

    @Operation(description = "Retrieve a existed record for consumer.")
    @GetMapping("/{id}")
    public ResponseVo<ResponseData> get(@ConsumerIdentifier ConsumerIdentifierDto identifier,
                                        @PathVariable(name = "id") String id) {

        return ResponseUtils.successResponse(redisCacheService.get(identifier, id));
    }


    private ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Operation(description = "Print Spring Beans.")
    @GetMapping("/springBean")
    public void springBean() {
        for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
            System.out.println(applicationContext.getBean(beanDefinitionName).getClass() + " - " + beanDefinitionName);
        }
    }

}
