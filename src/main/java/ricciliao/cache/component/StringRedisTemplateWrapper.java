package ricciliao.cache.component;


import org.springframework.data.redis.core.RedisTemplate;
import ricciliao.common.component.cache.RedisCacheBo;

import java.time.Duration;

public record StringRedisTemplateWrapper(RedisTemplate<String, RedisCacheBo> redisTemplate,
                                         Duration ttl) {

    public void set(String key, RedisCacheBo value) {

        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public RedisCacheBo get(String key) {

        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {

        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

}
