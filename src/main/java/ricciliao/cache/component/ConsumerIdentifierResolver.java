package ricciliao.cache.component;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ricciliao.x.cache.ConsumerIdentifier;
import ricciliao.x.cache.XCacheConstants;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;

public class ConsumerIdentifierResolver implements HandlerMethodArgumentResolver {

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
                webRequest.getHeader(XCacheConstants.HTTP_HEADER_FOR_CACHE_CUSTOMER),
                webRequest.getHeader(XCacheConstants.HTTP_HEADER_FOR_CACHE_STORE)
        );
    }

}
