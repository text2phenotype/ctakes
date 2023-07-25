package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.cas.CASException;

public class BaseTokenAttributesModel implements AttributesModel {

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    private String partOfSpeech;

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        return true;
    }
}
