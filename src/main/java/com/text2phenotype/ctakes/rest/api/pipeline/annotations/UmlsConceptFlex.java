package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.util.Pair;
import org.apache.ctakes.ytex.uima.types.DocKey_Type;
import org.apache.ctakes.ytex.uima.types.KeyValuePair;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP_Type;

public class UmlsConceptFlex extends UmlsConcept {

    @SuppressWarnings ("hiding")
    public final static int typeIndexID = JCasRegistry.register(UmlsConceptFlex.class);

    @SuppressWarnings ("hiding")
    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {return typeIndexID;}

    protected UmlsConceptFlex() {/* intentionally empty block */}

    public UmlsConceptFlex(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public UmlsConceptFlex(JCas jcas) {
        super(jcas);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}


//    public FSArray getParams() {
//        if (UmlsConceptFlex_Type.featOkTst && ((UmlsConceptFlex_Type)jcasType).casFeat_params == null)
//            jcasType.jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
//        return (FSArray) (jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params)));}
//
//    public void setParams(FSArray v) {
//        if (UmlsConceptFlex_Type.featOkTst && ((UmlsConceptFlex_Type)jcasType).casFeat_params == null)
//            jcasType.jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
//        jcasType.ll_cas.ll_setRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params, jcasType.ll_cas.ll_getFSRef(v));
//    }

    public FSArray getParams() {
        if (DocKey_Type.featOkTst && ((UmlsConceptFlex_Type)jcasType).casFeat_params == null)
            jcasType.jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params)));
    }

    public void setParams(FSArray v) {
        if (DocKey_Type.featOkTst && ((UmlsConceptFlex_Type)jcasType).casFeat_params == null)
            jcasType.jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        jcasType.ll_cas.ll_setRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params, jcasType.ll_cas.ll_getFSRef(v));
    }

    public Pair getParams(int i) {
        if (DocKey_Type.featOkTst && ((UmlsConceptFlex_Type)jcasType).casFeat_params == null)
            jcasType.jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params), i);
        return (Pair)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params), i)));
    }

    public void setParams(int i, Pair v) {
        if (DocKey_Type.featOkTst && ((UmlsConceptFlex_Type)jcasType).casFeat_params == null)
            jcasType.jcas.throwFeatMissing("params", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex");
        jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params), i);
        jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((UmlsConceptFlex_Type)jcasType).casFeatCode_params), i, jcasType.ll_cas.ll_getFSRef(v));
    }
}
