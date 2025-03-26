package ricciliao.cache.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import ricciliao.x.cache.ConsumerData;
import ricciliao.x.cache.XCacheConstants;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.component.exception.CmnParameterException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConsumerOpConverter extends AbstractHttpMessageConverter<ConsumerOpDto> {

    private final ObjectMapper objectMapper;
    private final CacheProviderSelector cacheProvider;

    public ConsumerOpConverter(ObjectMapper objectMapper, CacheProviderSelector cacheProvider) {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
        this.cacheProvider = cacheProvider;
    }

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {

        return ConsumerOpDto.class.isAssignableFrom(clazz);
    }

    @Nonnull
    @Override
    protected ConsumerOpDto readInternal(@Nonnull Class<? extends ConsumerOpDto> clazz,
                                         @Nonnull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {

        Optional<Field> dataFieldOptional = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConsumerData.class)).findFirst();
        if (dataFieldOptional.isEmpty()) {

            throw new HttpMessageNotReadableException("missing @interface " + ConsumerData.class.getSimpleName(), inputMessage);
        }
        try {
            List<String> customer = inputMessage.getHeaders().get(XCacheConstants.HTTP_HEADER_FOR_CACHE_CUSTOMER);
            List<String> store = inputMessage.getHeaders().get(XCacheConstants.HTTP_HEADER_FOR_CACHE_STORE);
            if (CollectionUtils.isEmpty(customer) || customer.size() > 1
                    || CollectionUtils.isEmpty(store) || store.size() > 1) {

                throw new CmnParameterException();
            }
            ConsumerIdentifierDto identifier = new ConsumerIdentifierDto(customer.get(0), store.get(0));
            Class<? extends CacheDto> cacheClass = cacheProvider.getCacheClass(identifier);
            if (Objects.isNull(cacheClass)) {

                throw new CmnParameterException();
            }
            Type type = objectMapper.getTypeFactory().constructParametricType(clazz, cacheClass);

            return objectMapper.readValue(inputMessage.getBody(), new TypeReference<>() {
                @Override
                public Type getType() {

                    return type;
                }
            });
        } catch (Exception e) {

            throw new HttpMessageNotReadableException("can not convert to " + ConsumerIdentifierDto.class, inputMessage);
        }
    }

    @Override
    protected void writeInternal(@Nonnull ConsumerOpDto consumerOperationDto,
                                 @Nonnull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        objectMapper.writeValue(outputMessage.getBody(), consumerOperationDto);
    }

}
