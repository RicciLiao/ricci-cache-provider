package ricciliao.cache.pojo.bo;

import ricciliao.common.component.cache.RedisCacheBo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class CaptchaRedisBo extends RedisCacheBo implements Serializable {
    @Serial
    private static final long serialVersionUID = -5729432642565598935L;

    private String captcha;
    private boolean verified = false;

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaptchaRedisBo that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getCaptcha(), that.getCaptcha()) && Objects.equals(getVerified(), that.getVerified());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getCaptcha(), getVerified());
    }
}
