package ricciliao.cache.configuration.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ricciliao.x.cache.ProviderCacheProperties;
import ricciliao.x.component.props.ApplicationProperties;

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

    public static class ConsumerProperties extends ProviderCacheProperties.ConsumerProperties<ConsumerProperties.StoreProperties> {

        private List<StoreProperties> storeList = new ArrayList<>();


        public void setStoreList(List<StoreProperties> storeList) {
            this.storeList = storeList;
        }

        @Override
        public List<StoreProperties> getStoreList() {

            return storeList;
        }

        public static class StoreProperties extends ProviderCacheProperties.StoreProperties {

            private String authDatabase = "admin";
            private AdditionalProperties addition = new AdditionalProperties();

            public String getAuthDatabase() {
                return authDatabase;
            }

            public void setAuthDatabase(String authDatabase) {
                this.authDatabase = authDatabase;
            }

            @Override
            public AdditionalProperties getAddition() {

                return addition;
            }

            public void setAddition(AdditionalProperties addition) {
                this.addition = addition;
            }

            public static class AdditionalProperties extends ProviderCacheProperties.AdditionalProperties {

            }
        }

    }

}
