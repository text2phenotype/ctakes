package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.apache.uima.cas.CASException;

/**
 * Attributes model for {@link SignSymptomMention}
 */
public class SignSymptomAttributesModel extends UnknownAttributesModel {

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        return super.init(mention);
    }
}
