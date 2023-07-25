package com.text2phenotype.ctakes.rest.api.pipeline.model;

import java.util.List;

/**
 * Data model for event
 */
public class EventModel extends IdentifiedAnnotationModel {

    private String aspect;
    private String name;


    public String getAspect() {
        return aspect;
    }
    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
