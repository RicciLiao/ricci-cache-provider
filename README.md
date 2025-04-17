# Cache CURD Interface Service

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
| ricciliao.x              | cache-provider                      | compile |
| jakarta.servlet          | jakarta.servlet-api                 | compile |

### 📌 Usage

**Cache Provider** provides a series of universal RESTful interfaces for MongoDB and Redis,
you can choose MongoDB or Redis as your provider(s) for your data,
also, you can use more than one provider(s) at the same times if you need to cache dataA into MongoDB and dataB into
Redis,
and don`t worry about the implement, just define it and use it!

### 📝Configuration

The **Cache Provider** include a custom starter which base on spring starter, you can config provider(s) properties in
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
the **Cache Provider** will use Redis to store your data,
and use `mongo`, will MongoDB.

As we know, the definition of starter properties are determined by the POJO class.

* #### ProviderCacheProperties.class

  *please refer to `x-cache-components`*

```java
  public abstract static class ConsumerProperties {
    private String consumer;

    public abstract List<? extends StoreProperties> getStoreList();
    //getter
    //setter
}

public abstract static class StoreProperties {
    private String store = "";
    private String host;
    private Integer port;
    private String password;
    private String database;
    private Class<CacheDto> storeClassName;

    public abstract AdditionalProperties getAddition();
    //getter
    //setter
}

public abstract static class AdditionalProperties {
    private Duration timeout = Duration.ofSeconds(30);
    private Duration ttl = Duration.ofSeconds(60);
    private Boolean statical = false;
    //getter
    //setter
}
```

* #### ConsumerProperties.class

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

### 📝 Coding

* #### CacheDto.class

```java
public abstract class CacheDto {
    private String cacheKey;
    private LocalDateTime createdDtm;
    private LocalDateTime updatedDtm;
    private LocalDateTime effectedDtm;

    public abstract CacheDto generateCacheKey();
    //getter
    //setter
}
```

Define your own data POJO class and extends `CacheDto.class`.

---

* #### ConsumerIdentifierDto.class

```java
public class ConsumerIdentifierDto {
    private String consumer;
    private String store;
    //getter
    //setter
}
```

The identification of user data, please refer `📝Configuration`.

---

* #### ConsumerOpDto.class

```java
public class ConsumerOpDto {
    private String id;
    private Long ttlOfMillis;
    //getter
    //setter

    public static class Batch<T extends CacheDto> extends ConsumerOpDto {
        private List<T> data = new ArrayList<>();
        //getter
        //setter
    }

    public static class Single<T extends CacheDto> extends ConsumerOpDto {
        private T data;
        //getter
        //setter
    }
}
```

The CURD operations POJO for user, include batch and single operations.

---

* #### ConsumerOpBatchQueryDto.class

```java
public class ConsumerOpBatchQueryDto {
    private Long limit;
    private CacheQuery.Property sortBy;
    private CacheQuery.Sort.Direction sortDirection;
    private Map<CacheQuery.Property, Serializable> criteriaMap = new EnumMap<>(CacheQuery.Property.class);
    //getter
    //setter
}
```

You can use this operation POJO to retrieve a list of data by your criteria.

---

* #### ProviderInfoDto.class

```java
public class ProviderInfoDto {

    private Long count;
    private LocalDateTime createdDtm;
    private LocalDateTime maxUpdatedDtm;
    private ConsumerIdentifierDto consumerIdentifier;
    //getter
    //setter
}
```

You can retrieve a provider information from this POJO.

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
