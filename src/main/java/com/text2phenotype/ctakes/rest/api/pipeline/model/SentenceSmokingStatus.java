package com.text2phenotype.ctakes.rest.api.pipeline.model;

public class SentenceSmokingStatus {

    private String status;
    private Object[] text;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object[] getText() {
        return text;
    }

    public void setText(Object[] text) {
        this.text = text;
    }
}
