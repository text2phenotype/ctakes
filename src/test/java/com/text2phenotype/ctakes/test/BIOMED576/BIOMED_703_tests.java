package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.ActivityMention;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.ExtendedConcept;
import com.text2phenotype.ctakes.rest.api.pipeline.consumers.FilteredDefaultTermConsumer;
import com.text2phenotype.ctakes.rest.api.pipeline.dictionary.BsvRareWordDictionaryWithSAB;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.ArrayListMap;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.textspan.DefaultTextSpan;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@RunWith(Parameterized.class)
public class BIOMED_703_tests {

    @Parameterized.Parameter()
    public String tui;

    @Parameterized.Parameter(1)
    public String className;

    @Parameterized.Parameters(name = "{index}: Test with {0}")
    public static Iterable<Object[]> samples() {
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[] {"T109", MedicationMention.class.getName()});
        result.add(new Object[] {"T019", DiseaseDisorderMention.class.getName()});
        result.add(new Object[] {"T033", SignSymptomMention.class.getName()});
        result.add(new Object[] {"T060", ProcedureMention.class.getName()});
        result.add(new Object[] {"T021", AnatomicalSiteMention.class.getName()});
        result.add(new Object[] {"T034", LabMention.class.getName()});
        result.add(new Object[] {"T058", ActivityMention.class.getName()});

        return result;
    }

    private static JCas JCAS;
    private static BsvRareWordDictionaryWithSAB DICT;

    private static CollectionMap<TextSpan, Long, ? extends Collection<Long>> TEXT_SPAN_CUIS;

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


    }
    
    private CollectionMap<Long, Concept, ? extends Collection<Concept>> createCuiConceptMap() {
        CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConceptMap = new ArrayListMap<>();

        CollectionMap<String, String, ? extends Collection<String>> codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T109");
        cuiConceptMap.placeValue(1L, new ExtendedConcept("C000001", "Any drugs", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T019");
        cuiConceptMap.placeValue(2L, new ExtendedConcept("C000002", "Any Disease", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T033");
        cuiConceptMap.placeValue(3L, new ExtendedConcept("C000003", "Any Finding", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T060");
        cuiConceptMap.placeValue(4L, new ExtendedConcept("C000004", "Any Procedure", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T021");
        cuiConceptMap.placeValue(5L, new ExtendedConcept("C000005", "Any Anatomical site", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T034");
        cuiConceptMap.placeValue(6L, new ExtendedConcept("C000006", "Any Lab", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TUI", "T058");
        cuiConceptMap.placeValue(7L, new ExtendedConcept("C000007", "Any Activity", codes));

        return cuiConceptMap;
        
    }

    @Test
    public void annotation_class_test() throws Exception {
        Properties props = new Properties();
        props.setProperty("targetSemantics", tui);
        props.setProperty("codingScheme", "Test");

        JCAS.reset();
        JCasSerializer.Load(JCAS, FileLocator.getFullPath("BIOMED_702/sample.xmi"));

        FilteredDefaultTermConsumer consumer = new FilteredDefaultTermConsumer(null, props);

        CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConceptMap = createCuiConceptMap();
        consumer.consumeHits(JCAS, DICT, TEXT_SPAN_CUIS, cuiConceptMap);
        Collection<IdentifiedAnnotation> ann = JCasUtil.select(JCAS, IdentifiedAnnotation.class);
        Assert.assertEquals("Wrong amount of annotations", 1, ann.size());
        Assert.assertEquals("Wrong annotation semantic: " + tui, ann.stream().findFirst().get().getClass().getName(), className);

    }
}
