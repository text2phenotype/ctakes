package com.text2phenotype.ctakes.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

public class AnnotationUtilsTests {
	@Test
	public void test_getCoveredTokens_matchToMiddle() throws UIMAException {
		 final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 15);
		Annotation annotation2 = new Annotation(jcas, 20, 25);
		Annotation annotation3 = new Annotation(jcas, 26, 30);
		
		List<Annotation> annotations = Arrays.asList(annotation1, annotation2, annotation3);
		
		Annotation key = new Annotation(jcas, 12, 28);
		
		List<Annotation> matched = AnnotationUtils.getCoveredTokens(annotations, key);
		
		assertEquals(1, matched.size());
		assertEquals(matched.get(0), annotation2);
	}
	
	@Test
	public void test_getCoveredTokens_noMatches_beforeFirst() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation = new Annotation(jcas, 10, 15);
		
		List<Annotation> annotations = Arrays.asList(annotation);
		
		Annotation key = new Annotation(jcas, 1, 2);
		
		assertEquals(0, AnnotationUtils.getCoveredTokens(annotations, key).size());
	}
	
	@Test
	public void test_getCoveredTokens_noMatches_afterLast() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation = new Annotation(jcas, 10, 15);
		
		List<Annotation> annotations = Arrays.asList(annotation);
		
		Annotation key = new Annotation(jcas, 16, 20);
		
		assertEquals(0, AnnotationUtils.getCoveredTokens(annotations, key).size());
	}
	
	@Test
	public void test_getCoveredTokens_matchToBeginning() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 15);
		Annotation annotation2 = new Annotation(jcas, 20, 25);
		Annotation annotation3 = new Annotation(jcas, 26, 30);
		
		List<Annotation> annotations = Arrays.asList(annotation1, annotation2, annotation3);
		
		Annotation key = new Annotation(jcas, 10, 25);
		
		List<Annotation> matched = AnnotationUtils.getCoveredTokens(annotations, key);
		
		assertEquals(2, matched.size());
		assertEquals(matched.get(0), annotation1);
		assertEquals(matched.get(1), annotation2);
	}
	
	@Test
	public void test_getCoveredTokens_matchToEnd() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 15);
		Annotation annotation2 = new Annotation(jcas, 20, 25);
		Annotation annotation3 = new Annotation(jcas, 26, 30);
		
		List<Annotation> annotations = Arrays.asList(annotation1, annotation2, annotation3);
		
		Annotation key = new Annotation(jcas, 20, 30);
		
		List<Annotation> matched = AnnotationUtils.getCoveredTokens(annotations, key);
		
		assertEquals(2, matched.size());
		assertEquals(matched.get(0), annotation2);
		assertEquals(matched.get(1), annotation3);
	}
	
	@Test
	public void test_begin_end_comparator_beginBefore() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 15);
		Annotation annotation2 = new Annotation(jcas, 20, 25);
		
		assertEquals(-1, AnnotationUtils.BEGIN_AND_END_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_end_comparator_beginAfter() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 15);
		Annotation annotation2 = new Annotation(jcas, 20, 25);
		
		assertEquals(1, AnnotationUtils.BEGIN_AND_END_COMPARATOR.compare(annotation2, annotation1));
	}
	
	@Test
	public void test_begin_end_comparator_equal() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 15);
		Annotation annotation2 = new Annotation(jcas, 10, 15);
		
		assertEquals(0, AnnotationUtils.BEGIN_AND_END_COMPARATOR.compare(annotation2, annotation1));
	}
	
	@Test
	public void test_begin_end_comparator_endBefore() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 10, 15);
		
		assertEquals(-1, AnnotationUtils.BEGIN_AND_END_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_end_comparator_endAfter() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 10, 15);
		
		assertEquals(1, AnnotationUtils.BEGIN_AND_END_COMPARATOR.compare(annotation2, annotation1));
	}
	
	@Test
	public void test_begin_strict_comparator_beginBefore() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 11, 15);
		
		assertEquals(-1, AnnotationUtils.BEGIN_STRICT_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_strict_comparator_beginAfter() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 11, 15);
		
		assertEquals(1, AnnotationUtils.BEGIN_STRICT_COMPARATOR.compare(annotation2, annotation1));
	}
	
	@Test
	public void test_begin_strict_comparator_beginEqualBeginEndNotEqual() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 10, 11);
		
		assertEquals(1, AnnotationUtils.BEGIN_STRICT_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_strict_comparator_beginEqualBeginEndEqual() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 10, 14);
		
		assertEquals(0, AnnotationUtils.BEGIN_STRICT_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_comparator_beginBefore() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 11, 14);
		
		assertEquals(-1, AnnotationUtils.BEGIN_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_comparator_beginEqual() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 13);
		Annotation annotation2 = new Annotation(jcas, 10, 14);
		
		assertEquals(0, AnnotationUtils.BEGIN_COMPARATOR.compare(annotation1, annotation2));
	}
	
	@Test
	public void test_begin_comparator_beginAfter() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		Annotation annotation1 = new Annotation(jcas, 10, 14);
		Annotation annotation2 = new Annotation(jcas, 11, 14);
		
		assertEquals(1, AnnotationUtils.BEGIN_COMPARATOR.compare(annotation2, annotation1));
	}
}
