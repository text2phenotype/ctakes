package com.text2phenotype.ctakes.rest.api.pipeline.annotations;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.SpecialLabValueWord;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation_Type;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

public class SpecialLabValueWord_Type extends IdentifiedAnnotation_Type {

    public static final int typeIndexID;
    public static final boolean featOkTst;

    public SpecialLabValueWord_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, this.getFSGenerator());
    }

    static {
        typeIndexID = SpecialLabValueWord.typeIndexID;
        featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.SpecialLabValueWord");
    }
}
