package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.DrugMentionAnnotatorWithPositions;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BIOMED_691_tests {
    private final static String TEXT = "Sample: amidone 20 mg twice a day oral 3 caps for two days";
    private final static String JCAS_DUMP_PATH = "BIOMED_691/sample.xmi";


    public AnalysisEngine init() throws ResourceInitializationException {
        return PipelineFactory
                .create()
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
                                "maxAttributeDistance", 15)
                )
                .build();
    }

    @Test
    public void attr_positions_test() {
        try {
            final JCas jcas = JCasFactory.createJCas();
            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_DUMP_PATH));
            final AnalysisEngine ae = init();
            ae.process(jcas);

            List<MedicationMention> mentions = new ArrayList<>(JCasUtil.select(jcas, MedicationMention.class));
            Assert.assertEquals("Incorrect count of mentions", 1, mentions.size());
            MedicationMention mention = mentions.get(0);

            // freq
            Assert.assertEquals(22, mention.getMedicationFrequency().getBegin());
            Assert.assertEquals(33, mention.getMedicationFrequency().getEnd());

            // strength
            Assert.assertEquals(16, mention.getMedicationStrength().getBegin());
            Assert.assertEquals(21, mention.getMedicationStrength().getEnd());

            // route
            Assert.assertEquals(34, mention.getMedicationRoute().getBegin());
            Assert.assertEquals(38, mention.getMedicationRoute().getEnd());

            // dosage
            Assert.assertEquals(39, mention.getMedicationDosage().getBegin());
            Assert.assertEquals(40, mention.getMedicationDosage().getEnd());

            // form
            Assert.assertEquals(41, mention.getMedicationForm().getBegin());
            Assert.assertEquals(45, mention.getMedicationForm().getEnd());

            // duration
            Assert.assertEquals(46, mention.getMedicationDuration().getBegin());
            Assert.assertEquals(58, mention.getMedicationDuration().getEnd());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }
}
