package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import org.apache.ctakes.typesystem.type.textsem.EventMention_Type;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

public class ActivityMention_Type extends EventMention_Type {
    public final static int typeIndexID = ActivityMention.typeIndexID;
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.ActivityMention");

    public ActivityMention_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

    }
}
