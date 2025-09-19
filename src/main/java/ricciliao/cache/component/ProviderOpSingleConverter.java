package ricciliao.cache.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.cache.pojo.ProviderOp;
import ricciliao.x.cache.pojo.CacheOperation;
import ricciliao.x.cache.pojo.CacheStore;
import ricciliao.x.component.response.code.ResponseCode;
import ricciliao.x.component.response.code.impl.ResponseCodeEnum;
import ricciliao.x.component.response.data.SimpleData;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Objects;

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
        ObjectNode responseNode = objectMapper.createObjectNode();
        if (Objects.isNull(single.getData())) {
            CacheOperation<SimpleData> op = new CacheOperation<>(SimpleData.blank());
            responseNode.set("data", objectMapper.valueToTree(SimpleData.blank()));
        } else {
            CacheOperation<CacheStore<LinkedHashMap<String, Serializable>>> op =
                    new CacheOperation<>(single.getTtlOfSeconds(), this.resume(single.getData()));
            responseNode.set("data", objectMapper.valueToTree(op));
        }

        ObjectNode codeNode = objectMapper.createObjectNode();
        ResponseCode code = ResponseCodeEnum.SUCCESS;
        codeNode.put("id", String.format("%d%03d", code.getPrimary().getId(), code.getSecondary().getId()));
        codeNode.put("message", code.getPrimary().getMessage());
        responseNode.set("code", codeNode);

        objectMapper.writeValue(outputMessage.getBody(), responseNode);
    }

    private CacheStore<LinkedHashMap<String, Serializable>> resume(ProviderCacheStore store) throws IOException {
        CacheStore<LinkedHashMap<String, Serializable>> result = new CacheStore<>();
        result.setCacheKey(store.getCacheKey());
        result.setCreatedDtm(store.getCreatedDtm());
        result.setUpdatedDtm(store.getUpdatedDtm());
        result.setEffectedDtm(store.getEffectedDtm());
        result.setData(objectMapper.readValue(objectMapper.readValue(store.getData(), String.class), new TypeReference<>() {
        }));

        return result;
    }
}
