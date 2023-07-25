package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.refsem.UmlsConcept_Type;
import org.apache.ctakes.typesystem.type.util.Pair;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

public class UmlsConceptFlex_Type extends UmlsConcept_Type {

    public static final int typeIndexID;
    public static final boolean featOkTst;
    public final Feature casFeat_params;
    public final int casFeatCode_params;

//    public int getParams(int addr) {
//        if (featOkTst && casFeat_params == null)
//            jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
//        return ll_cas.ll_getRefValue(addr, casFeatCode_params);
//    }
//
//    public void setParams(int addr, int v) {
//        if (featOkTst && casFeat_params == null)
//            jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
//
//        ll_cas.ll_setRefValue(addr, casFeatCode_params, v);
//    }

    public int getParams(int addr) {
        if (featOkTst && casFeat_params == null)
            jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        return ll_cas.ll_getRefValue(addr, casFeatCode_params);
    }

    public void setParams(int addr, int v) {
        if (featOkTst && casFeat_params == null)
            jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        ll_cas.ll_setRefValue(addr, casFeatCode_params, v);}

    public int getParams(int addr, int i) {
        if (featOkTst && casFeat_params == null)
            jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        if (lowLevelTypeChecks)
            return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_params), i, true);
        jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_params), i);
        return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_params), i);
    }

    public void setParams(int addr, int i, int v) {
        if (featOkTst && casFeat_params == null)
            jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        if (lowLevelTypeChecks)
            ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_params), i, v, true);
        jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_params), i);
        ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_params), i, v);
    }

    public UmlsConceptFlex_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

        casFeat_params = jcas.getRequiredFeatureDE(casType, "params", "uima.cas.FSArray", featOkTst);
        casFeatCode_params  = (null == casFeat_params) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_params).getCode();
    }

    static {
        typeIndexID = UmlsConceptFlex.typeIndexID;
        featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
    }
}
