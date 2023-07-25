package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.text2phenotype.ctakes.rest.api.pipeline.model.NPIModel;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class NPIResponseModel extends ResponseModel {

    @JsonProperty("providers")
    private List<NPIModel> npis = new ArrayList<>();

    public List<NPIModel> getProviders() {
        return npis;
    }
}
