package com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute;

import org.apache.ctakes.typesystem.type.refsem.Attribute_Type;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPIAttributes_Type extends Attribute_Type {

    public final static int typeIndexID = NPIAttributes.typeIndexID;

    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes");

    private static final int STREET = 0;
    private static final int CITY = 1;
    private static final int STATE = 2;
    private static final int ZIP = 3;
    private static final int PHONE = 4;
    private static final int FAX = 5;

    private static final List<String> feature_names = Arrays.asList(
            "street", "city", "state", "zip", "phone", "fax"
    );

    final Map<Integer, Feature> features = new HashMap<>();
    final Map<Integer, Integer> features_codes = new HashMap<>();


    public NPIAttributes_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

        for (int i=0; i <= 2; i++) {
            Feature feat = jcas.getRequiredFeatureDE(casType, feature_names.get(i), "uima.cas.String", featOkTst);
            int feat_code = (null == feat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)feat).getCode();

            features.put(i, feat);
            features_codes.put(i, feat_code);
        }

        for (int i=3; i <= 5; i++) {
            Feature feat = jcas.getRequiredFeatureDE(casType, feature_names.get(i), "uima.cas.Long", featOkTst);
            int feat_code = (null == feat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)feat).getCode();

            features.put(i, feat);
            features_codes.put(i, feat_code);
        }
    }

    private String getStringFeatureVal(int addr, int feature_type){
        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes");
        return ll_cas.ll_getStringValue(addr, features_codes.get(feature_type));
    }

    private void setStringFeatureVal(int addr, String v, int feature_type) {

        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes");
        ll_cas.ll_setStringValue(addr, features_codes.get(feature_type), v);
    }

    private Long getLongFeatureVal(int addr, int feature_type){
        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes");
        return ll_cas.ll_getLongValue(addr, features_codes.get(feature_type));
    }

    private void setLongFeatureVal(int addr, Long v, int feature_type) {

        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes");
        ll_cas.ll_setLongValue(addr, features_codes.get(feature_type), v);
    }

    public String getStreet(int addr) {
        return getStringFeatureVal(addr, STREET);
    }
    public String getCity(int addr) {
        return getStringFeatureVal(addr, CITY);
    }
    public String getState(int addr) {
        return getStringFeatureVal(addr, STATE);
    }
    public Long getZip(int addr) {
        return getLongFeatureVal(addr, ZIP);
    }
    public Long getPhone(int addr) {
        return getLongFeatureVal(addr, PHONE);
    }
    public Long getFax(int addr) {
        return getLongFeatureVal(addr, FAX);
    }

    public void setStreet(int addr, String v){
        setStringFeatureVal(addr, v, STREET);
    }
    public void setCity(int addr, String v){
        setStringFeatureVal(addr, v, CITY);
    }
    public void setState(int addr, String v){
        setStringFeatureVal(addr, v, STATE);
    }
    public void setZip(int addr, Long v){
        setLongFeatureVal(addr, v, ZIP);
    }
    public void setPhone(int addr, Long v){
        setLongFeatureVal(addr, v, PHONE);
    }
    public void setFax(int addr, Long v){
        setLongFeatureVal(addr, v, FAX);
    }

}
