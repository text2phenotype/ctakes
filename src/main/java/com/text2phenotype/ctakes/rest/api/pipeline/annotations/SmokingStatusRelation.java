package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.*;

import java.util.List;

public class SmokingStatusRelation extends TOP {

    public final static int typeIndexID = JCasRegistry.register(SmokingStatusRelation.class);

    @SuppressWarnings ("hiding")
    public final static int type = typeIndexID;

    @Override
    public int getTypeIndexID() {return typeIndexID;}

    protected SmokingStatusRelation() {}

    public SmokingStatusRelation(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    public SmokingStatusRelation(JCas jcas) {
        super(jcas);
        readObject();
    }

    private void readObject() {}

    public Sentence getSentence() {
        SmokingStatusRelation_Type tp = (SmokingStatusRelation_Type)jcasType;
        return tp.getSentence(addr);
    }

    public void setSentence(Sentence sent) {
        SmokingStatusRelation_Type tp = (SmokingStatusRelation_Type)jcasType;
        tp.setSentence(addr, sent);
    }


    public StringArray getStatuses() {
        SmokingStatusRelation_Type tp = (SmokingStatusRelation_Type)jcasType;
        return tp.getStatuses(addr);
    }

    public void setStatuses(StringArray statuses) {
        SmokingStatusRelation_Type tp = (SmokingStatusRelation_Type)jcasType;
        tp.setStatuses(addr, statuses);
    }
}
