package com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute;

import org.apache.ctakes.typesystem.type.refsem.Attribute;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

public class NPIAttributes extends Attribute {

    public final static int typeIndexID = JCasRegistry.register(NPIAttributes.class);

    public final static int type = typeIndexID;

    protected NPIAttributes() {/* intentionally empty block */}

    public NPIAttributes(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public NPIAttributes(JCas jcas) {
        super(jcas);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}

    @Override
    public int getTypeIndexID() {return typeIndexID;}


    public String getStreet() {
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        return attrs_type.getStreet(addr);
    }
    public String getCity() {
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        return attrs_type.getCity(addr);
    }
    public String getState() {
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        return attrs_type.getState(addr);
    }
    public Long getZip() {
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        return attrs_type.getZip(addr);
    }
    public Long getPhone() {
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        return attrs_type.getPhone(addr);
    }
    public Long getFax() {
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        return attrs_type.getFax(addr);
    }

    public void setStreet(String v){
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        attrs_type.setStreet(addr, v);
    }
    public void setCity(String v){
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        attrs_type.setCity(addr, v);
    }
    public void setState(String v){
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        attrs_type.setState(addr, v);
    }
    public void setZip(Long v){
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        attrs_type.setZip(addr, v);
    }
    public void setPhone(Long v){
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        attrs_type.setPhone(addr, v);
    }
    public void setFax(Long v){
        NPIAttributes_Type attrs_type = ((NPIAttributes_Type)jcasType);
        attrs_type.setFax(addr, v);
    }
}
