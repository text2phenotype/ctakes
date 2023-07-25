package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.TimeAnnotator24h;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.ExtendedConcept;
import com.text2phenotype.ctakes.rest.api.pipeline.consumers.AnnotationClassConsumer;
import com.text2phenotype.ctakes.rest.api.pipeline.dictionary.BsvRareWordDictionaryWithSAB;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.commons.lang.IllegalClassException;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.ArrayListMap;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.textspan.DefaultTextSpan;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@RunWith(Parameterized.class)
public class BIOMED_702_tests {

    @Parameterized.Parameter()
    public String className;

    @Parameterized.Parameter(1)
    public boolean isValid;

    @Parameterized.Parameters(name = "{index}: Test with {0}")
    public static Iterable<Object[]> samples() {
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[] {LabMention.class.getName(), true});
        result.add(new Object[] {ProcedureMention.class.getName(), true});
        result.add(new Object[] {MedicationMention.class.getName(), true});
        result.add(new Object[] {DiseaseDisorderMention.class.getName(), true});

        result.add(new Object[] {BaseToken.class.getName(), false});
        result.add(new Object[] {NumToken.class.getName(), false});
        result.add(new Object[] {UnitToken.class.getName(), false});

        return result;
    }

    private static JCas JCAS;
    private static BsvRareWordDictionaryWithSAB DICT;

    private static CollectionMap<TextSpan, Long, ? extends Collection<Long>> TEXT_SPAN_CUIS;
    private static CollectionMap<Long, Concept, ? extends Collection<Concept>> CUI_CONCEPTS;

    @BeforeClass
    public static void prebuild() throws Exception {
        JCAS = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        DICT = new BsvRareWordDictionaryWithSAB("dict", "BIOMED_702/dict.bsv", null);

        TEXT_SPAN_CUIS = new ArrayListMap<>();
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(13, 17), 1L);
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(19, 26), 2L);
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(28, 35), 3L);
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(37, 46), 4L);
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(48, 63), 5L);
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(65, 68), 6L);
        TEXT_SPAN_CUIS.placeValue(new DefaultTextSpan(70, 78), 7L);

        CUI_CONCEPTS = new ArrayListMap<>();

        CollectionMap<String, String, ? extends Collection<String>> codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T109");
        CUI_CONCEPTS.placeValue(1L, new ExtendedConcept("C000001", "Any drugs", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T119");
        CUI_CONCEPTS.placeValue(2L, new ExtendedConcept("C000002", "Any Disease", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T033");
        CUI_CONCEPTS.placeValue(3L, new ExtendedConcept("C000003", "Any Finding", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T060");
        CUI_CONCEPTS.placeValue(4L, new ExtendedConcept("C000004", "Any Procedure", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T021");
        CUI_CONCEPTS.placeValue(5L, new ExtendedConcept("C000005", "Any Anatomical site", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T034");
        CUI_CONCEPTS.placeValue(6L, new ExtendedConcept("C000006", "Any Lab", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T058");
        CUI_CONCEPTS.placeValue(7L, new ExtendedConcept("C000007", "Any Activity", codes));
    }

    @Test
    public void annotation_class_test() throws Exception {
        Properties props = new Properties();
        props.setProperty("annotationClass", className);
        props.setProperty("codingScheme", "Test");

        JCAS.reset();
        JCasSerializer.Load(JCAS, FileLocator.getFullPath("BIOMED_702/sample.xmi"));

        try {
            AnnotationClassConsumer consumer = new AnnotationClassConsumer(null, props);

            consumer.consumeHits(JCAS, DICT, TEXT_SPAN_CUIS, CUI_CONCEPTS);
            Collection<IdentifiedAnnotation> ann = JCasUtil.select(JCAS, IdentifiedAnnotation.class);
            Assert.assertTrue("Not all annotations have class: " + className, ann.stream().allMatch(a -> a.getClass().getName().equals(className)));

        } catch (IllegalClassException e) {
            Assert.assertFalse(isValid);
        }
    }
}
