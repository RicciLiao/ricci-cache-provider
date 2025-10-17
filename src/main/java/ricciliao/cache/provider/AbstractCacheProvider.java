package ricciliao.cache.provider;


import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanCreationException;
import ricciliao.cache.pojo.ProviderCacheStore;
import ricciliao.cache.properties.ProviderCacheProperties;
import ricciliao.x.cache.pojo.CacheStore;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.cache.pojo.ProviderInfo;
import ricciliao.x.cache.query.CacheBatchQuery;
import ricciliao.x.cache.query.CacheQuery;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public abstract class AbstractCacheProvider {

    private final CacheProviderConstruct constr;
    private final Map<CacheQuery.Property, Field> property2NameSortMap = new EnumMap<>(CacheQuery.Property.class);

    protected AbstractCacheProvider(CacheProviderConstruct cacheProviderConstruct) {
        this.constr = cacheProviderConstruct;
        Field[] fields = CacheStore.class.getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(CacheQuery.Support.class))
                .forEach(field -> {
                    CacheQuery.Support sortProperty = field.getAnnotation(CacheQuery.Support.class);
                    this.property2NameSortMap.put(sortProperty.value(), field);
                });
        if (MapUtils.isEmpty(this.property2NameSortMap)) {

            throw new BeanCreationException(
                    String.format(
                            "Initialize CacheProvider for consumer: [%s] failed!  Can not identify the QueryProperty.",
                            this.constr.consumerIdentifier.toString()
                    )
            );
        }
    }

    public ConsumerIdentifier getConsumerIdentifier() {
        return this.constr.consumerIdentifier;
    }

    public Map<CacheQuery.Property, Field> getProperty2NameSortMap() {
        return property2NameSortMap;
    }

    public ProviderCacheProperties.StoreProperties getStoreProps() {
        return this.constr.storeProps;
    }

    public ProviderCacheProperties.AdditionalProperties getAdditionalProps() {
        return this.constr.storeProps.getAddition();
    }

    public abstract boolean create(ProviderCacheStore store);

    public abstract boolean update(ProviderCacheStore store);

    public abstract ProviderCacheStore get(String key);

    public abstract boolean delete(String key);

    public abstract ProviderCacheStore.Batch list(CacheBatchQuery query);

    public boolean create(ProviderCacheStore.Batch batch) {
        for (ProviderCacheStore store : batch.batch()) {
            if (!this.create(store)) {

                return false;
            }
        }

        return true;
    }

    public abstract boolean delete(CacheBatchQuery query);

    public abstract ProviderInfo getProviderInfo();

    public abstract static class CacheProviderConstruct {
        private ConsumerIdentifier consumerIdentifier;
        private ProviderCacheProperties.StoreProperties storeProps;

        public ConsumerIdentifier getConsumerIdentifier() {
            return consumerIdentifier;
        }

        public void setConsumerIdentifier(ConsumerIdentifier consumerIdentifier) {
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
