package com;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.core.ae.SentenceDetectorAnnotatorBIO;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.junit.Test;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.FastSentenceDetectorAnnotatorBIO;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.TokenizerAnnotatorPTB;

public class BIOMED1016Tests {
	@Test
    public void testFailText1() throws UIMAException {
		testFailText("İNTİ\\nC.");
    }
	
	@Test
    public void testFailText2() throws UIMAException {
		testFailText("İİ\\nC.");
    }
	
	@Test
	public void testMissedLabAttributes() throws UIMAException {
		JCas jcas = processText("HCV viral load 210,000,000 IU/L");
        
        List<Class> expClasses = new ArrayList<Class>() {{
        	add(org.apache.ctakes.typesystem.type.syntax.WordToken.class);
        	add(org.apache.ctakes.typesystem.type.syntax.WordToken.class);
        	add(org.apache.ctakes.typesystem.type.syntax.WordToken.class);
        	add(org.apache.ctakes.typesystem.type.syntax.NumToken.class);
        	add(org.apache.ctakes.typesystem.type.syntax.WordToken.class);
        	add(org.apache.ctakes.typesystem.type.syntax.PunctuationToken.class);
        	add(org.apache.ctakes.typesystem.type.syntax.WordToken.class);
        }};
        for(Sentence sentAnnot : JCasUtil.select(jcas, Sentence.class)) {
        	List<org.apache.ctakes.typesystem.type.syntax.BaseToken> tokens = 
        			JCasUtil.selectCovered(org.apache.ctakes.typesystem.type.syntax.BaseToken.class, sentAnnot);
        	
        	int index = 0;
			for(org.apache.ctakes.typesystem.type.syntax.BaseToken bta : tokens) {
				LOGGER.debug(bta.getCoveredText() + ": " + bta.getClass());
				
				assertEquals(expClasses.get(index++), bta.getClass());
			}
		}
	}
	
	private static void testFailText(final String text) throws UIMAException {
		JCas jcas = processText(text);
        
        for(Sentence sentAnnot : JCasUtil.select(jcas, Sentence.class)) {
        	List<org.apache.ctakes.typesystem.type.syntax.BaseToken> tokens = 
        			JCasUtil.selectCovered(org.apache.ctakes.typesystem.type.syntax.BaseToken.class, sentAnnot);
        	
			for(org.apache.ctakes.typesystem.type.syntax.BaseToken bta : tokens) {
				LOGGER.debug(bta.getCoveredText() + ": " + bta.getClass());
				
				assertNotEquals(NumToken.class, bta.getClass());
			}
		}
	}
	
	private static JCas processText(final String text) throws UIMAException {
		AggregateBuilder builder = new AggregateBuilder();

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText(text);

        builder.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
        builder.add(AnalysisEngineFactory.createEngineDescription(
                FastSentenceDetectorAnnotatorBIO.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/core/sentdetect/model.jar",
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                "CHAR"));
        builder.add(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class));
        
        builder.createAggregate().process(jcas);
        
        return jcas;
	}
	
	private static final Logger LOGGER = Logger.getLogger(BIOMED1016Tests.class);
}
