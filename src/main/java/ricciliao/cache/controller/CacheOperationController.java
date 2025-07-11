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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ricciliao.cache.ProviderOp;
import ricciliao.cache.service.CacheService;
import ricciliao.x.cache.annotation.ConsumerId;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.query.CacheBatchQuery;
import ricciliao.x.component.response.ResponseData;
import ricciliao.x.component.response.ResponseSimpleData;
import ricciliao.x.component.response.ResponseUtils;
import ricciliao.x.component.response.ResponseVo;

@Tag(name = "Cache Operation Controller")
@RestController
@RequestMapping("/operation")
public class CacheOperationController {

    private CacheService cacheService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Operation(description = "Create a new record for the consumer(with identifier).")
    @PostMapping("")
    public ResponseVo<ResponseData> create(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody ProviderOp.Single operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Str(cacheService.create(identifier, operation)));
    }

    @Operation(description = "Update a existed record for the consumer(with identifier).")
    @PutMapping("")
    public ResponseVo<ResponseData> update(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody ProviderOp.Single operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(cacheService.update(identifier, operation)));
    }

    @Operation(description = "Delete a existed record for the consumer(with identifier).")
    @DeleteMapping("/{id}")
    public ResponseVo<ResponseData> delete(@ConsumerId ConsumerIdentifier identifier,
                                           @PathVariable String id) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(cacheService.delete(identifier, id)));
    }

    @Operation(description = "Retrieve a existed record for the consumer(with identifier).")
    @GetMapping("/{id}")
    public ProviderOp.Single get(@ConsumerId ConsumerIdentifier identifier,
                                 @PathVariable(name = "id") String id) {

        return cacheService.get(identifier, id);
    }

    @Operation(description = "Batch create new records for the consumer(with identifier).")
    @PostMapping("/batch")
    public ResponseVo<ResponseData> create(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody ProviderOp.Batch operation) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(cacheService.create(identifier, operation)));
    }

    @Operation(description = "Batch delete existed records for the consumer(with identifier).")
    @DeleteMapping("/batch")
    public ResponseVo<ResponseData> delete(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody CacheBatchQuery query) {

        return ResponseUtils.successResponse(new ResponseSimpleData.Bool(cacheService.delete(identifier, query)));
    }

    @Operation(description = "Retrieve list of existed record for the consumer(with identifier).")
    @PostMapping("/list")
    public ProviderOp.Batch list(@ConsumerId ConsumerIdentifier identifier,
                                 @RequestBody CacheBatchQuery query) {

        return cacheService.list(identifier, query);
    }

}
