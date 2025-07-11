package ricciliao.cache.configuration.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ricciliao.cache.properties.ProviderCacheProperties;
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

    public static class ConsumerProperties extends ProviderCacheProperties.ProviderProperties<ConsumerProperties.StoreProperties> {

        private List<StoreProperties> storeList = new ArrayList<>();

        @Override
        public List<StoreProperties> getStoreList() {

            return storeList;
        }

        public void setStoreList(List<StoreProperties> storeList) {
            this.storeList = storeList;
        }

        public static class StoreProperties extends ProviderCacheProperties.StoreProperties {

            private AdditionalProperties addition = new AdditionalProperties();

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
