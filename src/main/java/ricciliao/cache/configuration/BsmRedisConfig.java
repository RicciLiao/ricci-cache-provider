package ricciliao.cache.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.component.RedisCacheProvider;
import ricciliao.cache.pojo.bo.CaptchaRedisBo;
import ricciliao.cache.pojo.bo.EmailRedisBo;

@Configuration
public class BsmRedisConfig extends StringRedisWrapperConfig {


    public BsmRedisConfig(@Autowired RedisCacheProvider cacheProvider,
                          @Autowired ObjectMapper objectMapper,
                          @Autowired CacheProviderProps cacheProviderProps) {
        super(cacheProvider, objectMapper, cacheProviderProps);
    }

    @Override
    public void createWrappers() {
        this.createWrapper(CaptchaRedisBo.class, cacheProviderProps.getCaptchaRedisProps());
        this.createWrapper(EmailRedisBo.class, cacheProviderProps.getEmailRedisProps());
    }
}
