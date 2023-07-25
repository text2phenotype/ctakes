package com.text2phenotype.ctakes.rest.api.pipeline.annotations.token;

import org.apache.ctakes.typesystem.type.syntax.WordToken_Type;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

public class UnitToken_Type extends WordToken_Type {

    public final static int typeIndexID = UnitToken.typeIndexID;
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken");

    /** initialize variables to correspond with Cas Type and Features
     * @generated
     * @param jcas JCas
     * @param casType Type
     */
    public UnitToken_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());
    }
}
