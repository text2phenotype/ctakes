package com.text2phenotype.ctakes.rest.api.pipeline.model;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for Timex
 */
public class TimeExModel extends IdentifiedAnnotationModel {

    private List<Object> text = new ArrayList<Object>(3);
    private String type;
    private String mod = "NA";

    public List<Object> getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }
    public void setText(Annotation timex) {
        text.add(0, timex.getCoveredText());
        text.add(1, timex.getBegin());
        text.add(2, timex.getEnd());
    }
}
