package com.text2phenotype.ctakes.rest.api.pipeline.controllers;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.Text2phenotypeCliOptionals;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb.ResponseModelBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline description
 */
public class Pipeline {
    private String name;
    private String dict;
    private Map<String, String> params = new HashMap<>();
    private String piper;
    private Class<? extends ResponseModelBuilder> modelBuilder;
    private Text2phenotypeCliOptionals cli_options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getPiper() {
        return piper;
    }

    public void setPiper(String piper) {
        this.piper = piper;
    }

    public Class<? extends ResponseModelBuilder> getModelBuilder() {
        return modelBuilder;
    }

    public void setModelBuilder(Class<? extends ResponseModelBuilder> modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public Text2phenotypeCliOptionals getCli_options() {
        return cli_options;
    }

    public void setCli_options(Text2phenotypeCliOptionals cli_options) {
        this.cli_options = cli_options;
    }


    public int CheckParams(Map<String, String> requestParams) {
        int result = 0;
        for (String pp: this.params.keySet()) {
            if (requestParams.containsKey(pp) && this.params.get(pp).equals(requestParams.get(pp))) {
                result++;
            } else {
                result--;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("%s/%s", dict, name);
    }
}
