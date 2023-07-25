package com.text2phenotype.ctakes.rest.api.pipeline.concept;

import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;

import java.util.Collection;

public interface ConceptFactory2 {

    CollectionMap<Long, Concept, ? extends Collection<Concept>> createConceptsCollection(final Collection<Long> cuiCodes );
}
