package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation_Type;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.Feature;

public class PhoneNumberMention_Type extends IdentifiedAnnotation_Type {

    public static final int typeIndexID;
    public static final boolean featOkTst;

    private Feature numberFeat;
    private int numberFeat_code;

    public PhoneNumberMention_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, this.getFSGenerator());

        numberFeat = jcas.getRequiredFeatureDE(casType, "number", "uima.cas.Long", featOkTst);
        numberFeat_code = (null == numberFeat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)numberFeat).getCode();
    }

    static {
        typeIndexID = PhoneNumberMention.typeIndexID;
        featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.PhoneNumberMention");
    }

    public Long getNumber(int addr){
        if (featOkTst && numberFeat == null)
            jcas.throwFeatMissing("number", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.PhoneNumberMention");
        return ll_cas.ll_getLongValue(addr, numberFeat_code);
    }

    public void setNumber(int addr, Long v) {

        if (featOkTst && numberFeat == null)
            jcas.throwFeatMissing("number", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.PhoneNumberMention");
        ll_cas.ll_setLongValue(addr, numberFeat_code, v);
    }
}
