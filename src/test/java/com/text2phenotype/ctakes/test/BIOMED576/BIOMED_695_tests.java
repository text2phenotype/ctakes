package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.ActivityMention;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BIOMED_695_tests {

    private final static String TEXT_PATH = "BIOMED_695/sample.txt";
    private final static String JCAS_DUMP_PATH = "BIOMED_695/sample.xmi";
    private final static String FDL_PATH = "BIOMED_695/dict.xml";

    private Map<Class<? extends IdentifiedAnnotation>, testItemData> getEthalonData() {
        final Map<Class<? extends IdentifiedAnnotation>, testItemData> ethalonData = new HashMap<>();
        ethalonData.put(MedicationMention.class, new testItemData(13, 17, "Any drugs", "C000001", "T109", "CUSTOM", "1"));
        ethalonData.put(DiseaseDisorderMention.class, new testItemData(19, 26, "Any Disease", "C000002", "T019", "CUSTOM", "2"));
        ethalonData.put(SignSymptomMention.class, new testItemData(28, 35, "Any Finding", "C000003", "T033", "CUSTOM", "3"));
        ethalonData.put(ProcedureMention.class, new testItemData(37, 46, "Any Procedure", "C000004", "T060", "CUSTOM", "4"));
        ethalonData.put(AnatomicalSiteMention.class, new testItemData(48, 63, "Any Anatomical site", "C000005", "T021", "CUSTOM", "5"));
        ethalonData.put(LabMention.class, new testItemData(65, 68, "Any Lab", "C000006", "T034", "CUSTOM", "6"));
        ethalonData.put(ActivityMention.class, new testItemData(70, 78, "Any Activity", "C000007", "T058", "CUSTOM", "7"));
        return ethalonData;
    }
    private AnalysisEngine init() throws ResourceInitializationException {
        return PipelineFactory
                .create()
                .FDL(FDL_PATH)
                .build();
    }

    @Test
    public void annotations_test() {
        try {
            AnalysisEngine ae_fast = init();
            final JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());

            JCasSerializer.Load(jcas, FileLocator.getFullPath(JCAS_DUMP_PATH));
            ae_fast.process(jcas);

            Map<Class<? extends IdentifiedAnnotation>, testItemData> ethalonData = getEthalonData();
            Collection<IdentifiedAnnotation> annotations = JCasUtil.select(jcas, IdentifiedAnnotation.class);
            Assert.assertEquals("Count of annotations is wrong", 7, annotations.size());
            for (IdentifiedAnnotation annotation : annotations) {

                if (!ethalonData.containsKey(annotation.getClass())){
                    Assert.fail("The annotation is absent in the test set: " + annotation.getCoveredText());
                }

                testItemData ethalonDataItem = ethalonData.get(annotation.getClass());

                Assert.assertEquals("Text range is wrong: " + annotation.getCoveredText(), ethalonDataItem.begin, annotation.getBegin());
                Assert.assertEquals("Text range is wrong: " + annotation.getCoveredText(), ethalonDataItem.end, annotation.getEnd());

                FSArray concepts = annotation.getOntologyConceptArr();
                Assert.assertEquals("Count of ontology concepts is wrong", 1, concepts.size());
                UmlsConcept umls = (UmlsConcept)concepts.get(0);
                Assert.assertTrue("Bad concept: " + umls.getPreferredText(), ethalonDataItem.checkConcept(umls));
            }
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    private class testItemData {
        public int begin;
        public int end;
        public String prefText;
        public String cui;
        public String tui;
        public String codingScheme;
        public String code;

        public testItemData(int begin, int end, String prefText, String cui, String tui, String codingScheme, String code) {
            this.begin = begin;
            this.end = end;
            this.prefText = prefText;
            this.cui = cui;
            this.tui = tui;
            this.codingScheme = codingScheme;
            this.code = code;
        }
        public boolean checkConcept(UmlsConcept concept) {
            boolean result = concept.getPreferredText().equals(prefText);
            result &= concept.getCui().equals(cui);
            result &= concept.getTui().equals(tui);
            result &= concept.getCodingScheme().equals(codingScheme);
            result &= concept.getCode().equals(code);
            return result;
        }

    }
}
