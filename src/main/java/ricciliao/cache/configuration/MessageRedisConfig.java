package ricciliao.cache.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.component.RedisCacheProvider;
import ricciliao.cache.pojo.bo.MessageCodeBo;

@Configuration
public class MessageRedisConfig extends StringRedisWrapperConfig {

    public MessageRedisConfig(@Autowired RedisCacheProvider cacheProvider,
                              @Autowired ObjectMapper objectMapper,
                              @Autowired CacheProviderProps cacheProviderProps) {
        super(cacheProvider, objectMapper, cacheProviderProps);
    }

    @Override
    public void createWrappers() {
        this.createWrapper(MessageCodeBo.class, cacheProviderProps.getMessageRedisProps());
    }

}
