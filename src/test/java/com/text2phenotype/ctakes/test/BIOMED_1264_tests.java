package com.text2phenotype.ctakes.test;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.DrugMentionAnnotatorWithPositions;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class BIOMED_1264_tests {
    private final String TEXT = "Aspirin a couple words 20 mg twice a day";

    private AnalysisEngine init(int distance) throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .SegmentAnnotator()
                .SentenceDetector()
                .Tokenizer()
                .POSTagger()
                .FDL("BIOMED_1264/dict.xml")
                .add(
                    AnalysisEngineFactory.createEngineDescription(
                            DrugMentionAnnotatorWithPositions.class,
                            "medicationRelatedSection",
                            new String[]{ "20101", "20102", "20103", "20104", "20105", "20106", "20107", "20108", "20109", "20110", "20111", "20112", "20113", "20114", "20115", "20116", "20117", "20118", "20119", "20120", "20121", "20122", "20123", "20124", "20125", "20126", "20127", "20128", "20129", "20130", "20110", "20133", "20147", "SIMPLE_SEGMENT"},
                            "DISTANCE",
                            "1",
                            "DISTANCE_ANN_TYPE",
                            "org.apache.ctakes.typesystem.type.textspan.Sentence",
                            "STATUS_BOUNDARY_ANN_TYPE",
                            "org.apache.ctakes.typesystem.type.textspan.Sentence",
                            "maxAttributeDistance", distance)
                )
                .build();
    }

    @Test
    public void distance10_test() {
        try {

            final JCas jcas = JCasFactory.createJCas();
            jcas.setDocumentText(TEXT);
            AnalysisEngine ae = init(10);
            ae.process(jcas);

            MedicationMention medication = new ArrayList<>(JCasUtil.select(jcas, MedicationMention.class)).get(0);

            Assert.assertEquals("20 mg", medication.getMedicationStrength().getCoveredText());
            Assert.assertEquals("twice a day", medication.getMedicationFrequency().getCoveredText());
            Assert.assertNull(medication.getMedicationDosage());
            Assert.assertNull(medication.getMedicationRoute());
            Assert.assertNull(medication.getMedicationDuration());
            Assert.assertNull(medication.getMedicationForm());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void distance5_test() {
        try {

            final JCas jcas = JCasFactory.createJCas();
            jcas.setDocumentText(TEXT);
            AnalysisEngine ae = init(5);
            ae.process(jcas);

            MedicationMention medication = new ArrayList<>(JCasUtil.select(jcas, MedicationMention.class)).get(0);

            Assert.assertEquals("20 mg", medication.getMedicationStrength().getCoveredText());
            Assert.assertNull(medication.getMedicationFrequency());
            Assert.assertNull(medication.getMedicationDosage());
            Assert.assertNull(medication.getMedicationRoute());
            Assert.assertNull(medication.getMedicationDuration());
            Assert.assertNull(medication.getMedicationForm());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void distance2_test() {
        try {

            final JCas jcas = JCasFactory.createJCas();
            jcas.setDocumentText(TEXT);
            AnalysisEngine ae = init(2);
            ae.process(jcas);

            MedicationMention medication = new ArrayList<>(JCasUtil.select(jcas, MedicationMention.class)).get(0);

            Assert.assertNull(medication.getMedicationStrength());
            Assert.assertNull(medication.getMedicationFrequency());
            Assert.assertNull(medication.getMedicationDosage());
            Assert.assertNull(medication.getMedicationRoute());
            Assert.assertNull(medication.getMedicationDuration());
            Assert.assertNull(medication.getMedicationForm());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
