package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

public class PhoneNumberMention extends IdentifiedAnnotation {

    public final static int typeIndexID = JCasRegistry.register(PhoneNumberMention.class);

    public final static int type = typeIndexID;
    /**
     * @return index of the type
     */
    @Override
    public int getTypeIndexID() {return typeIndexID;}

    /** Never called.  Disable default constructor
     */
    protected PhoneNumberMention() {/* intentionally empty block */}

    /** Internal - constructor used by generator
     * @param addr low level Feature Structure reference
     * @param type the type of this Feature Structure
     */
    public PhoneNumberMention(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    /**
     * @param jcas JCas to which this Feature Structure belongs
     */
    public PhoneNumberMention(JCas jcas) {
        super(jcas);
        readObject();
    }

    /**
     * @param jcas JCas to which this Feature Structure belongs
     * @param begin offset to the begin spot in the SofA
     * @param end offset to the end spot in the SofA
     */
    public PhoneNumberMention(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}

    public Long getNumber(){
        PhoneNumberMention_Type tp = (PhoneNumberMention_Type)jcasType;
        return tp.getNumber(addr);
    }

    public void setNumber(Long v) {
        PhoneNumberMention_Type tp = (PhoneNumberMention_Type)jcasType;
        tp.setNumber(addr, v);
    }
}
