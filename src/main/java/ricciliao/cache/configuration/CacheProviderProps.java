package ricciliao.cache.configuration;

import org.springframework.context.annotation.Configuration;
import ricciliao.cache.configuration.redis.StringRedisWrapperConfig;
import ricciliao.common.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.common.component.context.ApplicationProperties;

import java.time.Duration;

@Configuration
public class CacheProviderProps extends ApplicationProperties {

    public CacheProviderProps() {
        super();
        this.timeZone = yamlProperties.getProperty("time-zone", String.class);
        this.redisHost = yamlProperties.getProperty("redis.host", String.class);
        this.redisPort = yamlProperties.getProperty("redis.port", Integer.class);
        this.password = yamlProperties.getProperty("redis.password", String.class);
        this.dynamicAopPointCutController = yamlProperties.getProperty("dynamic-aop.point-cut.controller", String.class);
        this.captchaRedisProps =
                new StringRedisWrapperConfig.RedisPropsBo(
                        redisHost,
                        redisPort,
                        password,
                        yamlProperties.getProperty("redis.db.bsm.captcha.database", Integer.class),
                        Duration.ofMillis(yamlProperties.getProperty("redis.db.bsm.captcha.timeout", Long.class)),
                        Duration.ofMillis(yamlProperties.getProperty("redis.db.bsm.captcha.ttl", Long.class)),
                        yamlProperties.getProperty("redis.db.bsm.captcha.pool.min-idle", Integer.class),
                        yamlProperties.getProperty("redis.db.bsm.captcha.pool.max-idle", Integer.class),
                        yamlProperties.getProperty("redis.db.bsm.captcha.pool.max-total", Integer.class),
                        new ConsumerIdentifierDto(
                                yamlProperties.getProperty("redis.db.bsm.consumer", String.class),
                                yamlProperties.getProperty("redis.db.bsm.captcha.store", String.class)
                        )
                );
        this.emailRedisProps =
                new StringRedisWrapperConfig.RedisPropsBo(
                        redisHost,
                        redisPort,
                        password,
                        yamlProperties.getProperty("redis.db.bsm.email.database", Integer.class),
                        Duration.ofMillis(yamlProperties.getProperty("redis.db.bsm.email.timeout", Long.class)),
                        Duration.ofMillis(yamlProperties.getProperty("redis.db.bsm.email.ttl", Long.class)),
                        yamlProperties.getProperty("redis.db.bsm.email.pool.min-idle", Integer.class),
                        yamlProperties.getProperty("redis.db.bsm.email.pool.max-idle", Integer.class),
                        yamlProperties.getProperty("redis.db.bsm.email.pool.max-total", Integer.class),
                        new ConsumerIdentifierDto(
                                yamlProperties.getProperty("redis.db.bsm.consumer", String.class),
                                yamlProperties.getProperty("redis.db.bsm.email.store", String.class)
                        )
                );
        this.messageRedisProps =
                new StringRedisWrapperConfig.RedisPropsBo(
                        redisHost,
                        redisPort,
                        password,
                        yamlProperties.getProperty("redis.db.message.code.database", Integer.class),
                        Duration.ofMillis(yamlProperties.getProperty("redis.db.message.code.timeout", Long.class)),
                        Duration.ofMillis(yamlProperties.getProperty("redis.db.message.code.ttl", Long.class)),
                        yamlProperties.getProperty("redis.db.message.code.pool.min-idle", Integer.class),
                        yamlProperties.getProperty("redis.db.message.code.pool.max-idle", Integer.class),
                        yamlProperties.getProperty("redis.db.message.code.pool.max-total", Integer.class),
                        new ConsumerIdentifierDto(
                                yamlProperties.getProperty("redis.db.message.consumer", String.class),
                                yamlProperties.getProperty("redis.db.message.code.store", String.class)
                        )
                );
    }

    private final String timeZone;
    private final String dynamicAopPointCutController;
    private final String redisHost;
    private final Integer redisPort;
    private final String password;
    private final StringRedisWrapperConfig.RedisPropsBo captchaRedisProps;
    private final StringRedisWrapperConfig.RedisPropsBo emailRedisProps;
    private final StringRedisWrapperConfig.RedisPropsBo messageRedisProps;

    public String getTimeZone() {
        return timeZone;
    }

    public String getDynamicAopPointCutController() {
        return dynamicAopPointCutController;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public String getPassword() {
        return password;
    }

    public StringRedisWrapperConfig.RedisPropsBo getCaptchaRedisProps() {
        return captchaRedisProps;
    }

    public StringRedisWrapperConfig.RedisPropsBo getEmailRedisProps() {
        return emailRedisProps;
    }

    public StringRedisWrapperConfig.RedisPropsBo getMessageRedisProps() {
        return messageRedisProps;
    }
}
