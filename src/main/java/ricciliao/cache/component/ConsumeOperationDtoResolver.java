package ricciliao.cache.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ricciliao.cache.pojo.bo.WrapperIdentifierBo;
import ricciliao.common.component.cache.ConsumerOperationData;
import ricciliao.common.component.cache.ConsumerIdentifierDto;
import ricciliao.common.component.cache.ConsumerOperationDto;
import ricciliao.common.component.cache.RedisCacheBo;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConsumeOperationDtoResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;
    private final RedisCacheProvider cacheProvider;

    public ConsumeOperationDtoResolver(ObjectMapper objectMapper,
                                       RedisCacheProvider cacheProvider) {
        this.objectMapper = objectMapper;
        this.cacheProvider = cacheProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.getParameterType().isAssignableFrom(ConsumerOperationDto.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (Objects.isNull(servletRequest)) {

            return null;
        }
        Optional<Field> dataFieldOptional = Arrays.stream(ConsumerOperationDto.class.getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConsumerOperationData.class)).findFirst();
        if (dataFieldOptional.isEmpty()) {

            return null;
        }
        ConsumerOperationDto<RedisCacheBo> result = null;
        try {
            ConsumerIdentifierDto identifier =
                    objectMapper.convertValue(
                            servletRequest.getParameterMap().entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            entry -> entry.getValue()[0]
                                    )),
                            new TypeReference<>() {
                            });
            Class<? extends RedisCacheBo> cacheClass = cacheProvider.getCacheClass(new WrapperIdentifierBo(identifier.getC(), identifier.getI()));
            JsonNode jsonNode = objectMapper.readTree(servletRequest.getReader().lines().collect(Collectors.joining()));
            JsonNode jsonDataNode = jsonNode.get(dataFieldOptional.get().getName());
            result = objectMapper.convertValue(jsonNode, new TypeReference<ConsumerOperationDto<RedisCacheBo>>() {
            });
            RedisCacheBo data = objectMapper.convertValue(jsonDataNode, new TypeReference<>() {
                @Override
                public Type getType() {

                    return cacheClass;
                }
            });
            result.setData(data);

            return result;
        } catch (Exception e) {

            return result;
        }
    }
}
