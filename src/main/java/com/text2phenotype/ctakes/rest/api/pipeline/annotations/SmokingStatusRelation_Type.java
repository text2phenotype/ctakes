package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;

import java.util.List;

public class SmokingStatusRelation_Type extends TOP_Type {

    @SuppressWarnings ("hiding")
    public final static int typeIndexID = SmokingStatusRelation.typeIndexID;

    @SuppressWarnings ("hiding")
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.SmokingStatusRelation");

    final Feature SentFeat;
    final int     SentFeatCode;

    final Feature StatusFeat;
    final int     StatusFeatCode;

    /** initialize variables to correspond with Cas Type and Features
     * @generated
     * @param jcas JCas
     * @param casType Type
     */
    public SmokingStatusRelation_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

        SentFeat = jcas.getRequiredFeatureDE(casType, "sentence", "org.apache.ctakes.typesystem.type.textspan.Sentence", featOkTst);
        SentFeatCode  = (null == SentFeat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)SentFeat).getCode();


        StatusFeat = jcas.getRequiredFeatureDE(casType, "statuses", "uima.cas.StringArray", featOkTst);
        StatusFeatCode  = (null == StatusFeat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)StatusFeat).getCode();
    }

    public Sentence getSentence(int addr) {
        if (featOkTst && SentFeat == null)
            jcas.throwFeatMissing("sentence", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.SmokingStatusRelation");
        return (Sentence)ll_cas.ll_getFSForRef(ll_cas.ll_getRefValue(addr, SentFeatCode));
    }

    public void setSentence(int addr, Sentence sent) {
        if (featOkTst && SentFeat == null)
            jcas.throwFeatMissing("sentence", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.SmokingStatusRelation");
        ll_cas.ll_setRefValue(addr, SentFeatCode, ll_cas.ll_getFSRef(sent));
    }


    public StringArray getStatuses(int addr) {
        if (featOkTst && StatusFeat == null)
            jcas.throwFeatMissing("statuses", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.SmokingStatusRelation");
        return (StringArray) ll_cas.ll_getFSForRef(ll_cas.ll_getRefValue(addr, StatusFeatCode));
    }

    public void setStatuses(int addr, StringArray statuses) {
        if (featOkTst && StatusFeat == null)
            jcas.throwFeatMissing("statuses", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.SmokingStatusRelation");
        ll_cas.ll_setRefValue(addr, StatusFeatCode, ll_cas.ll_getFSRef(statuses));
    }
}