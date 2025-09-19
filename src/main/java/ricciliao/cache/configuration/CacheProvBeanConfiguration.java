package ricciliao.cache.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.component.ConsumerIdentifierResolver;
import ricciliao.cache.component.ProviderOpBatchConverter;
import ricciliao.cache.component.ProviderOpSingleConverter;
import ricciliao.cache.configuration.mongo.MongoCacheAutoConfiguration;
import ricciliao.cache.configuration.redis.RedisCacheAutoConfiguration;
import ricciliao.x.starter.common.CommonAutoConfiguration;

import java.util.List;

@Configuration
@ImportAutoConfiguration({
        MongoCacheAutoConfiguration.class,
        RedisCacheAutoConfiguration.class
})
@ComponentScan(
        excludeFilters =
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        CommonAutoConfiguration.CommonWebMvcConfiguration.class
                }
        )
)
public class CacheProvBeanConfiguration implements WebMvcConfigurer {

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new ProviderOpSingleConverter(objectMapper, providerSelector()));
        converters.add(1, new ProviderOpBatchConverter(objectMapper, providerSelector()));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ConsumerIdentifierResolver());
    }

    @Bean
    public CacheProviderSelector providerSelector() {

        return new CacheProviderSelector();
    }

}
