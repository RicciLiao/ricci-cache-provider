package ricciliao.cache.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.configuration.redis.RedisCacheProperties;
import ricciliao.common.component.props.DefaultProperties;

@Configuration
public class CacheProviderProperties extends DefaultProperties {


    public CacheProviderProperties(@Autowired RedisCacheProperties redisCacheProperties) {
        super();
        this.redisCacheProps = redisCacheProperties;
    }

    private final RedisCacheProperties redisCacheProps;

    public RedisCacheProperties getRedisCacheProps() {
        return redisCacheProps;
    }
}
