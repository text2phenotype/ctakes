package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class DrugMentionAnnotatorWithPositionsTests {
	@Test
	public void test_findTotalCovered_noSpans() {
		final int[] EXPECTED = {Integer.MAX_VALUE, 0};
		final int[][] spans = new int[0][];
		
		assertTrue(Arrays.equals(EXPECTED, DrugMentionAnnotatorWithPositions.findTotalCovered(spans)));
	}
	
	@Test
	public void test_findTotalCovered_validSpans() {
		final int[] EXPECTED = {5, 1000};
		final int[][] spans = {{10, 100}, {25, 1000}, {5, 999}};
		
		assertTrue(Arrays.equals(EXPECTED, DrugMentionAnnotatorWithPositions.findTotalCovered(spans)));
	}
	
	@Test
	public void test_limitLongSpans_noMedications() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		final int[][] expected = {{0, Integer.MAX_VALUE}};
		int[][] spans = {{0, Integer.MAX_VALUE}};
		
		new DrugMentionAnnotatorWithPositions().limitLongSpans(jcas, spans);
		
		assertArraysEqual(expected, spans);
	}
	
	@Test
	public void test_limitLongSpans_noMedicationsInRange() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		final int[][] expected = {{0, 100}};
		int[][] spans = {{0, 100}};
		
		MedicationMention annotation = new MedicationMention(jcas);
		annotation.setBegin(1);
		annotation.setEnd(101);
		annotation.addToIndexes();
		
		for (int i = 0; i < DrugMentionAnnotatorWithPositions.DEFAULT_MAX_ATTR_DISTANCE + 1; i++) {
			BaseToken token = new BaseToken(jcas);
			token.setBegin(i);
			token.setEnd(i + 1);
			token.addToIndexes();	
		}
		
		new DrugMentionAnnotatorWithPositions().limitLongSpans(jcas, spans);
		
		assertArraysEqual(expected, spans);
	}
	
	@Test
	public void test_limitLongSpans_noBaseTokensInRange() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		final int[][] expected = {{0, 100}};
		int[][] spans = {{0, 100}};
		
		MedicationMention annotation = new MedicationMention(jcas);
		annotation.setBegin(1);
		annotation.setEnd(10);
		annotation.addToIndexes();
		
		for (int i = 0; i < DrugMentionAnnotatorWithPositions.DEFAULT_MAX_ATTR_DISTANCE + 1; i++) {
			BaseToken token = new BaseToken(jcas);
			token.setBegin(101 + i);
			token.setEnd(102 + i);
			token.addToIndexes();	
		}
		
		new DrugMentionAnnotatorWithPositions().limitLongSpans(jcas, spans);
		
		assertArraysEqual(expected, spans);
	}
	
	@Test
	public void test_limitLongSpans_noTruncation() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		int[][] spans = {{0, 100}};
		final int[][] expected = {{0, 100}};
		
		MedicationMention annotation = new MedicationMention(jcas);
		annotation.setBegin(1);
		annotation.setEnd(10);
		annotation.addToIndexes();
		
		for (int i = 0; i < DrugMentionAnnotatorWithPositions.DEFAULT_MAX_ATTR_DISTANCE; i++) {
			BaseToken token = new BaseToken(jcas);
			token.setBegin(12 + i);
			token.setEnd(15 + i);
			token.addToIndexes();	
		}
		
		new DrugMentionAnnotatorWithPositions().limitLongSpans(jcas, spans);
		
		assertArraysEqual(expected, spans);
	}
	
	@Test
	public void test_limitLongSpans_spanTruncated() throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		
		int[][] spans = {{0, 100}};
		final int[][] expected = {{0, 24}};
		
		MedicationMention annotation = new MedicationMention(jcas);
		annotation.setBegin(1);
		annotation.setEnd(10);
		annotation.addToIndexes();
		
		for (int i = 0; i < DrugMentionAnnotatorWithPositions.DEFAULT_MAX_ATTR_DISTANCE + 1; i++) {
			BaseToken token = new BaseToken(jcas);
			token.setBegin(12 + i);
			token.setEnd(15 + i);
			token.addToIndexes();	
		}
		
		new DrugMentionAnnotatorWithPositions().limitLongSpans(jcas, spans);
		
		assertArraysEqual(expected, spans);
	}
	
	private static void assertArraysEqual(final int[][] a1, final int[][] a2) {
		assertEquals(a1.length, a2.length);
		
		for (int i = 0; i < a1.length; i++) {
			assertEquals(a1[i][0], a2[i][0]);
			assertEquals(a1[i][1], a2[i][1]);
		}
	}
}
