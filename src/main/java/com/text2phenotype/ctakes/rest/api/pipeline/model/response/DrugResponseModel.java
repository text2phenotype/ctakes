package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.ContentModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.EventModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Response model for Drug NER pipeline
 */
public class DrugResponseModel extends ResponseModel {

    private List<ContentModel> drugEntities = new ArrayList<>();

    public List<ContentModel> getDrugEntities() {
        return drugEntities;
    }
}
