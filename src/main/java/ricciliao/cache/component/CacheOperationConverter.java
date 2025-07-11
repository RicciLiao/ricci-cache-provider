package ricciliao.cache.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import ricciliao.cache.ProviderOp;
import ricciliao.x.cache.XCacheConstants;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.component.exception.CmnParameterException;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public abstract class CacheOperationConverter<T extends ProviderOp<? extends Serializable>> extends AbstractHttpMessageConverter<T> {

    protected final ObjectMapper objectMapper;
    protected final CacheProviderSelector cacheProvider;

    public CacheOperationConverter(ObjectMapper objectMapper, CacheProviderSelector cacheProvider) {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
        this.cacheProvider = cacheProvider;
    }

    protected ConsumerIdentifier verify(HttpInputMessage inputMessage) throws CmnParameterException {
        List<String> customer = inputMessage.getHeaders().get(XCacheConstants.HTTP_HEADER_FOR_CACHE_CUSTOMER);
        List<String> store = inputMessage.getHeaders().get(XCacheConstants.HTTP_HEADER_FOR_CACHE_STORE);
        if (CollectionUtils.isEmpty(customer) || customer.size() > 1
                || CollectionUtils.isEmpty(store) || store.size() > 1) {

            throw new CmnParameterException();
        }
        ConsumerIdentifier identifier = new ConsumerIdentifier(customer.getFirst(), store.getFirst());
        if (!cacheProvider.getCacheProviderMap().containsKey(identifier)) {

            throw new CmnParameterException();
        }

        return identifier;
    }

    @Nonnull
    @Override
    protected T readInternal(@Nonnull Class<? extends T> clazz, @Nonnull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {

        try {
            this.verify(inputMessage);

            return this.readInternal(inputMessage);
        } catch (Exception e) {

            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }
    }

    @Nonnull
    abstract T readInternal(@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException;

}
