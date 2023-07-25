package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

public class NationalProviderMention extends EntityMention {

    public final static int typeIndexID = JCasRegistry.register(NationalProviderMention.class);

    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {return typeIndexID;}

    protected NationalProviderMention() {/* intentionally empty block */}

    public NationalProviderMention(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public NationalProviderMention(JCas jcas) {
        super(jcas);
        readObject();
    }

    public NationalProviderMention(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}

    public String getMatchType() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getMatchType(addr);
    }
    public String getSAB() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getSAB(addr);
    }
    public String getCode() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getCode(addr);
    }
    public String getTUI() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getTUI(addr);
    }
    public String getPrefText() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getPrefText(addr);
    }
    public NPIAttributes getMailingAddress() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getMailingAddress(addr);
    }
    public NPIAttributes getPhysicalAddress() {
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        return tp.getPhysicalAddress(addr);
    }

    public void setMatchType(String v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setMatchType(addr, v);
    }
    public void setSAB(String v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setSAB(addr, v);
    }
    public void setCode(String v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setCode(addr, v);
    }
    public void setTUI(String v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setTUI(addr, v);
    }
    public void setPrefText(String v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setPrefText(addr, v);
    }
    public void setMailingAddress(NPIAttributes v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setMailingAddress(addr, v);
    }
    public void setPhysicalAddress(NPIAttributes v){
        NationalProviderMention_Type tp = (NationalProviderMention_Type)jcasType;
        tp.setPhysicalAddress(addr, v);
    }
}