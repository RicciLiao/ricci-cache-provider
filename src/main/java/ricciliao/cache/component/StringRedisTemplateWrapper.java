package ricciliao.cache.component;


import org.springframework.data.redis.core.RedisTemplate;
import ricciliao.common.component.cache.pojo.ConsumerOperationDto;
import ricciliao.common.component.cache.pojo.RedisCacheDto;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StringRedisTemplateWrapper {

    private final RedisTemplate<String, RedisCacheDto> redisTemplate;
    private final Duration ttl;

    public StringRedisTemplateWrapper(RedisTemplate<String, RedisCacheDto> redisTemplate, Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    public boolean create(ConsumerOperationDto<RedisCacheDto> operation) {
        Duration duration = Objects.isNull(operation.getTtlOfMillis()) ? this.ttl : Duration.ofMillis(operation.getTtlOfMillis());

        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(operation.getId(), operation.getData(), duration)
        );
    }

    public boolean update(ConsumerOperationDto<RedisCacheDto> operation) {
        Duration duration = Objects.isNull(operation.getTtlOfMillis()) ?
                this.ttl : Duration.ofMillis(operation.getTtlOfMillis());

        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfPresent(operation.getId(), operation.getData(), duration)
        );
    }

    public ConsumerOperationDto<RedisCacheDto> get(String key) {
        Long ttlOfMillis = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        RedisCacheDto data = redisTemplate.opsForValue().get(key);

        return new ConsumerOperationDto<>(key, ttlOfMillis, data);
    }

    public boolean delete(String key) {

        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

}
