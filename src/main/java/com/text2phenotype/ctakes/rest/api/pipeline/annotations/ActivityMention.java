package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

public class ActivityMention extends EventMention {

    public final static int typeIndexID = JCasRegistry.register(ActivityMention.class);

    @SuppressWarnings ("hiding")
    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {return typeIndexID;}

    protected ActivityMention() {/* intentionally empty block */}

    public ActivityMention(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public ActivityMention(JCas jcas) {
        super(jcas);
        readObject();
    }

    public ActivityMention(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    private void readObject() {/*default - does nothing empty block */}
}
