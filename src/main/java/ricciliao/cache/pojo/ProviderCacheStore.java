package ricciliao.cache.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ricciliao.x.cache.pojo.CacheStore;

import java.io.Serial;
import java.time.Instant;
import java.util.List;

public class ProviderCacheStore extends CacheStore<byte[]> {
    @Serial
    private static final long serialVersionUID = -1634685567608547710L;

    public ProviderCacheStore() {
    }

    public ProviderCacheStore(byte[] data) {
        super();
        this.setData(data);
    }

    public ProviderCacheStore(Long ttlSec, byte[] data) {
        super();
        this.setData(data);
    }

    @JsonIgnore
    private Instant ttlEffectedDtm;

    public Instant getTtlEffectedDtm() {
        return ttlEffectedDtm;
    }

    public void setTtlEffectedDtm(Instant ttlEffectedDtm) {
        this.ttlEffectedDtm = ttlEffectedDtm;
    }

    public static class Batch extends CacheStore.Batch<ProviderCacheStore> {
        @Serial
        private static final long serialVersionUID = -7935524552356246855L;

        public Batch(List<ProviderCacheStore> batch) {
            super(batch);
        }
    }

}
