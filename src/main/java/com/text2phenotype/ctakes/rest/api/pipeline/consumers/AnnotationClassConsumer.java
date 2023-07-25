package com.text2phenotype.ctakes.rest.api.pipeline.consumers;

import org.apache.commons.lang.IllegalClassException;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.consumer.DefaultUmlsConceptCreator;
import org.apache.ctakes.dictionary.lookup2.consumer.TermConsumer;
import org.apache.ctakes.dictionary.lookup2.consumer.UmlsConceptCreator;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.dictionary.lookup2.util.CuiCodeUtil;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

/**
 * Creates annotations of special class
 */
public class AnnotationClassConsumer implements TermConsumer {

    private final Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /**
     * annotation class
     */
    private static final String PROPERTY_ANNOTATION_CLASS = "annotationClass";

    private Class<? extends IdentifiedAnnotation> annotationClass;
    private String defaultCodingScheme;

    private static final UmlsConceptCreator umlsConceptCreator = new DefaultUmlsConceptCreator();

    public AnnotationClassConsumer(final UimaContext uimaContext, final Properties properties ) {
        defaultCodingScheme = properties.getProperty( "codingScheme", "UNKNOWN");

        String annotationClassName = properties.getProperty( PROPERTY_ANNOTATION_CLASS, null );
        if (annotationClassName == null) {
            throw new IllegalClassException("Annotation class name is not found");
        }
        try {
            Class c = Class.forName(annotationClassName);
            if (!IdentifiedAnnotation.class.isAssignableFrom(c)){
                String messageText = String.format("Annotation class '%s' should be inherited from IdentifiedAnnotation", annotationClassName);
                logger.error(messageText);
                throw new IllegalClassException(messageText);
            }
            annotationClass = (Class<? extends IdentifiedAnnotation>) c;

        } catch (ClassNotFoundException e) {
            logger.warn(String.format("Annotation class '%s' not found", annotationClassName));
        }

    }

    private Collection<UmlsConcept> createUMLSConcepts(JCas jcas,
                                                       Collection<Long> cuis,
                                                       CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConcepts ) {
        final Collection<UmlsConcept> result = new HashSet<>();

        cuis.forEach(cui -> {
            Collection<Concept> concepts = cuiConcepts.get(cui);
            concepts.forEach(concept-> {
                Collection<String> tuis = concept.getCodes(Concept.TUI);


                tuis.forEach(tui -> result.addAll(umlsConceptCreator.createUmlsConcepts(jcas, defaultCodingScheme, tui, concept)));
            });
        });

        return result;

    }

    @Override
    public void consumeHits(JCas jcas, RareWordDictionary dictionary, CollectionMap<TextSpan, Long, ? extends Collection<Long>> textSpanCuis, CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConcepts) throws AnalysisEngineProcessException {

            try {
                Constructor<?> ctor = annotationClass.getConstructor(JCas.class);
                for (TextSpan span : textSpanCuis.keySet()) {
                    Collection<Long> cuis = textSpanCuis.get(span);

                    final Collection<UmlsConcept> umlsConcepts = createUMLSConcepts(jcas, cuis, cuiConcepts);

                    final FSArray conceptArr = new FSArray( jcas, umlsConcepts.size() );
                    int idx = 0;
                    for (UmlsConcept umlsConcept: umlsConcepts) {
                        conceptArr.set(idx, umlsConcept);
                        idx++;
                    }

                    IdentifiedAnnotation newAnnotation = (IdentifiedAnnotation)ctor.newInstance(jcas);
                    newAnnotation.setBegin(span.getStart());
                    newAnnotation.setEnd(span.getEnd());
                    newAnnotation.setOntologyConceptArr(conceptArr);
                    newAnnotation.addToIndexes();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());

            }
    }

    @Override
    public void consumeTypeIdHits(JCas jcas, String codingScheme, int cTakesSemantic, CollectionMap<TextSpan, Long, ? extends Collection<Long>> textSpanCuis, CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConcepts) throws AnalysisEngineProcessException {
        // not used
    }
}
