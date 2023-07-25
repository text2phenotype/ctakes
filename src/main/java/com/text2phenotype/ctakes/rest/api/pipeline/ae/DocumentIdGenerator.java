package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import java.util.UUID;

/**
 * Generates random Document ID (UUID)
 */
public class DocumentIdGenerator extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        DocumentID docId = new DocumentID(aJCas);
        docId.setDocumentID(UUID.randomUUID().toString());
        docId.addToIndexes();
    }
}
