package com.text2phenotype.ctakes.rest.api.pipeline.async;

import org.apache.uima.jcas.JCas;

public interface IFreeCasCallback {
    void onFreeCas(JCas jCas);
}
