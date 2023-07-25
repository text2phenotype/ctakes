package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseModel;
import org.apache.uima.jcas.JCas;

/**
 * Base interface for model buildeers
 */
public interface ResponseModelBuilder<T extends ResponseData> {

    ResponseData build(JCas jCas);
    void initRequestModel();
}
