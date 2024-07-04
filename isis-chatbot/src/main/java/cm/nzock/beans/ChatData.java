package cm.nzock.beans;

import java.io.Serializable;

public class ChatData implements Serializable {
    private Long pk ;
    private String input;
    private String value;
    private boolean initial;

    public ChatData(Long pk, String input, String value, boolean initial) {
        this.pk = pk;
        this.input = input;
        this.value = value;
        this.initial = initial;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }
}
