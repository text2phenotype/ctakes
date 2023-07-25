package com.text2phenotype.ctakes.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Utility methods for annotations.
 * @author mike.banos
 *
 */
public class AnnotationUtils {
	/**
     * Get the list of annotations covered by a given span.
     * @param <T> The annotation type.
     * @param annotations The pre-sorted list of annotations to search.
     * @param key The annotation defining the search span.
     * @return The annotations falling within the defined span.
     */
    final public static<T extends Annotation> List<T> getCoveredTokens(final List<T> annotations, final T key) {
    	List<T> covered = new ArrayList<T>();
    	
    	int searchStart = getCoverStartIndex(annotations, key);
    	
    	T annotation;
    	while (searchStart < annotations.size()) {
    		annotation = annotations.get(searchStart++);
    		
    		if (annotation.getBegin() >= key.getEnd())  break;
    		
    		if (annotation.getEnd() <= key.getEnd())  covered.add(annotation);
    	}

    	return covered;
    }
    
    /**
     * Get the index of the first candidate covered annotation.
     * @return
     */
    private static<T extends Annotation> int getCoverStartIndex(final List<T> annotations, final T key) {
    	int searchStart = Collections.binarySearch(annotations, key, BEGIN_COMPARATOR);
    	
    	// no matching begin index found
    	if (searchStart < 0)  return Math.abs(searchStart + 1);
    	
    	if (DEBUG)  LOGGER.debug("raw search start: " + searchStart);
    	
    	// found a matching begin index, make sure we're at the first annotation with that begin index
		while (searchStart > 0 && annotations.get(searchStart - 1).getBegin() >= key.getBegin()) {
    		searchStart--;
    	}
		
		if (DEBUG)  LOGGER.debug("adjusted search start: " + searchStart);
    	
    	return searchStart;
    }
    
    /**
	 * Comparator to sort annotations by begin index.
	 */
	final public static Comparator<Annotation> BEGIN_COMPARATOR = new Comparator<Annotation>() {
		@Override
		public int compare(Annotation a1, Annotation a2) {
			return Integer.compare(a1.getBegin(), a2.getBegin());
        }
	};
	
	/**
	 * Comparator to sort annotations first by begin index.
	 * Annotations are considered equal only the case that both begin and end indices are equal.
	 */
	final public static Comparator<Annotation> BEGIN_STRICT_COMPARATOR = new Comparator<Annotation>() {
		@Override
		public int compare(Annotation a1, Annotation a2) {
        	if (a1.getBegin() < a2.getBegin())  return -1;
        	
        	if (a1.getBegin() == a2.getBegin() && a1.getEnd() == a2.getEnd())  return 0;
        	
            return 1;
        }
	};
	
	/**
	 * Comparator to sort annotations first by begin index, then by end index in the case of a begin tie.
	 */
	final public static Comparator<Annotation> BEGIN_AND_END_COMPARATOR = new Comparator<Annotation>() {
		@Override
		public int compare(Annotation a1, Annotation a2) {
        	if (a1.getBegin() == a2.getBegin() && a1.getEnd() < a2.getEnd()) {
        		return -1;
        	}

            return BEGIN_STRICT_COMPARATOR.compare(a1, a2);
        }
	};
	
	private static final Logger LOGGER = Logger.getLogger(AnnotationUtils.class);
	private static final boolean DEBUG = LOGGER.isDebugEnabled();
}
