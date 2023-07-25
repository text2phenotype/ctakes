package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

/**
 * Annotation for units
 */
public class UnitAnnotation extends IdentifiedAnnotation {

    @SuppressWarnings ("hiding")
    public final static int typeIndexID = JCasRegistry.register(UnitAnnotation.class);

    @SuppressWarnings ("hiding")
    public final static int type = typeIndexID;

    /**
     * @return index of the type
     */
    @Override
    public int getTypeIndexID() {return typeIndexID;}

    /** Never called.  Disable default constructor
    */
    protected UnitAnnotation() {/* intentionally empty block */}

    /** Internal - constructor
     * @param addr low level Feature Structure reference
     * @param type the type of this Feature Structure
     */
    public UnitAnnotation(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    /**
     * @param jcas JCas to which this Feature Structure belongs
     */
    public UnitAnnotation(JCas jcas) {
        super(jcas);
        readObject();
    }

    /**
     * @param jcas JCas to which this Feature Structure belongs
     * @param begin offset to the begin spot in the SofA
     * @param end offset to the end spot in the SofA
     */
    public UnitAnnotation(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    /**
     * Write your own initialization here
     */
    private void readObject() {/*default - does nothing empty block */}
}
