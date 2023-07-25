package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation_Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;

/**
 * Unit relation type
 */
public class UnitRelation_Type extends BinaryTextRelation_Type {

    @SuppressWarnings ("hiding")
    public final static int typeIndexID = UnitRelation.typeIndexID;

    @SuppressWarnings ("hiding")
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitRelation");

    /** initialize variables to correspond with Cas Type and Features
     * @generated
     * @param jcas JCas
     * @param casType Type
     */
    public UnitRelation_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());
    }
}