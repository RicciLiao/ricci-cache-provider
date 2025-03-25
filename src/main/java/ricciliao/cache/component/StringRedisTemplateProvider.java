package ricciliao.cache.component;


import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import ricciliao.cache.configuration.redis.RedisCacheAutoProperties;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StringRedisTemplateProvider extends CacheProvider {

    private final RedisTemplate<String, CacheDto> redisTemplate;

    public StringRedisTemplateProvider(ConsumerIdentifierDto consumerIdentifier,
                                       RedisCacheAutoProperties.ConsumerProperties.StoreProperties props,
                                       RedisTemplate<String, CacheDto> redisTemplate) {
        super(consumerIdentifier, props.getAddition().getTtl());
        this.redisTemplate = redisTemplate;
    }

    public boolean create(ConsumerOpDto.Single<CacheDto> operation) {
        Duration duration = Objects.isNull(operation.getTtlOfMillis()) ? this.getTtl() : Duration.ofMillis(operation.getTtlOfMillis());

        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(operation.getData().getId(), operation.getData(), duration)
        );
    }

    public boolean update(ConsumerOpDto.Single<CacheDto> operation) {
        Duration duration = Objects.isNull(operation.getTtlOfMillis()) ?
                this.getTtl() : Duration.ofMillis(operation.getTtlOfMillis());

        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfPresent(operation.getId(), operation.getData(), duration)
        );
    }

    public ConsumerOpDto.Single<CacheDto> get(String key) {
        Long ttlOfMillis = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        CacheDto data = redisTemplate.opsForValue().get(key);

        return new ConsumerOpDto.Single<>(data, ttlOfMillis);
    }

    public boolean delete(String key) {

        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public ConsumerOpDto.Batch<CacheDto> list() {
        return null;
    }

    @Override
    public boolean create(ConsumerOpDto.Batch<CacheDto> operation) {
        for (CacheDto cache : operation.getData()) {
            if (!this.create(new ConsumerOpDto.Single<>(cache, operation.getTtlOfMillis()))) {

                return false;
            }
        }

        return true;
    }

    @Override
    public ProviderInfoDto getProviderInfo() {
        ProviderInfoDto result = new ProviderInfoDto(this.getConsumerIdentifier());
        Set<String> keySet = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match("*").count(500).build();
        try (Cursor<String> cursor = redisTemplate.scan(options);) {
            while (cursor.hasNext()) {
                keySet.add(cursor.next());
            }
        }
        List<CacheDto> cacheList = redisTemplate.opsForValue().multiGet(keySet);
        if (CollectionUtils.isNotEmpty(cacheList)) {
            cacheList.sort(Comparator.comparing(CacheDto::getUpdatedDtm).reversed());
            result.setCreatedDtm(cacheList.get(0).getEffectedDtm());
            result.setMaxUpdatedDtm(cacheList.get(0).getUpdatedDtm());
            result.setCount((long) cacheList.size());
        }

        return result;
    }

}
