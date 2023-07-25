package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes;
import org.apache.ctakes.typesystem.type.textsem.EntityMention_Type;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention_Type;
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

public class NationalProviderMention_Type  extends EntityMention_Type {
    public final static int typeIndexID = NationalProviderMention.typeIndexID;
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention");

    private static final int MATCH_TYPE = 0;
    private static final int SAB = 1;
    private static final int CODE = 2;
    private static final int TUI = 3;
    private static final int PREF_TEXT = 4;
    private static final int MAILING_ADDRESS = 5;
    private static final int PHYSICAL_ADDRESS = 6;

    private static final List<String> feature_names = Arrays.asList(
            "match_type", "sab", "code", "tui", "prefText", "mailingAddress", "physicalAddress"
    );

    final Map<Integer, Feature> features = new HashMap<>();
    final Map<Integer, Integer> features_codes = new HashMap<>();

    public NationalProviderMention_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

        // init string features
        for (int i=0; i <= 4; i++) {
            Feature feat = jcas.getRequiredFeatureDE(casType, feature_names.get(i), "uima.cas.String", featOkTst);
            int feat_code = (null == feat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)feat).getCode();

            features.put(i, feat);
            features_codes.put(i, feat_code);
        }

        // init addresses features
        for (int i=5; i <= 6; i++) {
            Feature feat = jcas.getRequiredFeatureDE(casType, feature_names.get(i), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes", featOkTst);
            int feat_code = (null == feat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)feat).getCode();

            features.put(i, feat);
            features_codes.put(i, feat_code);
        }
    }

    private String getFeatureVal(int addr, int feature_type){
        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention");
        return ll_cas.ll_getStringValue(addr, features_codes.get(feature_type));
    }

    private void setFeatureVal(int addr, String v, int feature_type) {

        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention");
        ll_cas.ll_setStringValue(addr, features_codes.get(feature_type), v);
    }

    public String getMatchType(int addr) {
        return getFeatureVal(addr, MATCH_TYPE);
    }
    public String getSAB(int addr) {
        return getFeatureVal(addr, SAB);
    }
    public String getCode(int addr) {
        return getFeatureVal(addr, CODE);
    }
    public String getTUI(int addr) {
        return getFeatureVal(addr, TUI);
    }
    public String getPrefText(int addr) {
        return getFeatureVal(addr, PREF_TEXT);
    }

    public void setMatchType(int addr, String v){
        setFeatureVal(addr, v, MATCH_TYPE);
    }
    public void setSAB(int addr, String v){
        setFeatureVal(addr, v, SAB);
    }
    public void setCode(int addr, String v){
        setFeatureVal(addr, v, CODE);
    }
    public void setTUI(int addr, String v){
        setFeatureVal(addr, v, TUI);
    }
    public void setPrefText(int addr, String v){
        setFeatureVal(addr, v, PREF_TEXT);
    }


    private NPIAttributes getAddressFeatureVal(int addr, int feature_type){
        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention");
        return (NPIAttributes)ll_cas.ll_getFSForRef(ll_cas.ll_getRefValue(addr, features_codes.get(feature_type)));
    }

    private void setAddressFeatureVal(int addr, NPIAttributes v, int feature_type) {

        if (featOkTst && features.get(feature_type) == null)
            jcas.throwFeatMissing(feature_names.get(feature_type), "com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention");
        ll_cas.ll_setRefValue(addr, features_codes.get(feature_type), ll_cas.ll_getFSRef(v));
    }

    public NPIAttributes getMailingAddress(int addr) {
        return getAddressFeatureVal(addr, MAILING_ADDRESS);
    }
    public NPIAttributes getPhysicalAddress(int addr) {
        return getAddressFeatureVal(addr, PHYSICAL_ADDRESS);
    }

    public void setMailingAddress(int addr, NPIAttributes v){
        setAddressFeatureVal(addr, v, MAILING_ADDRESS);
    }
    public void setPhysicalAddress(int addr, NPIAttributes v){
        setAddressFeatureVal(addr, v, PHYSICAL_ADDRESS);
    }
}