package ricciliao.cache.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ricciliao.cache.configuration.redis.RedisCacheAutoConfiguration;
import ricciliao.common.component.cache.consumer.ConsumerIdentifierDtoResolver;
import ricciliao.common.component.cache.consumer.ConsumerOperationDtoConverter;
import ricciliao.common.component.cache.provider.CacheProviderSelector;

import java.util.List;
import java.util.TimeZone;

@Configuration
@ImportAutoConfiguration({
        RedisCacheAutoConfiguration.class
})
public class CacheProvBeanConfiguration implements WebMvcConfigurer {

    private CacheProviderProperties props;

    @Autowired
    public void setProps(CacheProviderProperties props) {
        this.props = props;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setTimeZone(TimeZone.getTimeZone(props.getTimeZone()));
        // objectMapper java.time.LocalDate/LocalDateTime
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    @Bean
    public CacheProviderSelector providerSelector() {

        return new CacheProviderSelector();
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new ConsumerOperationDtoConverter(objectMapper(), providerSelector()));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ConsumerIdentifierDtoResolver());
    }

}
