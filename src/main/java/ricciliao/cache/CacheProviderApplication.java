package ricciliao.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ricciliao.common.component.cache.CacheProviderSelector;
import ricciliao.common.component.cache.ConsumeOperationDtoConverter;
import ricciliao.common.component.cache.ConsumerIdentifierDtoResolver;

import java.util.List;


@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
public class CacheProviderApplication extends SpringBootServletInitializer implements WebMvcConfigurer {

    private ObjectMapper objectMapper;
    private CacheProviderSelector providerSelector;

    @Autowired
    public void setProviderSelector(CacheProviderSelector providerSelector) {
        this.providerSelector = providerSelector;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new ConsumeOperationDtoConverter(objectMapper, providerSelector));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ConsumerIdentifierDtoResolver());
    }

    public static void main(String[] args) {
        SpringApplication.run(CacheProviderApplication.class, args);
    }

}
