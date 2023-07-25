package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Response model for errors
 */
public class ErrorResponseModel extends ResponseModel {

    private List<String> errors;

    public ErrorResponseModel(String... messages){
        errors = new ArrayList<>();
        errors.addAll(Arrays.asList(messages));
    }

    public ErrorResponseModel(List<String> messages){
        errors = messages;
    }

    public List<String> getErrors() {
        return errors;
    }
}
