# Cache Store CURD Interface Service

## *Cache Provider `🚀️ V1.0.0`*

### 📚 Dependency

Please refer to `dependencies-control-center` for the version number.

| groupId                  | artifactId                          | scope   |
|--------------------------|-------------------------------------|---------|
| org.springframework.boot | spring-boot-starter-web             | compile |
| org.springdoc            | springdoc-openapi-starter-webmvc-ui | compile |
| org.springframework.boot | spring-boot-starter-data-mongodb    | compile |
| org.springframework.boot | spring-boot-starter-validation      | compile |
| redis.clients            | jedis                               | compile |
| ricciliao.x              | components-starter                  | compile |
| ricciliao.x              | cache-common-component              | compile |
| jakarta.servlet          | jakarta.servlet-api                 | compile |

### 📌 Usage

**Cache Provider** provides a series of universal RESTful interfaces for MongoDB and Redis,
you can choose MongoDB or Redis as your provider(s) for your data,
also, you can use more than one provider(s) at the same times if you need to cache dataA into MongoDB and dataB into
Redis,
and don`t worry about the implement, just define it and use it!

### 📝Configuration

The **Cache Provider** include a custom starter which base on spring starter, you can config the provider(s) properties
in
your `application.yml`
and the starter will auto define provider(s) by your properties when your app start up.

```yaml
cache-provider:
  redis:
    consumer-list:
      - consumer:
        store-list:
          - store:
            host:
            port:
            password:
            database:
            store-class-name:
            addition:
              max-idle:
              max-total:
              min-idle:
              timeout:
              ttl:
              statical:
  mongo:
    consumer-list:
      - consumer:
        store-list:
          - store:
            host:
            port:
            password:
            database:
            store-class-name:
            addition:
              ttl:
          - store:
            host:
            port:
            password:
            database:
            store-class-name:
            addition:
              timeout:
              statical:
```

Obviously, If you use the properties which starting with `redis` ,
the **Cache Provider** will define Redis to store your data,
and use `mongo`, will define MongoDB.

As we know, the definition of starter properties are determined by the POJO class.

* #### ProviderCacheProperties.class

```java
  public abstract static class ProviderProperties {
    private String consumer;

    public abstract List<? extends StoreProperties> getStoreList();
}

public abstract static class StoreProperties {
    private String store = "";
    private String host;
    private Integer port;
    private String password;
    private String database;
    private Class<CacheDto> storeClassName;

    public abstract AdditionalProperties getAddition();
}

public abstract static class AdditionalProperties {
    private Duration timeout = Duration.ofSeconds(30);
    private Duration ttl = Duration.ofSeconds(60);
    private Boolean statical = false;
}
```

* #### ProviderProperties.class

    * `consumer`: define the identity code of service which will use this provider,
      like A service use A as code, B service as B.
* #### StoreProperties.class

    * `store`: define the identity code of data which from the consumer.
    * `host`: MongoDB or Redis host.
    * `port`: MongoDB or Redis port.
    * `password`: MongoDB or Redis password.
    * `database`: MongoDB scheme or Redis DB index.
    * `storeClassName`: your data POJO class, it must extends `CacheDto.class`.
* #### AdditionalProperties.class

    * `timeout`: connection pool timeout.
    * `ttl`: data expired time.
    * `statical`: true=static data, like some code list; false=dynamic data, which can be updated in real-time.
* #### RedisCacheAutoProperties.class

```java
public class RedisCacheAutoProperties extends ApplicationProperties {
    private List<ConsumerProperties> consumerList = new ArrayList<>();

    public static class ConsumerProperties extends ProviderCacheProperties.ProviderProperties<ConsumerProperties.StoreProperties> {
        private List<StoreProperties> storeList = new ArrayList<>();

        @Override
        public List<StoreProperties> getStoreList() {
            return storeList;
        }

        public static class StoreProperties extends ProviderCacheProperties.StoreProperties {
            private AdditionalProperties addition = new AdditionalProperties();

            @Override
            public AdditionalProperties getAddition() {
                return addition;
            }

            public static class AdditionalProperties extends ProviderCacheProperties.AdditionalProperties {
                private Integer minIdle = 2;
                private Integer maxIdle = 5;
                private Integer maxTotal = 20;
            }
        }
    }
}
```

* #### MongoCacheAutoProperties.class

```java
public class MongoCacheAutoProperties extends ApplicationProperties {
    private List<ConsumerProperties> consumerList = new ArrayList<>();

    public static class ConsumerProperties extends ProviderCacheProperties.ProviderProperties<ConsumerProperties.StoreProperties> {
        private List<StoreProperties> storeList = new ArrayList<>();

        @Override
        public List<StoreProperties> getStoreList() {
            return storeList;
        }

