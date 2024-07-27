package cm.nzock.beans;

import java.io.Serializable;

public class ChatData implements Serializable {
    private Long pk ;
    private String input;
    private String value;
    private boolean initial;
    private double cosim;
    private boolean review = true;

    public ChatData(Long pk, String input, String value, boolean initial, boolean review) {
        this.pk = pk;
        this.input = input;
        this.value = value;
        this.initial = initial;
        this.review = review;
    }

    public ChatData(Long pk, String input, String value, double cosim, boolean initial, boolean review) {
        this.pk = pk;
        this.input = input;
        this.value = value;
        this.initial = initial;
        this.cosim = cosim;
        this.review = review;
    }

    public ChatData() {
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

    public double getCosim() {
        return cosim;
    }

    public void setCosim(double cosim) {
        this.cosim = cosim;
    }

    public boolean isReview() {
        return review;
    }

    public void setReview(boolean review) {
        this.review = review;
    }
}
