package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.TimeAnnotator24h;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.textsem.TimeAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class BIOMED_698_tests {

    private static AnalysisEngine AE;
    private static JCas JCAS;

    @BeforeClass
    public static void buildPipeline() throws Exception {
        JCAS = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        AE = PipelineFactory
                .create()
                .SegmentAnnotator()
                .SentenceDetector()
                .Tokenizer()
                .add(
                        AnalysisEngineFactory.createEngineDescription(
                                TimeAnnotator24h.class
                        )
                )
                .build();
    }

    @Parameterized.Parameter(0)
    public String txt;

    @Parameterized.Parameter(1)
    public boolean isValid;

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Iterable<Object[]> samples() {
        List<Object[]> result = new ArrayList<>();
        List<Character> separators = Arrays.asList(':', '/', '-');
        for (int sepI=0; sepI < separators.size(); sepI++) {
            Character sep = separators.get(sepI);
            for (int h = 0; h < 100; h+=1) {
                for (int m = 0; m < 100; m+=1) {
                    boolean isValid = (sep==':') & (h < 24) & (m < 60);
                    String txt = String.format("%02d%c%02d", h, sep, m);
                    Object[] batch = {txt, isValid};
                    result.add(batch);
                }
            }
        }
        Object[] batch = new Object[]{"Correct time in the middle: 00:00 end", true};
        result.add(batch);
        batch = new Object[]{"Correct time in the end: 00:00", true};
        result.add(batch);
        batch = new Object[]{"00:00 - correct time in the beginning ", true};
        result.add(batch);
        batch = new Object[]{"Incorrect time in the middle: 25:00 end", false};
        result.add(batch);
        batch = new Object[]{"Incorrect time in the end: 00:60", false};
        result.add(batch);
        batch = new Object[]{"70:00 - incorrect time in the beginning ", false};
        result.add(batch);

        return result;
    }

    @Test
    public void time_test() {
        try {
            JCAS.reset();
            JCAS.setDocumentText(this.txt);
            AE.process(JCAS);

            int expectedSize = this.isValid ? 1 : 0;
            Assert.assertEquals("Wrong amount of time annotations: " + this.txt, expectedSize, JCasUtil.select(JCAS, TimeAnnotation.class).size());

        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
}