        public static class StoreProperties extends ProviderCacheProperties.StoreProperties {
            private AdditionalProperties addition = new AdditionalProperties();

            @Override
            public AdditionalProperties getAddition() {
                return addition;
            }

            public static class AdditionalProperties extends ProviderCacheProperties.AdditionalProperties {

            }
        }
    }
}
```

### 📝 Coding

#### 📀 Provider

* #### CacheOperation.class

```java
public class CacheOperation<T extends Serializable> implements ResponseData {
    private Long ttlOfSeconds;
    @CacheData
    private T data;
}
```

Cache operation payload for CURD.

---

* #### CacheStore.class

```java
public class CacheStore<T extends Serializable> implements Serializable {
    @CacheQuery.Support(CacheQuery.Property.CACHE_KEY)
    @CacheId
    private String cacheKey;
    @CacheQuery.Support(CacheQuery.Property.CREATED_DTM)
    private LocalDateTime createdDtm;
    @CacheQuery.Support(CacheQuery.Property.UPDATED_DTM)
    private LocalDateTime updatedDtm;
    @JsonIgnore
    private LocalDateTime effectedDtm;
    @CacheData
    private T data;
}
```

Definition of data structure which stored in **Cache Provider**.

---

* #### ProviderCacheStore.class

```java
public class ProviderCacheStore extends CacheStore<byte[]> {
}
```

Definition of provider data structure which stored in **Cache Provider**.

---

* #### ProviderOp.class

```java
public class ProviderOp<T extends Serializable> extends CacheOperation<T> {

    public static class Single extends ProviderOp<ProviderCacheStore> {
    }

    public static class Batch extends ProviderOp<ProviderCacheStore[]> {
    }
}
```

Definition of provider operation payload, you can use this to manipulate one or more data.

---

* #### ProviderInfoDto.class

```java
public class ProviderInfoDto {

    private Long count;
    private LocalDateTime createdDtm;
    private LocalDateTime maxUpdatedDtm;
    private ConsumerIdentifierDto consumerIdentifier;
}
```

You can retrieve a provider information from this POJO.

---

* #### CacheOperationConverter.class

```java
public abstract class CacheOperationConverter<T extends ProviderOp<? extends Serializable>> extends AbstractHttpMessageConverter<T> {

    protected final ObjectMapper objectMapper;
    protected final CacheProviderSelector cacheProvider;

    public CacheOperationConverter(ObjectMapper objectMapper, CacheProviderSelector cacheProvider) {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
        this.cacheProvider = cacheProvider;
    }

    protected ConsumerIdentifier verify(HttpInputMessage inputMessage) throws CmnParameterException {
        List<String> customer = inputMessage.getHeaders().get(XCacheConstants.HTTP_HEADER_FOR_CACHE_CUSTOMER);
        List<String> store = inputMessage.getHeaders().get(XCacheConstants.HTTP_HEADER_FOR_CACHE_STORE);
        if (CollectionUtils.isEmpty(customer) || customer.size() > 1
                || CollectionUtils.isEmpty(store) || store.size() > 1) {

            throw new CmnParameterException();
        }
        ConsumerIdentifier identifier = new ConsumerIdentifier(customer.getFirst(), store.getFirst());
        if (!cacheProvider.getCacheProviderMap().containsKey(identifier)) {

            throw new CmnParameterException();
        }

        return identifier;
    }

    @Nonnull
    @Override
    protected T readInternal(@Nonnull Class<? extends T> clazz, @Nonnull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {

        try {
            this.verify(inputMessage);

            return this.readInternal(inputMessage);
        } catch (Exception e) {

            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }
    }

    @Nonnull
    abstract T readInternal(@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException;

}
```

---

* #### ProviderOpSingleConverter.class

```java
public class ProviderOpSingleConverter extends CacheOperationConverter<ProviderOp.Single> {

    public ProviderOpSingleConverter(ObjectMapper objectMapper,
                                     CacheProviderSelector cacheProvider) {
        super(objectMapper, cacheProvider);
    }

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {

        return ProviderOp.Single.class.isAssignableFrom(clazz);
    }

    @Nonnull
    @Override
    ProviderOp.Single readInternal(@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JsonNode jsonNode = objectMapper.readTree(inputMessage.getBody());
        CacheOperation<CacheStore<Serializable>> op = objectMapper.convertValue(jsonNode, new TypeReference<>() {
        });
        String data = objectMapper.writeValueAsString(op.getData().getData());
        op.getData().setData(objectMapper.writeValueAsBytes(data));

        return objectMapper.convertValue(op, new TypeReference<>() {
        });
    }

    @Override
    protected void writeInternal(@Nonnull ProviderOp.Single single, @Nonnull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        String data = objectMapper.readValue(single.getData().getData(), String.class);
        CacheOperation<CacheStore<Serializable>> op = objectMapper.convertValue(single, new TypeReference<>() {
        });
        op.getData().setData((Serializable) objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
        }));

        objectMapper.writeValue(outputMessage.getBody(), ResponseUtils.successResponse(op));
    }
}
```

---

* #### ProviderOpBatchConverter.class

```java
public class ProviderOpBatchConverter extends CacheOperationConverter<ProviderOp.Batch> {

