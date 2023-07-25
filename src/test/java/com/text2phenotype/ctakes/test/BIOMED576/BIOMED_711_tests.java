package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.negation.ExtendedNegationContextAnalyzer;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.necontexts.ContextAnnotator;
import org.apache.ctakes.necontexts.ContextHit;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BIOMED_711_tests {

    // Alcohol: no
    @Test
    public void negative_polarity_test() throws Exception {
        JCas jCas = JCasFactory.createJCas();
        JCasSerializer.Load(jCas, FileLocator.getFullPath("BIOMED_711/neg_sample.xmi"));

        ExtendedNegationContextAnalyzer na = new ExtendedNegationContextAnalyzer();
        na.initialize(null);
        List<Annotation> annotations = new ArrayList<>(JCasUtil.select(jCas, Annotation.class));
        ContextHit hit = na.analyzeContext(annotations, ContextAnnotator.ALL_SCOPE);
        Assert.assertNotNull("Wrong negation", hit);
    }

    // Alcohol: yes
    @Test
    public void positive_polarity_test() throws Exception {
        JCas jCas = JCasFactory.createJCas();
        JCasSerializer.Load(jCas, FileLocator.getFullPath("BIOMED_711/pos_sample.xmi"));

        ExtendedNegationContextAnalyzer na = new ExtendedNegationContextAnalyzer();
        na.initialize(null);
        List<Annotation> annotations = new ArrayList<>(JCasUtil.select(jCas, Annotation.class));
        ContextHit hit = na.analyzeContext(annotations, ContextAnnotator.ALL_SCOPE);
        Assert.assertNull("Wrong negation", hit);
    }
}
