package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation_Type;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/**
 * Unit annotation type
 */
public class UnitAnnotation_Type extends IdentifiedAnnotation_Type {
    public static final int typeIndexID;
    public static final boolean featOkTst;

    public UnitAnnotation_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, this.getFSGenerator());
    }

    static {
        typeIndexID = UnitAnnotation.typeIndexID;
        featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitAnnotation");
    }
}