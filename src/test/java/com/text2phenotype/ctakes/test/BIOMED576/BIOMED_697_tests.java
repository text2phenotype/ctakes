package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.PatientDataExtractor;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class BIOMED_697_tests {

    @Parameterized.Parameter(0)
    public String dump_path;

    @Parameterized.Parameter(1)
    public String sample_txt;

    @Parameterized.Parameters(name = "Case {index}: {1}")
    public static Iterable<String[]> samples() {
        String[][] params = {
                {"BIOMED_697/dob.xmi", "Patient DOB: 12/02/2019"},
                {"BIOMED_697/birth.xmi", "Patient birth: 12/02/2019"}
        };
        return Arrays.asList(params);
    }

    private AnalysisEngine init() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .add(
                        AnalysisEngineFactory.createEngineDescription(
                                PatientDataExtractor.class
                        )
                )
                .build();
    }

    @Test
    public void patient_data_test() {
        try {
            AnalysisEngine ae = init();
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas, FileLocator.getFullPath(dump_path));
            ae.process(jcas);

            List<Demographics> demograph = new ArrayList<>(JCasUtil.select(jcas, Demographics.class));
            Assert.assertEquals("Demographics annotation is absent", 1, demograph.size());
            Demographics d = demograph.get(0);
            Assert.assertEquals("Birth date is wrong", "12/02/2019", d.getBirthDate());
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
}
