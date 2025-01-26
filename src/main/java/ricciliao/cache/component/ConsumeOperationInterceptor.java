package ricciliao.cache.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.stream.Collectors;

public class ConsumeOperationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {


        HeaderRequestWrapper wrapper =
                new HeaderRequestWrapper(
                        request,
                        request.getParameterMap().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue()[0]
                                )));

        return HandlerInterceptor.super.preHandle(wrapper, response, handler);
    }
}
