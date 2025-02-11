package ricciliao.cache.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.configuration.CacheProviderProps;
import ricciliao.cache.pojo.dto.MessageCodeRedisDto;
import ricciliao.common.component.cache.CacheProviderSelector;

@Configuration
public class MessageRedisConfig extends StringRedisWrapperConfig {

    public MessageRedisConfig(@Autowired CacheProviderSelector providerSelector,
                              @Autowired ObjectMapper objectMapper,
                              @Autowired CacheProviderProps cacheProviderProps) {
        super(providerSelector, objectMapper, cacheProviderProps);
    }

    @Override
    public void createWrappers() {
        this.createWrapper(MessageCodeRedisDto.class, cacheProviderProps.getMessageRedisProps());
    }

}
