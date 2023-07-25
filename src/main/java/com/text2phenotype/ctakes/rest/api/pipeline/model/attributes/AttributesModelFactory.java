package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.textsem.*;

/**
 * Factory of Attribute model
 */
public class AttributesModelFactory {

    /**
     * Create attributes by mention type
     * @param mention Event mention
     * @return
     */
    public static AttributesModel createModel(EventMention mention){

        if (mention instanceof DiseaseDisorderMention){
            return new DiseaseDisorderAttributesModel();
        } else if (mention instanceof MedicationMention) {
            return new MedicationAttributesModel();
        } else if (mention instanceof SignSymptomMention) {
            return new SignSymptomAttributesModel();
        } else if (mention instanceof LabMention) {
            return new LabAttributesModel();
        } else if (mention instanceof MedicationEventMention) {
            return new MedicationEventAttributesModel();
        } else if (mention instanceof ProcedureMention) {
            return new ProcedureAttributesModel();
        }

        return new UnknownAttributesModel();
    }
}
