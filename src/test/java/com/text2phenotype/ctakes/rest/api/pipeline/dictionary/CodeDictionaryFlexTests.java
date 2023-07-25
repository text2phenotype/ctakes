package com.text2phenotype.ctakes.rest.api.pipeline.dictionary;

import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CodeDictionaryFlexTests {
	@Test
    public void icd9_test() throws Exception {
		String query = "006.2";

		final AnalysisEngine engine = PipelineFactory
				.create()
				.SegmentAnnotator()
				.SentenceDetector()
				.Tokenizer()
				.FDL("com/text2phenotype/ctakes/resources/dictionaries/icd9_code.xml")
				.build();

		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(query);
		engine.process(jcas);
		List<IdentifiedAnnotation> annotations = new ArrayList<>(JCasUtil.select(jcas, IdentifiedAnnotation.class));
		assertEquals(1, annotations.size());
		assertEquals("006.2", annotations.get(0).getCoveredText());
    }
	
	@Test
    public void test_icdo_topography() throws Exception {
		String query = "Topo. code is (C00.6)";

		final AnalysisEngine engine = PipelineFactory
				.create()
				.SegmentAnnotator()
				.SentenceDetector()
				.Tokenizer()
				.FDL("com/text2phenotype/ctakes/resources/dictionaries/cancer-topology-code.xml")
				.build();

		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(query);
		engine.process(jcas);
		List<IdentifiedAnnotation> annotations = new ArrayList<>(JCasUtil.select(jcas, IdentifiedAnnotation.class));
		assertEquals(1, annotations.size());
		assertEquals("C00.6", annotations.get(0).getCoveredText());
    }
	
	@Test
    public void test_icdo_morphology() throws Exception {
		final String query = "Morph. code is 8022/3.";

		final AnalysisEngine engine = PipelineFactory
				.create()
				.SegmentAnnotator()
				.SentenceDetector()
				.Tokenizer()
				.FDL("com/text2phenotype/ctakes/resources/dictionaries/cancer-morphology-code.xml")
				.build();

		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(query);
		engine.process(jcas);
		List<IdentifiedAnnotation> annotations = new ArrayList<>(JCasUtil.select(jcas, IdentifiedAnnotation.class));
		assertEquals(1, annotations.size());
		assertEquals("8022/3", annotations.get(0).getCoveredText());
    }
}