    public ProviderOpBatchConverter(ObjectMapper objectMapper,
                                    CacheProviderSelector cacheProvider) {
        super(objectMapper, cacheProvider);
    }

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {

        return ProviderOp.Batch.class.isAssignableFrom(clazz);
    }

    @Nonnull
    @Override
    protected ProviderOp.Batch readInternal(@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JsonNode jsonNode = objectMapper.readTree(inputMessage.getBody());
        CacheOperation<CacheStore<?>[]> op = objectMapper.convertValue(jsonNode, new TypeReference<>() {
        });
        ProviderCacheStore[] stores =
                Arrays.stream(op.getData()).map(store -> {
                            ProviderCacheStore providerCacheStore = new ProviderCacheStore();
                            providerCacheStore.setCacheKey(store.getCacheKey());
                            providerCacheStore.setCreatedDtm(store.getCreatedDtm());
                            providerCacheStore.setUpdatedDtm(store.getUpdatedDtm());
                            providerCacheStore.setEffectedDtm(store.getEffectedDtm());
                            String data = SneakyThrowUtils.get(() -> objectMapper.writeValueAsString(store.getData()));
                            providerCacheStore.setData(SneakyThrowUtils.get(() -> objectMapper.writeValueAsBytes(data)));

                            return providerCacheStore;
                        })
                        .toArray(ProviderCacheStore[]::new);
        op.setData(stores);

        return objectMapper.convertValue(op, new TypeReference<>() {
        });
    }

    @Override
    protected void writeInternal(@Nonnull ProviderOp.Batch batch, @Nonnull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        CacheOperation<CacheStore<Serializable>[]> op = objectMapper.convertValue(batch, new TypeReference<>() {
        });
        for (CacheStore<Serializable> datum : op.getData()) {
            String data = objectMapper.readValue((byte[]) datum.getData(), String.class);
            datum.setData((Serializable) objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            }));
        }


        objectMapper.writeValue(outputMessage.getBody(), ResponseUtils.successResponse(op));
    }
}
```

---

#### 💿 Consumer

* #### ConsumerCacheData.class

```java
public interface ConsumerCacheData extends ResponseData {
    String generateCacheKey();
}
```

Interface of cache data, if you want to cache your data in **Cache Provider**, please implement this in your POJO.

---

* #### ConsumerCacheStore.class

```java
public class ConsumerCacheStore<T extends ConsumerCacheData> extends CacheStore<T> implements Serializable {
}
```

Definition of consumer data structure.

---

* #### ConsumerOp.class

```java
public class ConsumerOp<T extends Serializable> extends CacheOperation<T> {
    public static class Single<T extends ConsumerCacheData> extends ConsumerOp<ConsumerCacheStore<T>> {
    }

    public static class Batch<T extends ConsumerCacheData> extends ConsumerOp<ConsumerCacheStore<T>[]> {
    }
}
```

Definition of consumer operation payload, you can use this to manipulate one or more data.

---

* #### ConsumerIdentifierDto.class

```java
public class ConsumerIdentifierDto {
    private String consumer;
    private String store;
}
```

The identification of user data, please refer `📝Configuration`.

---

* #### CacheBatchQuery.class

```java
public class CacheBatchQuery implements Serializable {
    private Long limit;
    private CacheQuery.Property sortBy;
    private CacheQuery.Sort.Direction sortDirection;
    private Map<CacheQuery.Property, Serializable> criteriaMap = new EnumMap<>(CacheQuery.Property.class);
}
```

You can use this POJO to retrieve a list of data by your criteria in consumer.

---

* ### 💱 Workflow

![saving.png](assets/saving.png?t=1752227661528)

---

* ### 🎯 Interface

    * *POST* `/operation`
      Create a new record for the consumer(with identifier).
    * *PUT* `/operation`
      Update a existed record for the consumer(with identifier).
    * *DELETE* `/operation/{id}`
      Delete a existed record for the consumer(with identifier).
    * *GET* `/operation/{id}`
      Retrieve a existed record for the consumer(with identifier).
    * *POST* `/operation/batch`
      Batch create new records for the consumer(with identifier).
    * *POST* `/operation/list`
      Retrieve list of existed record for the consumer(with identifier).
    * *GET* `/operation/extra/providerInfo`
      Retrieve provider information for the consumer(with identifier).

---

🤖 Good luck and enjoy it ~~
