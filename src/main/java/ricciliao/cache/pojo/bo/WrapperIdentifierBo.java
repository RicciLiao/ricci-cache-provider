package ricciliao.cache.pojo.bo;

import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class WrapperIdentifierBo implements Serializable {
    @Serial
    private static final long serialVersionUID = -6723685925978483107L;

    public WrapperIdentifierBo(String consumer, String identifier) {
        this.consumer = consumer;
        this.identifier = identifier;
    }

    public WrapperIdentifierBo() {
    }

    private String consumer;
    private String identifier;

    public String consumer() {
        return consumer;
    }

    public String identifier() {
        return identifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumer, identifier);
    }

    @Override
    public String toString() {

        return StringUtils.lowerCase(consumer + "_" + identifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WrapperIdentifierBo that)) return false;

        return Objects.equals(this.toString(), that.toString());
    }

}
