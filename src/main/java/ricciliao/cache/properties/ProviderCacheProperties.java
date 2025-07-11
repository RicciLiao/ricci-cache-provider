package ricciliao.cache.properties;

import java.time.Duration;
import java.util.List;

public class ProviderCacheProperties {

    private ProviderCacheProperties() {

    }

    public abstract static class ProviderProperties<T extends StoreProperties> {
        private String consumer;

        public String getConsumer() {
            return consumer;
        }

        public void setConsumer(String consumer) {
            this.consumer = consumer;
        }

        public abstract List<T> getStoreList();
    }

    public abstract static class StoreProperties {
        private String store = "";
        private String host;
        private Integer port;
        private String password;
        private String database;

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

        public abstract AdditionalProperties getAddition();

    }

    public abstract static class AdditionalProperties {
        private Duration timeout = Duration.ofSeconds(30);
        private Duration ttl = Duration.ofSeconds(60);
        private Boolean statical = false;

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

        public Boolean getStatical() {
            return statical;
        }

        public void setStatical(Boolean statical) {
            this.statical = statical;
        }
    }
}
