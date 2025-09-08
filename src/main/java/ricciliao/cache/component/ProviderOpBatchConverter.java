package ricciliao.cache.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.cache.pojo.ProviderOp;
import ricciliao.x.cache.pojo.CacheOperation;
import ricciliao.x.cache.pojo.CacheStore;
import ricciliao.x.component.response.ResponseUtils;
import ricciliao.x.component.sneaky.SneakyThrowUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class ProviderOpBatchConverter extends CacheOperationConverter<ProviderOp.Batch> {

    public ProviderOpBatchConverter(ObjectMapper objectMapper,
                                    CacheProviderSelector cacheProvider) {
        super(objectMapper, cacheProvider);
    }

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {

        return ProviderOp.Batch.class.isAssignableFrom(clazz);
    }

    @Nonnull
    @Override
    protected ProviderOp.Batch readInternal(@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JsonNode jsonNode = objectMapper.readTree(inputMessage.getBody());
        CacheOperation<CacheStore<?>[]> op = objectMapper.convertValue(jsonNode, new TypeReference<>() {
        });
        ProviderCacheStore[] stores =
                Arrays.stream(op.getData()).map(store -> {
                            ProviderCacheStore providerCacheStore = new ProviderCacheStore();
                            providerCacheStore.setCacheKey(store.getCacheKey());
                            providerCacheStore.setCreatedDtm(store.getCreatedDtm());
                            providerCacheStore.setUpdatedDtm(store.getUpdatedDtm());
                            providerCacheStore.setEffectedDtm(store.getEffectedDtm());
                            String data = SneakyThrowUtils.get(() -> objectMapper.writeValueAsString(store.getData()));
                            providerCacheStore.setData(SneakyThrowUtils.get(() -> objectMapper.writeValueAsBytes(data)));

                            return providerCacheStore;
                        })
                        .toArray(ProviderCacheStore[]::new);
        op.setData(stores);

        return objectMapper.convertValue(op, new TypeReference<>() {
        });
    }

    @Override
    protected void writeInternal(@Nonnull ProviderOp.Batch batch, @Nonnull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        CacheOperation<CacheStore<Serializable>[]> op = objectMapper.convertValue(batch, new TypeReference<>() {
        });
        for (CacheStore<Serializable> datum : op.getData()) {
            String data = objectMapper.readValue((byte[]) datum.getData(), String.class);
            datum.setData((Serializable) objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            }));
        }


        objectMapper.writeValue(outputMessage.getBody(), ResponseUtils.success(op));
    }
}
