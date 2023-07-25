package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * Term - value pair
 */
public class TermValuePair {
    private Annotation term;
    private Annotation value;

    public TermValuePair(Annotation term, Annotation value) {
        this.term = term;
        this.value = value;
    }

    public Annotation getTerm() {
        return term;
    }

    public Annotation getValue() {
        return value;
    }
}
