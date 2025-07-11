package ricciliao.cache.pojo;

import ricciliao.x.cache.pojo.CacheOperation;

import java.io.Serial;
import java.io.Serializable;

public class ProviderOp<T extends Serializable> extends CacheOperation<T> {
    @Serial
    private static final long serialVersionUID = -9063339882999027457L;

    public ProviderOp() {
        super();
    }

    public ProviderOp(Long ttlOfSeconds, T data) {
        super(ttlOfSeconds, data);
    }

    public static class Single extends ProviderOp<ProviderCacheStore> {
        @Serial
        private static final long serialVersionUID = -5686402731559236165L;

        public Single() {
            super();
        }

        public Single(Long ttlOfSeconds, ProviderCacheStore data) {
            super(ttlOfSeconds, data);
        }

    }

    public static class Batch extends ProviderOp<ProviderCacheStore[]> {
        @Serial
        private static final long serialVersionUID = -6738583214712630689L;

        public Batch() {
            super();
        }

        public Batch(Long ttlOfSeconds, ProviderCacheStore[] data) {
            super(ttlOfSeconds, data);
        }
    }

}
