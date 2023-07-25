package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

public class AddEvent extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        for (EventMention emention : JCasUtil.select(jCas, EventMention.class)) {
            EventProperties eventProperties = new org.apache.ctakes.typesystem.type.refsem.EventProperties(jCas);

            Event event = new Event(jCas);

            event.setProperties(eventProperties);
            emention.setEvent(event);

            eventProperties.addToIndexes();
            event.addToIndexes();
        }
    }
}
