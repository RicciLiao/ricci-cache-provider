package ricciliao.cache.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ricciliao.cache.configuration.mongo.MongoCacheAutoProperties;
import ricciliao.cache.configuration.redis.RedisCacheAutoProperties;
import ricciliao.x.component.props.ApplicationProperties;
import ricciliao.x.starter.XProperties;

@Configuration("cacheProviderProperties")
public class CacheProviderProperties extends ApplicationProperties {


    public CacheProviderProperties(@Autowired(required = false) RedisCacheAutoProperties redisCacheProperties,
                                   @Autowired(required = false) MongoCacheAutoProperties mongoCacheProperties,
                                   @Autowired XProperties xProperties) {
        super();
        this.redisCacheProps = redisCacheProperties;
        this.mongoCacheProps = mongoCacheProperties;
        this.xProps = xProperties;
    }

    private final RedisCacheAutoProperties redisCacheProps;
    private final MongoCacheAutoProperties mongoCacheProps;
    private final XProperties xProps;

    public RedisCacheAutoProperties getRedisCacheProps() {
        return redisCacheProps;
    }

    public MongoCacheAutoProperties getMongoCacheProps() {
        return mongoCacheProps;
    }

    public XProperties getxProps() {
        return xProps;
    }
}
