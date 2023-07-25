package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.LabValueModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Response model for Lab Value pipeline
 */
public class LabResponseModel extends ResponseModel {

    private List<LabValueModel> labValues = new ArrayList<>();

    public List<LabValueModel> getLabValues() {
        return labValues;
    }
}
