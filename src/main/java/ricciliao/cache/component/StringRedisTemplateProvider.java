package ricciliao.cache.component;


import org.springframework.data.redis.core.RedisTemplate;
import ricciliao.x.component.cache.pojo.CacheDto;
import ricciliao.x.component.cache.pojo.ConsumerOperationDto;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StringRedisTemplateProvider extends CacheProvider {

    private final RedisTemplate<String, CacheDto> redisTemplate;

    public StringRedisTemplateProvider(RedisTemplate<String, CacheDto> redisTemplate,
                                       Duration ttl) {
        super(ttl);
        this.redisTemplate = redisTemplate;
    }

    public boolean create(ConsumerOperationDto<CacheDto> operation) {
        Duration duration = Objects.isNull(operation.getTtlOfMillis()) ? this.getTtl() : Duration.ofMillis(operation.getTtlOfMillis());

        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(operation.getId(), operation.getData(), duration)
        );
    }

    public boolean update(ConsumerOperationDto<CacheDto> operation) {
        Duration duration = Objects.isNull(operation.getTtlOfMillis()) ?
                this.getTtl() : Duration.ofMillis(operation.getTtlOfMillis());

        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfPresent(operation.getId(), operation.getData(), duration)
        );
    }

    public ConsumerOperationDto<CacheDto> get(String key) {
        Long ttlOfMillis = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        CacheDto data = redisTemplate.opsForValue().get(key);

        return new ConsumerOperationDto<>(key, ttlOfMillis, data);
    }

    public boolean delete(String key) {

        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public List<ConsumerOperationDto<CacheDto>> list() {
        return null;
    }

}
