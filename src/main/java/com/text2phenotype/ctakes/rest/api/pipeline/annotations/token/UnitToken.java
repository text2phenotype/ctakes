package com.text2phenotype.ctakes.rest.api.pipeline.annotations.token;

import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

/**
 * Lab unit token
 */
public class UnitToken extends WordToken {

    public final static int typeIndexID = JCasRegistry.register(UnitToken.class);
    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {
        return typeIndexID;
    }

    protected UnitToken() {/* intentionally empty block */}

    public UnitToken(JCas jcas) {
        super(jcas);
    }

    public UnitToken(int addr, TOP_Type type) {
        super(addr, type);
    }

    public UnitToken(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
    }


}
