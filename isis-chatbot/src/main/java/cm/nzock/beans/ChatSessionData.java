package cm.nzock.beans;

import java.io.Serializable;

public class ChatSessionData implements Serializable {
    private Long pk ;
    private String label ;

    public ChatSessionData() {
    }

    /**
     *
     * @param pk
     * @param label
     */
    public ChatSessionData(Long pk, String label) {
        this.pk = pk;
        this.label = label;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
