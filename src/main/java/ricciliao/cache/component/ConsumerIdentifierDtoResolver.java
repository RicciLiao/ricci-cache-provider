package ricciliao.cache.component;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ricciliao.cache.annotation.ConsumerIdentifier;
import ricciliao.common.component.cache.Constants;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;

public class ConsumerIdentifierDtoResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(ConsumerIdentifier.class)
                && parameter.getParameterType().isAssignableFrom(ConsumerIdentifierDto.class);
    }

    @Override
    public ConsumerIdentifierDto resolveArgument(@NonNull MethodParameter parameter,
                                                 ModelAndViewContainer mavContainer,
                                                 NativeWebRequest webRequest,
                                                 WebDataBinderFactory binderFactory) {

        return new ConsumerIdentifierDto(
                webRequest.getHeader(Constants.HTTP_HEADER_FOR_CACHE_CUSTOMER),
                webRequest.getHeader(Constants.HTTP_HEADER_FOR_CACHE_STORE)
        );
    }

}
