package ricciliao.cache.configuration.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.lang.Nullable;
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.component.MongoTemplateProvider;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;
import ricciliao.x.component.utils.CoreUtils;
import ricciliao.x.starter.PropsAutoConfiguration;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@PropsAutoConfiguration(
        properties = MongoCacheAutoProperties.class,
        conditionProperties = "cache-provider.mongo.consumer-list[0].consumer"
)
public class MongoCacheAutoConfiguration {

    public MongoCacheAutoConfiguration(@Autowired MongoCacheAutoProperties props,
                                       @Autowired CacheProviderSelector providerSelector) {
        for (MongoCacheAutoProperties.ConsumerProperties consumerProps : props.getConsumerList()) {
            for (MongoCacheAutoProperties.ConsumerProperties.StoreProperties storeProps : consumerProps.getStoreList()) {
                this.createWrapper(
                        new ConsumerIdentifierDto(consumerProps.getConsumer(), storeProps.getStore()),
                        consumerProps.getConsumer(),
                        storeProps,
                        providerSelector
                );
            }
        }
    }

    private void createWrapper(ConsumerIdentifierDto identifier,
                               String consumer,
                               MongoCacheAutoProperties.ConsumerProperties.StoreProperties props,
                               CacheProviderSelector providerSelector) {
        providerSelector.getCacheProviderMap().put(
                identifier,
                new MongoTemplateProvider(
                        createMongoTemplate(consumer, props),
                        props.getAddition().getTtl(),
                        props.getStore(),
                        props.getStoreClassName()
                )
        );
        providerSelector.getCacheClass().put(identifier, props.getStoreClassName());
    }

    private MongoTemplate createMongoTemplate(String consumer,
                                              MongoCacheAutoProperties.ConsumerProperties.StoreProperties props) {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        builder.credential(MongoCredential.createCredential(consumer, props.getAuthDatabase(), props.getPassword().toCharArray()));
        MongoClientSettings settings =
                builder.retryWrites(true)
                        .applyConnectionString(new ConnectionString("mongodb://" + props.getHost()))
                        .build();

        SimpleMongoClientDatabaseFactory databaseFactory =
                new SimpleMongoClientDatabaseFactory(
                        MongoClients.create(
                                settings,
                                MongoDriverInformation.builder(MongoDriverInformation.builder().build()).driverName("spring-data").build()
                        ),
                        props.getDatabase()
                );

        MongoCustomConversions customConversions =
                MongoCustomConversions.create(adapter -> {
                    adapter.useNativeDriverJavaTimeCodecs();
                    adapter.registerConverter(new LocalDateTime2Date());
                    adapter.registerConverter(new Date2LocalDateTime());
                });

        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(Set.of(props.getStoreClassName()));
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(PropertyNameFieldNamingStrategy.INSTANCE);
        mappingContext.setAutoIndexCreation(false);
        mappingContext.initialize();

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(databaseFactory);

        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(customConversions);
        converter.setCodecRegistryProvider(databaseFactory);
        converter.afterPropertiesSet();

        MongoTemplate mongoTemplate = new MongoTemplate(databaseFactory, converter);

        mongoTemplate.indexOps(props.getStore()).ensureIndex(
                new Index()
                        .on("effectedDtm", Sort.Direction.ASC)
                        .expire(props.getAddition().getTtl())
                        .background()
        );

        return mongoTemplate;
    }

    static class LocalDateTime2Date implements Converter<LocalDateTime, Date> {

        @Override
        public Date convert(@Nullable LocalDateTime source) {
            Long timestamp = CoreUtils.toLong(source);
            if (Objects.isNull(timestamp)) {

                return null;
            }

            return new Date(timestamp);
        }
    }

    static class Date2LocalDateTime implements Converter<Date, LocalDateTime> {

        @Override
        public LocalDateTime convert(@Nullable Date source) {
            if (Objects.isNull(source)) {

                return null;
            }

            return CoreUtils.toLocalDateTime(source.getTime());
        }
    }

}
