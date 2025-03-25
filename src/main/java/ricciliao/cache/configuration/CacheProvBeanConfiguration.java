package ricciliao.cache.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.component.ConsumerIdentifierResolver;
import ricciliao.cache.component.ConsumerOpConverter;
import ricciliao.cache.configuration.mongo.MongoCacheAutoConfiguration;
import ricciliao.cache.configuration.redis.RedisCacheAutoConfiguration;

import java.util.List;

@Configuration
@ImportAutoConfiguration({
        MongoCacheAutoConfiguration.class,
        RedisCacheAutoConfiguration.class
})
public class CacheProvBeanConfiguration implements WebMvcConfigurer {

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public CacheProviderSelector providerSelector() {

        return new CacheProviderSelector();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new ConsumerOpConverter(objectMapper, providerSelector()));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ConsumerIdentifierResolver());
    }

}
