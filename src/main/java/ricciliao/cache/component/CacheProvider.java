package ricciliao.cache.component;


import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanCreationException;
import ricciliao.x.cache.CacheQuery;
import ricciliao.x.cache.ProviderCacheProperties;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.cache.pojo.ConsumerOpBatchQueryDto;
import ricciliao.x.cache.pojo.ConsumerOpDto;
import ricciliao.x.cache.pojo.ProviderInfoDto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public abstract class CacheProvider {

    private final CacheProviderConstruct constr;
    private final Map<CacheQuery.Property, String> property2NameSortMap = new EnumMap<>(CacheQuery.Property.class);

    protected CacheProvider(CacheProviderConstruct cacheProviderConstruct) {
        this.constr = cacheProviderConstruct;
        Field[] fields = CacheDto.class.getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(CacheQuery.Support.class))
                .forEach(field -> {
                    CacheQuery.Support sortProperty = field.getAnnotation(CacheQuery.Support.class);
                    this.getProperty2NameSortMap().put(sortProperty.value(), field.getName());
                });
        if (MapUtils.isEmpty(this.getProperty2NameSortMap())) {

            throw new BeanCreationException(
                    String.format(
                            "Initialize CacheProvider for consumer: [%s] failed!  Can not identify the SortProperty.",
                            this.constr.consumerIdentifier.toString()
                    )
            );
        }
    }

    public ConsumerIdentifierDto getConsumerIdentifier() {
        return this.constr.consumerIdentifier;
    }

    public Map<CacheQuery.Property, String> getProperty2NameSortMap() {
        return property2NameSortMap;
    }

    public ProviderCacheProperties.StoreProperties getStoreProps() {
        return this.constr.storeProps;
    }

    public ProviderCacheProperties.AdditionalProperties getAdditionalProps() {
        return this.constr.storeProps.getAddition();
    }

    public abstract boolean create(ConsumerOpDto.Single<CacheDto> operation);

    public abstract boolean update(ConsumerOpDto.Single<CacheDto> operation);

    public abstract ConsumerOpDto.Single<CacheDto> get(String key);

    public abstract boolean delete(String key);

    public abstract ConsumerOpDto.Batch<CacheDto> list(ConsumerOpBatchQueryDto query);

    public boolean create(ConsumerOpDto.Batch<CacheDto> operation) {
        for (CacheDto cache : operation.getData()) {
            if (!this.create(new ConsumerOpDto.Single<>(cache, operation.getTtlOfMillis()))) {

                return false;
            }
        }

        return true;
    }

    public abstract ProviderInfoDto getProviderInfo();

    public abstract static class CacheProviderConstruct {
        private ConsumerIdentifierDto consumerIdentifier;
        private ProviderCacheProperties.StoreProperties storeProps;

        public ConsumerIdentifierDto getConsumerIdentifier() {
            return consumerIdentifier;
        }

        public void setConsumerIdentifier(ConsumerIdentifierDto consumerIdentifier) {
            this.consumerIdentifier = consumerIdentifier;
        }

        public ProviderCacheProperties.StoreProperties getStoreProps() {
            return storeProps;
        }

        public void setStoreProps(ProviderCacheProperties.StoreProperties storeProps) {
            this.storeProps = storeProps;
        }
    }

}
