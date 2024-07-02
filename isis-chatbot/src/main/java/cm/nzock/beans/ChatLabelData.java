package cm.nzock.beans;

import java.io.Serializable;

public class ChatLabelData implements Serializable {
    private String uuid ;
    private String value;

    public ChatLabelData(String uuid, String value) {
        this.uuid = uuid;
        this.value = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
