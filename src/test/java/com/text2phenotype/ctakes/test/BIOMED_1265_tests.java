package com.text2phenotype.ctakes.test;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.LabValuesAnnotatorSequence;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.TokenAdjuster;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.typesystem.type.refsem.Lab;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class BIOMED_1265_tests {
    private final String TEXT = "sodium a couple words 20 mg other words";

    private AnalysisEngine init(int distance) throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .SegmentAnnotator()
                .SentenceDetector()
                .Tokenizer()
                .add(AnalysisEngineFactory.createEngineDescription(TokenAdjuster.class))
                .POSTagger()
                .FDL("BIOMED_1265/dict.xml")
                .add(
                    AnalysisEngineFactory.createEngineDescription(
                            LabValuesAnnotatorSequence.class,
                            "classifierJarPath",
                            "/com/text2phenotype/ctakes/resources/lab_values/model.jar",
                            "labValueWords",
                            "normal",
                            "maxTokenDistance", distance)
                )
                .build();
    }

    @Test
    public void distance10_test() {
        try {

            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            jcas.setDocumentText(TEXT);
            AnalysisEngine ae = init(10);
            ae.process(jcas);

            LabMention lab = new ArrayList<>(JCasUtil.select(jcas, LabMention.class)).get(0);

            Lab labEventData = (Lab)lab.getEvent();
            String value = labEventData.getLabValue().getNumber();
            String unit = labEventData.getLabValue().getUnit();
            String term = lab.getCoveredText();

            Assert.assertEquals("sodium", term);
            Assert.assertEquals("20", value);
            Assert.assertEquals("mg", unit);


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void distance2_test() {
        try {

            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            jcas.setDocumentText(TEXT);
            AnalysisEngine ae = init(2);
            ae.process(jcas);

            LabMention lab = new ArrayList<>(JCasUtil.select(jcas, LabMention.class)).get(0);

            Assert.assertEquals("sodium", lab.getCoveredText());
            Assert.assertNull(lab.getEvent());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
