package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.cas.CASException;


/**
 * Base interface for all attribute models
 */
public interface AttributesModel {

    boolean init(IdentifiedAnnotation mention) throws CASException;
}
