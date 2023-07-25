package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

public class SpecialLabValueWord extends IdentifiedAnnotation {

    public final static int typeIndexID = JCasRegistry.register(SpecialLabValueWord.class);
    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {
        return typeIndexID;
    }

    protected SpecialLabValueWord() {/* intentionally empty block */}

    public SpecialLabValueWord(JCas jcas) {
        super(jcas);
    }

    public SpecialLabValueWord(int addr, TOP_Type type) {
        super(addr, type);
    }

    public SpecialLabValueWord(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
    }


}
