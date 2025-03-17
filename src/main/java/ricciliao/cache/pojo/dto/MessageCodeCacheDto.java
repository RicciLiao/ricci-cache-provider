package ricciliao.cache.pojo.dto;


import ricciliao.x.component.cache.pojo.CacheDto;

import java.io.Serial;
import java.util.Objects;

public class MessageCodeCacheDto extends CacheDto {
    @Serial
    private static final long serialVersionUID = -4046549743760144498L;
    private Long code;
    private String type;
    private String projectCode;
    private String description;
    private Boolean active;

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageCodeCacheDto that)) return false;
        return Objects.equals(getCacheId(), that.getCacheId()) && Objects.equals(getCode(), that.getCode()) && Objects.equals(getType(), that.getType()) && Objects.equals(getProjectCode(), that.getProjectCode()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getActive(), that.getActive());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCacheId(), getCode(), getType(), getProjectCode(), getDescription(), getActive());
    }
}
