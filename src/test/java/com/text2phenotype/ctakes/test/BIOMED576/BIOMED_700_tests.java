package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.TokenAdjuster;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class BIOMED_700_tests {
    private final String SAMPLE_PATH = "BIOMED_700/sample.txt";
    private final String XMI_PATH = "BIOMED_700/sample.xmi";

    private AnalysisEngine buildPipeline() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .add(
                    AnalysisEngineFactory.createEngineDescription(
                        TokenAdjuster.class
                    )
                )
                .build();
    }

    @Test
    public void unit_token_test() {
        try {
            AnalysisEngine ae = buildPipeline();
            JCas jCas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jCas, FileLocator.getFullPath(XMI_PATH));
            ae.process(jCas);

            Collection<UnitToken> units = JCasUtil.select(jCas, UnitToken.class);
            Assert.assertEquals("Wrong amount of tokens", 505, units.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
