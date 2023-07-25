package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.LabValuesDetectionEnhancer;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BIOMED_694_tests {
    private final static String TEXT_PATH = "BIOMED_694/sample.txt";
    private final static String JCAS_DUMP_PATH = "BIOMED_694/sample.xmi";

    private AnalysisEngine init_fast() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .LVG()
                .build();
    }

    private AnalysisEngine init_default() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .add(
                        AnalysisEngineFactory.createEngineDescription(
                                LvgAnnotator.class
                        )
                )
                .build();
    }

    @Test
    public void compare_with_default_lvg_annotator_test() {
        try {
            AnalysisEngine ae_fast = init_fast();
            final JCas jcas_fast = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas_fast, FileLocator.getFullPath(JCAS_DUMP_PATH));
            ae_fast.process(jcas_fast);

            AnalysisEngine ae_default = init_fast();
            final JCas jcas_default = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas_default, FileLocator.getFullPath(JCAS_DUMP_PATH));
            ae_default.process(jcas_default);

            List<WordToken> tokens_fast = new ArrayList<>(JCasUtil.select(jcas_fast, WordToken.class));
            List<WordToken> tokens_default = new ArrayList<>(JCasUtil.select(jcas_default, WordToken.class));

            for (int i=0; i < tokens_default.size(); i++) {
                WordToken token_default = tokens_default.get(i);
                WordToken token_fast = tokens_fast.get(i);
                Assert.assertEquals("Tokens have different canonical form", token_default.getCanonicalForm(), token_fast.getCanonicalForm());
                Assert.assertEquals("Tokens have different normalized form", token_default.getNormalizedForm(), token_fast.getNormalizedForm());
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
