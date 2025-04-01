package ricciliao.cache.configuration.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ricciliao.x.cache.pojo.CacheDto;
import ricciliao.x.component.props.ApplicationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("cache-provider.mongo")
public class MongoCacheAutoProperties extends ApplicationProperties {

    private List<ConsumerProperties> consumerList = new ArrayList<>();

    public List<ConsumerProperties> getConsumerList() {
        return consumerList;
    }

    public void setConsumerList(List<ConsumerProperties> consumerList) {
        this.consumerList = consumerList;
    }

    public static class ConsumerProperties {

        private String consumer;
        private List<StoreProperties> storeList = new ArrayList<>();

        public String getConsumer() {
            return consumer;
        }

        public void setConsumer(String consumer) {
            this.consumer = consumer;
        }

        public List<StoreProperties> getStoreList() {
            return storeList;
        }

        public void setStoreList(List<StoreProperties> storeList) {
            this.storeList = storeList;
        }

        public static class StoreProperties {

            private String store = "";
            private String host;
            private Integer port;
            private String password;
            private String database;
            private String authDatabase = "admin";
            private Class<? extends CacheDto> storeClassName;
            private AdditionalProperties addition = new AdditionalProperties();

            public String getAuthDatabase() {
                return authDatabase;
            }

            public void setAuthDatabase(String authDatabase) {
                this.authDatabase = authDatabase;
            }

            public String getStore() {
                return store;
            }

            public void setStore(String store) {
                this.store = store;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public Integer getPort() {
                return port;
            }

            public void setPort(Integer port) {
                this.port = port;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getDatabase() {
                return database;
            }

            public void setDatabase(String database) {
                this.database = database;
            }

            public Class<? extends CacheDto> getStoreClassName() {
                return storeClassName;
            }

            public void setStoreClassName(Class<? extends CacheDto> storeClassName) {
                this.storeClassName = storeClassName;
            }

            public AdditionalProperties getAddition() {
                return addition;
            }

            public void setAddition(AdditionalProperties addition) {
                this.addition = addition;
            }

            public static class AdditionalProperties {

                private Duration timeout = Duration.ofSeconds(30);
                private Duration ttl = Duration.ofSeconds(60);
                private Boolean statical = false;

                public Boolean getStatical() {
                    return statical;
                }

                public void setStatical(Boolean statical) {
                    this.statical = statical;
                }

                public Duration getTimeout() {
                    return timeout;
                }

                public void setTimeout(Duration timeout) {
                    this.timeout = timeout;
                }

                public Duration getTtl() {
                    return ttl;
                }

                public void setTtl(Duration ttl) {
                    this.ttl = ttl;
                }
            }
        }

    }

}
