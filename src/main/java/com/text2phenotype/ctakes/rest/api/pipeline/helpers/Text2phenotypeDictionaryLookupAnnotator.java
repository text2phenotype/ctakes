package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.dictionary.lookup.ae.DictionaryLookupAnnotator;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;


/**
 * Wrapper for DictionaryLookupAnnotator. Used by DrugNER module
 */
public class Text2phenotypeDictionaryLookupAnnotator extends DictionaryLookupAnnotator {

    public static AnalysisEngineDescription createAnnotatorDescription()
            throws ResourceInitializationException {
        try {
            String fullPath = FileLocator.getFullPath("com/text2phenotype/ctakes/resources/DictionaryLookupAnnotator.xml");
            return AnalysisEngineFactory.createEngineDescriptionFromPath(fullPath);
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }


}
