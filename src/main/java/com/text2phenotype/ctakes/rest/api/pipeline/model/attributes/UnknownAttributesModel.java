package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.cas.CASException;

/**
 * Base implementation of AttributesModel
 */
public class UnknownAttributesModel implements AttributesModel {

    public String polarity;
//    public String generic;
//    public String conditional;
//    public String modality = "N/A";
    //public String timeRel;
//    public String uncertainty;

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        polarity = mention.getPolarity() >= 0 ? "positive" : "negative";
//        conditional = String.valueOf(mention.getConditional() == CONST.NE_CONDITIONAL_TRUE);
//        generic = String.valueOf(mention.getGeneric() == CONST.NE_GENERIC_TRUE);
//        uncertainty = String.valueOf(mention.getUncertainty() == CONST.NE_UNCERTAINTY_PRESENT);


//        if (mention instanceof EventMention) {
//            EventMention eventMention = (EventMention)mention;
//            Event event = eventMention.getEvent();
//            if (event != null && event.getProperties() != null) {
//                EventProperties props = event.getProperties();
//                modality = props.getContextualModality();
//                //timeRel = props.getDocTimeRel();
//            }
//        }

        return true;
    }

}
