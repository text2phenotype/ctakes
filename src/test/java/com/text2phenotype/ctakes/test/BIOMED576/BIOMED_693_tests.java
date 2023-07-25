package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.LabValuesAnnotatorSequence;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.LabValuesDetectionEnhancer;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class BIOMED_693_tests {

    private final static String TEXT = "SODIUM 140mmol/L";
    private final static String JCAS_DUMP_PATH = "BIOMED_693/sample.xmi";

    private AnalysisEngine init() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .add(
                        AnalysisEngineFactory.createEngineDescription(
                                LabValuesDetectionEnhancer.class
                        )
                )
                .build();
    }

    @Test
    public void merged_value_units_test() {
        try {
            AnalysisEngine ae = init();
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_DUMP_PATH));
            ae.process(jcas);
            boolean hasNumber = JCasUtil
                    .select(jcas, NumToken.class)
                    .stream()
                    .anyMatch(num -> num.getBegin() == 7 && num.getEnd() == 10);
            Assert.assertTrue("Value is not found", hasNumber);

            boolean hasToken = JCasUtil
                    .select(jcas, WordToken.class)
                    .stream()
                    .anyMatch(word -> word.getBegin() == 10 && word.getEnd() == 14);
            Assert.assertTrue("First token of unit is not found", hasToken);

        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
}
