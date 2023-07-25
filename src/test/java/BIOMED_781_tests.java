import com.text2phenotype.ctakes.rest.api.pipeline.concept.BsvConceptFactoryFlex;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.ExtendedConcept;
import com.text2phenotype.ctakes.rest.api.pipeline.dictionary.BsvRareWordDictionaryFlex;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class BIOMED_781_tests {

    private final String DICT_PATH = "BIOMED_781/dict.bsv";
    private final String DICT2_PATH = "BIOMED_781/dict2.bsv";

    @Test
    public void flex_dictionary_test() {
        Properties props = new Properties();
        props.setProperty("bsvPath", DICT_PATH);
        props.setProperty("CUI", "0");
        props.setProperty("TERM", "5");

        BsvRareWordDictionaryFlex dict = new BsvRareWordDictionaryFlex("Test", null, props);
        Collection<RareWordTerm> rareWordTerms = dict.getRareWordHits("asthma");
        Assert.assertEquals("Rare word terms are not found", 1, rareWordTerms.size());

        List<RareWordTerm> termsList = new ArrayList<>(rareWordTerms);

        Assert.assertEquals("CUI is wrong", 4096L, (long)termsList.get(0).getCuiCode());

        rareWordTerms = dict.getRareWordHits("asthma2");
        Assert.assertEquals("Rare word terms are found", 0, rareWordTerms.size());

    }

    @Test
    public void flex_dictionary_not_typical_order_test() {
        Properties props = new Properties();
        props.setProperty("bsvPath", DICT2_PATH);
        props.setProperty("CUI", "0");
        props.setProperty("TERM", "1");

        BsvRareWordDictionaryFlex dict = new BsvRareWordDictionaryFlex("Test", null, props);
        Collection<RareWordTerm> rareWordTerms = dict.getRareWordHits("asthma");
        Assert.assertEquals("Rare word terms are not found", 1, rareWordTerms.size());

        List<RareWordTerm> termsList = new ArrayList<>(rareWordTerms);

        Assert.assertEquals("CUI is wrong", 4096L, (long)termsList.get(0).getCuiCode());

        rareWordTerms = dict.getRareWordHits("asthma2");
        Assert.assertEquals("Rare word terms are found", 0, rareWordTerms.size());

    }

    @Test
    public void flex_factory_test() {
        Properties props = new Properties();
        props.setProperty("bsvPath", DICT_PATH);
        props.setProperty("scheme", "CUI|TUI|TTY|CODE|SAB|STR|PREF");

        BsvConceptFactoryFlex factory = new BsvConceptFactoryFlex("Test", null, props);
        Map<Long, Concept> c = factory.createConcepts(Collections.singleton(4096L));

        Assert.assertEquals("Concepts are not found", 1, c.size());
        Assert.assertTrue("Wrong concept type", c.get(4096L) instanceof ExtendedConcept);
        ExtendedConcept ec = (ExtendedConcept) c.get(4096L);
        Assert.assertEquals("Wrong params set", 1, ec.getParams().size());
        Assert.assertTrue("TTY is absent", ec.getParams().containsKey("TTY"));
        Assert.assertEquals("Wrong TTY", "BN", ec.getParams().get("TTY"));
        Assert.assertTrue("Wrong TUI", ec.getCtakesSemantics().stream().allMatch(sem -> sem == CONST.NE_TYPE_ID_DISORDER));
    }

    @Test
    public void flex_factory_not_typical_order_test() {
        Properties props = new Properties();
        props.setProperty("bsvPath", DICT2_PATH);
        props.setProperty("scheme", "CUI|STR|TTY|CODE|SAB|TUI|PREF");

        BsvConceptFactoryFlex factory = new BsvConceptFactoryFlex("Test", null, props);
        Map<Long, Concept> c = factory.createConcepts(Collections.singleton(4096L));

        Assert.assertEquals("Concepts are not found", 1, c.size());
        Assert.assertTrue("Wrong concept type", c.get(4096L) instanceof ExtendedConcept);
        ExtendedConcept ec = (ExtendedConcept) c.get(4096L);
        Assert.assertEquals("Wrong params set", 1, ec.getParams().size());
        Assert.assertTrue("TTY is absent", ec.getParams().containsKey("TTY"));
        Assert.assertEquals("Wrong TTY", "BN", ec.getParams().get("TTY"));
        Assert.assertTrue("Wrong TUI", ec.getCtakesSemantics().stream().allMatch(sem -> sem == CONST.NE_TYPE_ID_DISORDER));
    }

    @Test(expected = AssertionError.class)
    public void flex_factory_absent_params_test() {
        Properties props = new Properties();
        props.setProperty("bsvPath", DICT_PATH);

        BsvConceptFactoryFlex factory = new BsvConceptFactoryFlex("Test", null, props);
    }
}
