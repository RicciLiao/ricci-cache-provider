package ricciliao.cache.component;

import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ricciliao.x.cache.ConsumerId;
import ricciliao.x.cache.XCacheConstants;
import ricciliao.x.cache.pojo.ConsumerIdentifier;

public class ConsumerIdentifierResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(ConsumerId.class)
                && parameter.getParameterType().isAssignableFrom(ConsumerIdentifier.class);
    }

    @Override
    public ConsumerIdentifier resolveArgument(@Nonnull MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {

        return new ConsumerIdentifier(
                webRequest.getHeader(XCacheConstants.HTTP_HEADER_FOR_CACHE_CUSTOMER),
                webRequest.getHeader(XCacheConstants.HTTP_HEADER_FOR_CACHE_STORE)
        );
    }

}
