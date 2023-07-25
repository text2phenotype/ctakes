package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ctakes.core.ae.SentenceDetectorAnnotatorBIO;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.text2phenotype.ctakes.test.utils.PipelineFactory;

public class FastSentenceDetectorAnnotatorBIOTests {
	private static String txt;
    @BeforeClass
    public static void loadSample() {
        try {
            String samplePath = FileLocator.getFullPath("BIOMED_668/correctness_test_sample.txt");
            txt = new String(Files.readAllBytes(Paths.get(samplePath)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
	/**
     * Compares results of improved and default Sentence detectors.
     */
    @Test
    public void test_Text2phenotypeSentenceDetectorVsNative() throws UIMAException {
        AggregateBuilder builder_default = new AggregateBuilder();

        JCas jcas_default = JCasFactory.createJCas();
        jcas_default.setDocumentText(txt);

        JCas jcas_improved = JCasFactory.createJCas();
        jcas_improved.setDocumentText(txt);

        // create a pipeline with default sentence detector
        builder_default.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
        builder_default.add(AnalysisEngineFactory.createEngineDescription(
                SentenceDetectorAnnotatorBIO.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/core/sentdetect/model.jar",
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                "CHAR"));
        builder_default.createAggregate().process(jcas_default);

        // create a pipeline with improved sentence detector
        AnalysisEngine builder_improved = initPipeline();
        builder_improved.process(jcas_improved);

        List<Sentence> sent_default = new ArrayList<>(JCasUtil.select(jcas_default, Sentence.class));
        List<Sentence> sent_improved = new ArrayList<>(JCasUtil.select(jcas_improved, Sentence.class));

        assertTrue("Sentences are not found", sent_default.size() > 0);

        // check sizes
        assertEquals(sent_default.size(), sent_improved.size());

        // compare sentences ranges
        for (int i=0; i <sent_default.size(); i++) {
            Sentence sent_d = sent_default.get(i);
            Sentence sent_i = sent_improved.get(i);

            assertTrue("Ranges are different", (sent_d.getBegin() == sent_i.getBegin()) && (sent_d.getEnd() == sent_i.getEnd()));
        }
    }
    
    @Test
    public void test_colonInVitals() throws UIMAException {
    	final String text = "Aspirin\n" + 
    			"Tylenol\n" + 
    			"Ibuprofin\n" + 
    			"\n" + 
    			": 100 % (Charted at 02/04/2020 13:15 CDT by Some doc,RN)\n" + 
    			": 100 % (Charted at 02/04/2020 13:45 CDT by Some doc,RN)\n" +
    			": 100 % (Charted at 02/04/2020 14:15 CDT by Some doc,RN)\n" +
    			": 100 % (Charted at 02/04/2020 14:45 CDT by Some doc,RN)\n" +
    			": 100 % (Charted at 02/04/2020 15:15 CDT by Some doc,RN)\n" +
    			"\n" +
    			"Patient has history of...";
    	JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText(text);
        
    	AnalysisEngine engine = initPipeline();
    	engine.process(jcas);
    	
    	final List<int[]> expected = Arrays.asList(
    			new int[] {0, 25}, 
    			new int[] {27, 83}, 
    			new int[] {84, 140}, 
    			new int[] {141, 197}, 
    			new int[] {198, 254}, 
    			new int[] {255, 311}, 
    			new int[] {313, 338});
    	
    	List<Sentence> sentences = new ArrayList<>(JCasUtil.select(jcas, Sentence.class));
    	assertEquals(expected.size(), sentences.size());
    	
    	for (int i = 0; i < expected.size(); i++) {
    		assertEquals(expected.get(i)[0], sentences.get(i).getBegin());
    		assertEquals(expected.get(i)[1], sentences.get(i).getEnd());
    	}
    }
    
    private static AnalysisEngine initPipeline() throws ResourceInitializationException {
    	return PipelineFactory.create().SegmentAnnotator().SentenceDetector().build();
    }
}
