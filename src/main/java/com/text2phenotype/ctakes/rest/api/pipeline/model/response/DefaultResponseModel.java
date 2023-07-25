package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.ContentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for default clinical pipeline results
 */
public class DefaultResponseModel extends ResponseModel {

    private List<ContentModel> content = new ArrayList<>();

    public List<ContentModel> getContent() {
        return content;
    }
}
