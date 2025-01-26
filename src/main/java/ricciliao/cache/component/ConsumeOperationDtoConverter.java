package ricciliao.cache.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import ricciliao.cache.pojo.bo.WrapperIdentifierBo;
import ricciliao.common.component.cache.ConsumerOperationData;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

public class ConsumeOperationDtoConverter extends AbstractHttpMessageConverter<ConsumerOperationDto<? extends RedisCacheBo>> {

    private final ObjectMapper objectMapper;
    private final RedisCacheProvider cacheProvider;

    public ConsumeOperationDtoConverter(ObjectMapper objectMapper, RedisCacheProvider cacheProvider) {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
        this.cacheProvider = cacheProvider;
    }

    @Override
    protected boolean supports(@NonNull Class<?> clazz) {

        return ConsumerOperationDto.class.isAssignableFrom(clazz);
    }

    @NonNull
    @Override
    protected ConsumerOperationDto<? extends RedisCacheBo> readInternal(@NonNull Class<? extends ConsumerOperationDto<? extends RedisCacheBo>> clazz,
                                                                        @NonNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {

        Optional<Field> dataFieldOptional = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConsumerOperationData.class)).findFirst();
        if (dataFieldOptional.isEmpty()) {

            throw new HttpMessageNotReadableException("missing @interface " + ConsumerOperationData.class.getSimpleName(), inputMessage);
        }
        try {

            return objectMapper.readValue(inputMessage.getBody(), new TypeReference<>() {
                @Override
                public Type getType() {
                    WrapperIdentifierBo identifier =
                            objectMapper.convertValue(inputMessage.getHeaders().toSingleValueMap(), new TypeReference<>() {
                            }); // TODO cannot convert directly...

                    return objectMapper.getTypeFactory().constructParametricType(
                            ConsumerOperationDto.class, cacheProvider.getCacheClass(identifier)
                    );
                }
            });
        } catch (Exception e) {

            return null;
        }
    }

    @Override
    protected void writeInternal(@NonNull ConsumerOperationDto<? extends RedisCacheBo> consumerOperationDto,
                                 @NonNull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        objectMapper.writeValue(outputMessage.getBody(), consumerOperationDto);
    }

}
