package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.BaseTokenModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Response model for POS Tagger pipeline
 */
public class POSTaggerResponseModel extends ResponseModel {

    private List<BaseTokenModel> tokens = new ArrayList<>();

    public List<BaseTokenModel> getTokens() {
        return tokens;
    }
}
