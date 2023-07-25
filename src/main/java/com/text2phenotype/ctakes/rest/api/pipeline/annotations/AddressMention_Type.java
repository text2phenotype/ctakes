package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation_Type;
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

public class AddressMention_Type extends IdentifiedAnnotation_Type {
    public final static int typeIndexID = AddressMention.typeIndexID;
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.AddressMention");

    private static final int MATCH_TYPE = 0;
    private static final int STREET = 1;
    private static final int CITY = 2;
    private static final int STATE = 3;
    private static final int ZIP = 4;

    private static final List<String> feature_names = Arrays.asList(
            "match_type", "street", "city", "state", "zip"
    );

    final Map<Integer, Feature> features = new HashMap<>();
    final Map<Integer, Integer> features_codes = new HashMap<>();

    Feature isPationAddressFeature;
    int isPationAddressFeatureCode;

    public AddressMention_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

        // init string features
        for (int i=0; i < feature_names.size(); i++) {
            Feature feat = jcas.getRequiredFeatureDE(casType, feature_names.get(i), "uima.cas.String", featOkTst);
            int feat_code = (null == feat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)feat).getCode();

            features.put(i, feat);
            features_codes.put(i, feat_code);
        }

        isPationAddressFeature = jcas.getRequiredFeatureDE(casType, "isPatientAddress", "uima.cas.Boolean", featOkTst);
        isPationAddressFeatureCode = (null == isPationAddressFeature) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)isPationAddressFeature).getCode();
    }

    private String getFeatureVal(int addr, int feature_type){
        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.AddressMention");
        return ll_cas.ll_getStringValue(addr, features_codes.get(feature_type));
    }

    private void setFeatureVal(int addr, String v, int feature_type) {

        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.AddressMention");
        ll_cas.ll_setStringValue(addr, features_codes.get(feature_type), v);
    }

    public String getMatchType(int addr) {
        return getFeatureVal(addr, MATCH_TYPE);
    }
    public void setMatchType(int addr, String v){
        setFeatureVal(addr, v, MATCH_TYPE);
    }

    public String getStreet(int addr) {
        return getFeatureVal(addr, STREET);
    }
    public String getCity(int addr) {
        return getFeatureVal(addr, CITY);
    }
    public String getState(int addr) {
        return getFeatureVal(addr, STATE);
    }
    public String getZip(int addr) {
        return getFeatureVal(addr, ZIP);
    }

    public void setStreet(int addr, String v){
        setFeatureVal(addr, v, STREET);
    }
    public void setCity(int addr, String v){
        setFeatureVal(addr, v, CITY);
    }
    public void setState(int addr, String v){
        setFeatureVal(addr, v, STATE);
    }
    public void setZip(int addr, String v){
        setFeatureVal(addr, v, ZIP);
    }

    public Boolean isPatientAddress(int addr) {
        if (featOkTst && isPationAddressFeature == null)
            jcas.throwFeatMissing("isPatientAddress", "com.text2phenotype.ctakes.rest.api.pipeline.annotations.AddressMention");
        return ll_cas.ll_getBooleanValue(addr, isPationAddressFeatureCode);
    }
}
