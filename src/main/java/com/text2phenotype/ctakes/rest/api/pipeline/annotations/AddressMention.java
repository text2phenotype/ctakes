package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

public class AddressMention extends IdentifiedAnnotation {
    public final static int typeIndexID = JCasRegistry.register(AddressMention.class);

    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {return typeIndexID;}

    protected AddressMention() {/* intentionally empty block */}

    public AddressMention(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public AddressMention(JCas jcas) {
        super(jcas);
        readObject();
    }

    public AddressMention(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}

    public String getMatchType() {
        AddressMention_Type tp = (AddressMention_Type)jcasType;
        return tp.getMatchType(addr);
    }

    public String getStreet() {
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        return attrs_type.getStreet(addr);
    }
    public String getCity() {
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        return attrs_type.getCity(addr);
    }
    public String getState() {
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        return attrs_type.getState(addr);
    }
    public String getZip() {
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        return attrs_type.getZip(addr);
    }

    public Boolean isPatientAddress() {
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        return attrs_type.isPatientAddress(addr);
    }

    public void setMatchType(String v){
        AddressMention_Type tp = (AddressMention_Type)jcasType;
        tp.setMatchType(addr, v);
    }

    public void setStreet(String v){
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        attrs_type.setStreet(addr, v);
    }
    public void setCity(String v){
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        attrs_type.setCity(addr, v);
    }
    public void setState(String v){
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        attrs_type.setState(addr, v);
    }
    public void setZip(String v){
        AddressMention_Type attrs_type = ((AddressMention_Type)jcasType);
        attrs_type.setZip(addr, v);
    }
}
