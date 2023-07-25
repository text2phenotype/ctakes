package com.text2phenotype.ctakes.rest.api.pipeline.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data model for content field
 */
public class ContentModel extends EventModel {

    private Set<UmlsConceptModel> umlsConcepts = new HashSet<>();

    public Set<UmlsConceptModel> getUmlsConcepts() {
        return umlsConcepts;
    }
}
