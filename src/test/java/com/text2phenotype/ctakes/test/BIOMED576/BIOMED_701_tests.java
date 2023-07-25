package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.concept.BsvConceptFactoryWithSAB;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.ExtendedConcept;
import org.apache.ctakes.core.util.collection.ArrayListMap;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class BIOMED_701_tests {
    private final String DICT_PATH = "BIOMED_701/dict.bsv";

    private Map<Long, ExtendedConcept> createEthalonData() {
        Map<Long, ExtendedConcept> result = new HashMap<>();

        CollectionMap<String, String, ? extends Collection<String>> codes = new ArrayListMap<>();
        codes.placeValue("TEST_VOCAB1", "1");
        codes.placeValue("TUI", "T059");
        result.put(1L, new ExtendedConcept("C0000001", "NUMBER ONE", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TEST_VOCAB2", "2");
        codes.placeValue("TUI", "T053");
        result.put(2L, new ExtendedConcept("C0000002", "NUMBER TWO", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TEST_VOCAB3", "3");
        codes.placeValue("TUI", "T034");
        result.put(3L, new ExtendedConcept("C0000003", "NUMBER THREE", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TEST_VOCAB4", "4");
        codes.placeValue("TUI", "T047");
        result.put(4L, new ExtendedConcept("C0000004", "NUMBER FOUR", codes));

        codes = new ArrayListMap<>();
        codes.placeValue("TEST_VOCAB5", "5");
        codes.placeValue("TUI", "T048");
        result.put(5L, new ExtendedConcept("C0000005", "NUMBER FIVE", codes));

        return result;
    }

    @Test(expected = AssertionError.class)
    public void bsv_concept_factory_create_concept_test() {
        BsvConceptFactoryWithSAB factory = new BsvConceptFactoryWithSAB("Test", DICT_PATH);
        factory.createConcept(0L);
    }

    @Test
    public void bsv_concept_factory_create_concepts_test() {
        BsvConceptFactoryWithSAB factory = new BsvConceptFactoryWithSAB("Test", DICT_PATH);

        List<Long> cuis = new ArrayList<>();
        for (long i=0; i<10; i++) {
            cuis.add(i);
        }

        Map<Long, Concept> conceptMap = factory.createConcepts(cuis);
        Map<Long, ExtendedConcept> ethalonConceptMap = createEthalonData();
        Assert.assertEquals("Wrong concepts amount", ethalonConceptMap.size(), conceptMap.size());

        for (long n : conceptMap.keySet()) {
            if (!ethalonConceptMap.containsKey(n)) {
                Assert.fail("Wrong concept: " + n);
            }

            ExtendedConcept concept = (ExtendedConcept)conceptMap.get(n);
            ExtendedConcept ethalonConcept = ethalonConceptMap.get(n);

            Assert.assertEquals("Wrong CUI: " + n, ethalonConcept.getCui(), concept.getCui());
            Assert.assertEquals("Wrong Pref Text: " + n, ethalonConcept.getPreferredText(), concept.getPreferredText());
            Assert.assertArrayEquals("Code names are wrong: " + n, ethalonConcept.getCodeNames().toArray(), concept.getCodeNames().toArray());
            Assert.assertArrayEquals("Semantics are wrong: " + n, ethalonConcept.getCtakesSemantics().toArray(), concept.getCtakesSemantics().toArray());


            for (String codeName : concept.getCodeNames()) {
                Collection<String> conceptCodes = concept.getCodes(codeName);
                Collection<String> ethalonCodes = ethalonConcept.getCodes(codeName);
                Assert.assertArrayEquals("Codes are wrong: " + n, ethalonCodes.toArray(), conceptCodes.toArray());
            }
        }
    }

    @Test
    public void bsv_concept_factory_create_concepts_collection_test() {
        BsvConceptFactoryWithSAB factory = new BsvConceptFactoryWithSAB("Test", DICT_PATH);

        List<Long> cuis = new ArrayList<>();
        for (long i=0; i<10; i++) {
            cuis.add(i);
        }

        CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap = factory.createConceptsCollection(cuis);

        Map<Long, ExtendedConcept> ethalonConceptMap = createEthalonData();
        Assert.assertEquals("Wrong concepts amount", ethalonConceptMap.size(), conceptMap.size());

        for (long n : conceptMap.keySet()) {
            if (!ethalonConceptMap.containsKey(n)) {
                Assert.fail("Wrong concept: " + n);
            }

            ExtendedConcept ethalonConcept = ethalonConceptMap.get(n);

            conceptMap.get(n).stream().forEach(concept -> {
                Assert.assertEquals("Wrong CUI: " + n, ethalonConcept.getCui(), concept.getCui());
                Assert.assertEquals("Wrong Pref Text: " + n, ethalonConcept.getPreferredText(), concept.getPreferredText());
                Assert.assertArrayEquals("Code names are wrong: " + n, ethalonConcept.getCodeNames().toArray(), concept.getCodeNames().toArray());
                Assert.assertArrayEquals("Semantics are wrong: " + n, ethalonConcept.getCtakesSemantics().toArray(), concept.getCtakesSemantics().toArray());


                for (String codeName : concept.getCodeNames()) {
                    Collection<String> conceptCodes = concept.getCodes(codeName);
                    Collection<String> ethalonCodes = ethalonConcept.getCodes(codeName);
                    Assert.assertArrayEquals("Codes are wrong: " + n, ethalonCodes.toArray(), conceptCodes.toArray());
                }
            });


        }
    }
}
