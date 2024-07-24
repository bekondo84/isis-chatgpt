package cm.nzock.beans;

import java.io.Serializable;

public class DomainData implements Serializable {
    private Long pk ;
    private String code ;
    private String label ;
    private String description;
    private boolean isDefault;

    public DomainData() {
    }

    public Long getPk() {
        return pk;
    }

    public DomainData setPk(Long pk) {
        this.pk = pk;
        return this;
    }

    public String getCode() {
        return code;
    }

    public DomainData setCode(String code) {
        this.code = code;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public DomainData setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DomainData setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public DomainData setDefault(boolean aDefault) {
        isDefault = aDefault;
        return this;
    }
}
