package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

/**
 * Relation for lab units
 * */
public class UnitRelation extends BinaryTextRelation {

    @SuppressWarnings ("hiding")
    public final static int typeIndexID = JCasRegistry.register(UnitRelation.class);

    @SuppressWarnings ("hiding")
    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {return typeIndexID;}

    protected UnitRelation() {/* intentionally empty block */}

    public UnitRelation(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public UnitRelation(JCas jcas) {
        super(jcas);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}

}

