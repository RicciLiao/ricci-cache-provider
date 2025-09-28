package ricciliao.cache.configuration.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClients;
import jakarta.annotation.Nullable;
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
import ricciliao.cache.component.CacheProviderSelector;
import ricciliao.cache.component.MongoTemplateProvider;
import ricciliao.x.cache.pojo.ConsumerIdentifier;
import ricciliao.x.component.utils.CoreUtils;
import ricciliao.x.starter.PropsAutoConfiguration;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@PropsAutoConfiguration(
        properties = MongoCacheAutoProperties.class,
        conditionalOnProperties = "cache-provider.mongo.consumer-list[0].consumer"
)
public class MongoCacheAutoConfiguration {

    public MongoCacheAutoConfiguration(@Autowired MongoCacheAutoProperties props,
                                       @Autowired CacheProviderSelector providerSelector) {
        for (MongoCacheAutoProperties.ConsumerProperties consumerProps : props.getConsumerList()) {
            for (MongoCacheAutoProperties.ConsumerProperties.StoreProperties storeProps : consumerProps.getStoreList()) {
                this.createWrapper(
                        new ConsumerIdentifier(consumerProps.getConsumer(), storeProps.getStore()),
                        storeProps,
                        providerSelector
                );
            }
        }
    }

    private void createWrapper(ConsumerIdentifier identifier,
                               MongoCacheAutoProperties.ConsumerProperties.StoreProperties props,
                               CacheProviderSelector providerSelector) {
        MongoTemplateProvider.MongoTemplateProviderConstruct construct = new MongoTemplateProvider.MongoTemplateProviderConstruct();
        construct.setMongoTemplate(createMongoTemplate(identifier.getConsumer(), props));
        construct.setConsumerIdentifier(identifier);
        construct.setStoreProps(props);

        providerSelector.getCacheProviderMap().put(identifier, new MongoTemplateProvider(construct));
        providerSelector.getCacheStaticalMap().put(identifier, props.getAddition().getStatical());
    }

    private MongoTemplate createMongoTemplate(String consumer,
                                              MongoCacheAutoProperties.ConsumerProperties.StoreProperties props) {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        builder.credential(MongoCredential.createCredential(consumer, props.getDatabase(), props.getPassword().toCharArray()));
        MongoClientSettings settings =
                builder.retryWrites(true)
                        .applyConnectionString(new ConnectionString("mongodb://" + props.getHost() + ":" + props.getPort()))
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
                MongoCustomConversions.create(adapter-> {
                    adapter.registerConverter(new LocalDateTime2Date());
                    adapter.registerConverter(new Date2LocalDateTime());
                });

        MongoMappingContext mappingContext = new MongoMappingContext();
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
