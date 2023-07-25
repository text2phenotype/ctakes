package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import java.util.Comparator;
import java.util.List;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

public class AnnotationUtils {

    private static Comparator<IdentifiedAnnotation> startEndComparator = new Comparator<IdentifiedAnnotation>() {
        @Override
        public int compare(IdentifiedAnnotation o1, IdentifiedAnnotation o2) {
            int startResult = Integer.compare(o1.getBegin(), o2.getBegin());

            return startResult != 0 ? startResult : Integer.compare(o1.getEnd(), o2.getEnd());
        }
    };

    private AnnotationUtils() { throw new AssertionError(); }

    /**
     * Merges the same annotations. Removes duplicates and combines ontology concepts
     */
    public static void mergeIdentifiedAnnotations(JCas jcas, List<IdentifiedAnnotation> annotations) {
    	int hashCode = jcas.hashCode();
    	if (DEBUG)  LOGGER.debug("[" + hashCode + "] Merging " + annotations.size() + " annotations...");
    	
        IdentifiedAnnotation lastAnnotation = null;
        annotations.sort(startEndComparator);
        int removedCount = 0;
        for (IdentifiedAnnotation currentAnnotation : annotations) {
            if (lastAnnotation != null
                    && currentAnnotation.getClass() == lastAnnotation.getClass()
                    && startEndComparator.compare(currentAnnotation, lastAnnotation) == 0
                    ) {
                // add ontology concepts to last annotation
                FSArray lastAnnotationConcepts = lastAnnotation.getOntologyConceptArr();
                FSArray currentAnnotationConcepts = currentAnnotation.getOntologyConceptArr();

                if (currentAnnotationConcepts != null) {
                    int lastConceptsCount = 0;
                    if (lastAnnotationConcepts != null) {
                        lastConceptsCount = lastAnnotationConcepts.size();
                    }

                    int currentConceptsCount = currentAnnotationConcepts.size();

                    FSArray mergedArray = new FSArray(jcas, lastConceptsCount + currentConceptsCount);

                    if (lastAnnotationConcepts != null) {
                        for (int i = 0; i < lastConceptsCount; ++i) {
                            mergedArray.set(i, lastAnnotationConcepts.get(i));
                        }
                    }

                    for (int j = 0; j < currentConceptsCount; ++j) {
                        mergedArray.set(j + lastConceptsCount, currentAnnotationConcepts.get(j));
                    }

                    lastAnnotation.setOntologyConceptArr(mergedArray);
                }

                currentAnnotation.removeFromIndexes();
                removedCount++;
            } else {
                lastAnnotation = currentAnnotation;
            }
        }
        
        if (DEBUG)  LOGGER.debug("[" + hashCode + "] Removed " + removedCount + " annotations.");
    }
    
    static private final Logger LOGGER = Logger.getLogger(AnnotationUtils.class);
    static private final boolean DEBUG = LOGGER.isDebugEnabled();
}
