package com.text2phenotype.ctakes.rest.api.pipeline.concept;

import org.apache.ctakes.dictionary.lookup2.concept.Concept;

import java.util.Map;

/**
 * Extension of concepts.
 */
public interface Concept2 extends Concept {

    Map<String, String> getParams();
}
