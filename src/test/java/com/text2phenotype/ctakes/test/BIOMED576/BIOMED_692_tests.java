package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.LabValuesAnnotatorSequence;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.refsem.Lab;
import org.apache.ctakes.typesystem.type.refsem.LabValue;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class BIOMED_692_tests {
    private final static String TEXT_PATH = "BIOMED_692/sample.txt";
    private final static String JCAS_DUMP_PATH = "BIOMED_692/sample.xmi";
    private final static String JCAS_SPECWORD_DUMP_PATH = "BIOMED_692/special_words_sample.xmi";

    private Map<Integer, LabValueData> getEthalonData() {
        final Map<Integer, LabValueData> ethalonData = new HashMap<>();
        ethalonData.put(0, new LabValueData("7.4", null));
        ethalonData.put(25, new LabValueData("12.9", null));
        ethalonData.put(42, new LabValueData("39", null));
        ethalonData.put(57, new LabValueData("313,000", null));
        ethalonData.put(84, new LabValueData("normal", null));
        ethalonData.put(116, new LabValueData("51", "%"));
        ethalonData.put(133, new LabValueData("37", "%"));
        ethalonData.put(149, new LabValueData("9", "%"));
        ethalonData.put(166, new LabValueData("3", "%"));
        ethalonData.put(270, new LabValueData("17", null));
        ethalonData.put(284, new LabValueData("0.5", null));
        ethalonData.put(302, new LabValueData("30", "ng/ml"));
        ethalonData.put(342, new LabValueData("13", null));
        ethalonData.put(350, new LabValueData("13", null)); // wrong prediction of ML
        return ethalonData;
    }

    private AnalysisEngine init(String specWord) throws ResourceInitializationException {
                return PipelineFactory
                    .create()
                    .add(
                            AnalysisEngineFactory.createEngineDescription(
                                    LabValuesAnnotatorSequence.class,
                                    "classifierJarPath",
                                    "/com/text2phenotype/ctakes/resources/lab_values/model.jar",
                                    "labValueWords",
                                    specWord
                            )
                    )
                    .build();
    }

    @Test
    public void lab_values_extraction_test() {

        try {
            AnalysisEngine ae = init("normal");
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_DUMP_PATH));
            ae.process(jcas);
            Collection<LabMention> mentions = JCasUtil.select(jcas, LabMention.class);
            Assert.assertEquals(15, mentions.size());

            Map<Integer, LabValueData> ethalonData = this.getEthalonData();
            for (LabMention mention: mentions) {
                if (mention.getEvent() != null) {
                    Assert.assertTrue("There is not ethalon data for the Lab mention", ethalonData.containsKey(mention.getBegin()));
                    LabValueData data = ethalonData.get(mention.getBegin());
                    LabValue lv = ((Lab)mention.getEvent()).getLabValue();
                    Assert.assertEquals(lv.getNumber(), data.value);
                    Assert.assertEquals(lv.getUnit(), data.unit);
                }

            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void special_words_test() {
        try {
            AnalysisEngine without_special_words_ae = init(null);
            AnalysisEngine with_special_words_ae = init("normal");
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_SPECWORD_DUMP_PATH));
            without_special_words_ae.process(jcas);
            List<LabMention> mentions = new ArrayList<>(JCasUtil.select(jcas, LabMention.class));
            Assert.assertEquals(1, mentions.size());
            Assert.assertNull("Speciall word should not be detected", mentions.get(0).getEvent());

            with_special_words_ae.process(jcas);
            mentions = new ArrayList<>(JCasUtil.select(jcas, LabMention.class));
            Assert.assertEquals(1, mentions.size());
            Assert.assertNotNull("Special word 'normal' is not extracted as a value", mentions.get(0).getEvent());
            Assert.assertEquals("normal", ((Lab)mentions.get(0).getEvent()).getLabValue().getNumber());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private class LabValueData {
        public String value;
        public String unit;

        public LabValueData(String v, String u) {
            value = v;
            unit = u;
        }
    }
}
