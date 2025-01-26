package ricciliao.cache.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.aspect.ControllerAspect;
import ricciliao.dynamic.aop.DynamicPointcutAdvisor;

import java.util.TimeZone;

@Configuration
public class CacheProviderBeanConfig {

    private CacheProviderProps applicationProperties;

    @Autowired
    public void setApplicationProperties(CacheProviderProps cacheProviderProps) {
        this.applicationProperties = cacheProviderProps;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setTimeZone(TimeZone.getTimeZone(applicationProperties.getTimeZone()));
        // objectMapper java.time.LocalDate/LocalDateTime
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    @Bean
    public DynamicPointcutAdvisor controllerAspect() {

        return new DynamicPointcutAdvisor(
                applicationProperties.getDynamicAopPointCutController(),
                new ControllerAspect()
        );
    }

}
