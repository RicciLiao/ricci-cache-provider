package ricciliao.cache.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import ricciliao.cache.pojo.ProviderOp;
import ricciliao.x.cache.pojo.CacheOperation;
import ricciliao.x.cache.pojo.CacheStore;
import ricciliao.x.component.response.ResponseUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class ProviderOpSingleConverter extends CacheOperationConverter<ProviderOp.Single> {

    public ProviderOpSingleConverter(ObjectMapper objectMapper,
                                     CacheProviderSelector cacheProvider) {
        super(objectMapper, cacheProvider);
    }

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {

        return ProviderOp.Single.class.isAssignableFrom(clazz);
    }

    @Nonnull
    @Override
    ProviderOp.Single readInternal(@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JsonNode jsonNode = objectMapper.readTree(inputMessage.getBody());
        CacheOperation<CacheStore<Serializable>> op = objectMapper.convertValue(jsonNode, new TypeReference<>() {
        });
        String data = objectMapper.writeValueAsString(op.getData().getData());
        op.getData().setData(objectMapper.writeValueAsBytes(data));

        return objectMapper.convertValue(op, new TypeReference<>() {
        });
    }

    @Override
    protected void writeInternal(@Nonnull ProviderOp.Single single, @Nonnull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        String data = objectMapper.readValue(single.getData().getData(), String.class);
        CacheOperation<CacheStore<Serializable>> op = objectMapper.convertValue(single, new TypeReference<>() {
        });
        op.getData().setData((Serializable) objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
        }));

        objectMapper.writeValue(outputMessage.getBody(), ResponseUtils.successResponse(op));
    }
}
