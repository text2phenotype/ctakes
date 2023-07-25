package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationEventMention;
import org.apache.uima.cas.CASException;

/**
 * Attributes model for {@link MedicationEventMention}
 */
public class MedicationEventAttributesModel extends UnknownAttributesModel {

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        return super.init(mention);

    }
}
