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
import ricciliao.cache.pojo.ProviderOp;
import ricciliao.cache.service.CacheService;
import ricciliao.x.cache.annotation.ConsumerId;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.query.CacheBatchQuery;
import ricciliao.x.component.response.Response;
import ricciliao.x.component.response.ResponseUtils;
import ricciliao.x.component.response.data.ResponseData;
import ricciliao.x.component.response.data.SimpleData;

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
    public Response<ResponseData> create(@ConsumerId ConsumerIdentifier identifier,
                                         @RequestBody ProviderOp.Single operation) {

        return ResponseUtils.success(SimpleData.of(cacheService.create(identifier, operation)));
    }

    @Operation(description = "Update a existed record for the consumer(with identifier).")
    @PutMapping("")
    public Response<ResponseData> update(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody ProviderOp.Single operation) {

        return ResponseUtils.success(SimpleData.of(cacheService.update(identifier, operation)));
    }

    @Operation(description = "Delete a existed record for the consumer(with identifier).")
    @DeleteMapping("/{id}")
    public Response<ResponseData> delete(@ConsumerId ConsumerIdentifier identifier,
                                           @PathVariable String id) {

        return ResponseUtils.success(SimpleData.of(cacheService.delete(identifier, id)));
    }

    @Operation(description = "Retrieve a existed record for the consumer(with identifier).")
    @GetMapping("/{id}")
    public ProviderOp.Single get(@ConsumerId ConsumerIdentifier identifier,
                                 @PathVariable(name = "id") String id) {

        return cacheService.get(identifier, id);
    }

    @Operation(description = "Batch create new records for the consumer(with identifier).")
    @PostMapping("/batch")
    public Response<ResponseData> create(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody ProviderOp.Batch operation) {

        return ResponseUtils.success(SimpleData.of(cacheService.create(identifier, operation)));
    }

    @Operation(description = "Batch delete existed records for the consumer(with identifier).")
    @DeleteMapping("/batch")
    public Response<ResponseData> delete(@ConsumerId ConsumerIdentifier identifier,
                                           @RequestBody CacheBatchQuery query) {

        return ResponseUtils.success(SimpleData.of(cacheService.delete(identifier, query)));
    }

    @Operation(description = "Retrieve list of existed record for the consumer(with identifier).")
    @PostMapping("/list")
    public ProviderOp.Batch list(@ConsumerId ConsumerIdentifier identifier,
                                 @RequestBody CacheBatchQuery query) {

        return cacheService.list(identifier, query);
    }

}
