package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.MergeSimilarAnnotator;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

public class BIOMED_696_tests {

    private final static String TEXT1 = "Two annotation should be merged for text: Drug";
    private final static String TEXT2 = "Two annotation should not be merged for text: Proc";
    private final static String JCAS_DUMP1_PATH = "BIOMED_696/sample1.xmi";
    private final static String JCAS_DUMP2_PATH = "BIOMED_696/sample2.xmi";
    private final static String FDL_PATH = "BIOMED_696/dict.xml";

    private AnalysisEngine init() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .add(
                        AnalysisEngineFactory.createEngineDescription(
                                MergeSimilarAnnotator.class
                        )
                )
                .build();
    }

    @Test
    public void should_be_merged_test() {
        try {
            AnalysisEngine ae = init();
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_DUMP1_PATH));
            ae.process(jcas);
            Assert.assertEquals("There is should be only one identified annotation",1, JCasUtil.select(jcas, MedicationMention.class).size());
            Assert.assertEquals("The annotation should contain two concepts",2, JCasUtil.select(jcas, MedicationMention.class).stream().findFirst().get().getOntologyConceptArr().size());

        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void should_not_be_merged_test() {
        try {
            AnalysisEngine ae = init();
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_DUMP2_PATH));
            ae.process(jcas);
            Assert.assertEquals("There are should be two identified annotations",2, JCasUtil.select(jcas, IdentifiedAnnotation.class).size());
            Assert.assertTrue("Each annotation should contain only one concept",
                    JCasUtil.select(jcas, IdentifiedAnnotation.class)
                            .stream()
                            .allMatch(annotation -> annotation.getOntologyConceptArr().size() == 1)
            );
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
}
