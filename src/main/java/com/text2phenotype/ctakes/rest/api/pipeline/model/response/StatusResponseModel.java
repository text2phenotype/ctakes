package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

public class StatusResponseModel implements ResponseData {

    public StatusResponseModel(String msg, Integer status) {
        setMessage(msg);
        setStatus(status);
    }
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
