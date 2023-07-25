package com.text2phenotype.ctakes.rest.api.pipeline.controllers;

import com.text2phenotype.ctakes.rest.api.pipeline.model.AnnotationModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthCheckUtils {

    public static final List<String> HEALTH_CHECK_PIPELINES = Arrays.asList(
            "rxnorm/drug_ner",
            "original/default_clinical",
            "general/temporal_module",
            "loinc/lab_value"
    );

    private  HealthCheckUtils() {

    }
    public static List<AnnotationModel> getHealthCheckData(ResponseData model) {

        List<AnnotationModel> result = new ArrayList<>();
        if (model instanceof DefaultResponseModel) {
            DefaultResponseModel typedModel = (DefaultResponseModel)model;
            if (typedModel.getContent().size() > 0) {
                result.add(typedModel.getContent().get(0));
            }
        }

        if (model instanceof TemporalResponseModel) {
            TemporalResponseModel typedModel = (TemporalResponseModel)model;
            if (typedModel.getEvents().size() > 0) {
                result.add(typedModel.getEvents().get(0));
            }

            if (typedModel.getTimex().size() > 0) {
                result.add(typedModel.getTimex().get(0));
            }

            if (typedModel.getTlinks().size() > 0) {
                result.add(typedModel.getTlinks().get(0));
            }
        }

        if (model instanceof DrugResponseModel) {
            DrugResponseModel typedModel = (DrugResponseModel)model;
            if (typedModel.getDrugEntities().size() > 0) {
                result.add(typedModel.getDrugEntities().get(0));
            }
        }

        if (model instanceof LabResponseModel) {
            LabResponseModel typedModel = (LabResponseModel)model;
            if (typedModel.getLabValues().size() > 0) {
                result.add(typedModel.getLabValues().get(0));
            }
        }

        return result;
    }
}
