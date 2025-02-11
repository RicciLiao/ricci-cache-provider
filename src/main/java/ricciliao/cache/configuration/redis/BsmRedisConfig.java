package ricciliao.cache.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.configuration.CacheProviderProps;
import ricciliao.cache.pojo.dto.CaptchaRedisDto;
import ricciliao.cache.pojo.dto.EmailRedisDto;
import ricciliao.common.component.cache.CacheProviderSelector;

@Configuration
public class BsmRedisConfig extends StringRedisWrapperConfig {


    public BsmRedisConfig(@Autowired CacheProviderSelector providerSelector,
                          @Autowired ObjectMapper objectMapper,
                          @Autowired CacheProviderProps cacheProviderProps) {
        super(providerSelector, objectMapper, cacheProviderProps);
    }

    @Override
    public void createWrappers() {
        this.createWrapper(CaptchaRedisDto.class, cacheProviderProps.getCaptchaRedisProps());
        this.createWrapper(EmailRedisDto.class, cacheProviderProps.getEmailRedisProps());
    }
}
