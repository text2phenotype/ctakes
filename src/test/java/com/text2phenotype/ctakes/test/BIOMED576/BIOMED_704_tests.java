package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.dictionary.BsvRareWordDictionaryWithSAB;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Collection;

public class BIOMED_704_tests {

    private final String dict_path = FileLocator.getFullPath("BIOMED_704/dict.bsv");
    private final String dict_name = "DictionaryName";
    private final String test_dict_text = "white cell";
    private final String test_rare_word = "white";
    private final String test_wrong_rare_word = "red";

    public BIOMED_704_tests() throws FileNotFoundException {
    }

    @Test
    public void fast_lookup_token_test() throws Exception {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, null);

        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        jcas.setDocumentText("Test term is white cell");
        WordToken tok = new WordToken(jcas, 13, 18);

        FastLookupToken flToken = new FastLookupToken(tok);
        Collection<RareWordTerm> result = dict.getRareWordHits(flToken);
        Assert.assertEquals("Wrong amount of rare word terms", 1, result.size());

        RareWordTerm rareTerm = result.stream().findFirst().get();
        Assert.assertEquals("Wrong tokens count", 2, rareTerm.getTokenCount());
        Assert.assertEquals("Wrong rare word index", 0, rareTerm.getRareWordIndex());
        Assert.assertEquals("Wrong rare word", test_rare_word, rareTerm.getRareWord());
        Assert.assertEquals("Wrong dict text", test_dict_text, rareTerm.getText());
    }

    @Test
    public void rare_word_test() {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, null);
        Collection<RareWordTerm> result = dict.getRareWordHits(test_rare_word);
        Assert.assertEquals("Wrong amount of rare word terms", 1, result.size());

        RareWordTerm rareTerm = result.stream().findFirst().get();
        Assert.assertEquals("Wrong tokens count", 2, rareTerm.getTokenCount());
        Assert.assertEquals("Wrong rare word index", 0, rareTerm.getRareWordIndex());
        Assert.assertEquals("Wrong rare word", test_rare_word, rareTerm.getRareWord());
        Assert.assertEquals("Wrong dict text", test_dict_text, rareTerm.getText());
    }

    @Test
    public void fast_lookup_token_by_code_test() throws Exception  {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, "CODE");

        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        jcas.setDocumentText("Test term code is 1");
        NumToken tok = new NumToken(jcas, 18, 19);

        FastLookupToken flToken = new FastLookupToken(tok);
        Collection<RareWordTerm> result = dict.getRareWordHits(flToken);
        Assert.assertEquals("Wrong amount of rare word terms", 1, result.size());

        RareWordTerm rareTerm = result.stream().findFirst().get();
        Assert.assertEquals("Wrong tokens count", 1, rareTerm.getTokenCount());
        Assert.assertEquals("Wrong rare word index", 0, rareTerm.getRareWordIndex());
        Assert.assertEquals("Wrong rare word", "1", rareTerm.getRareWord());
        Assert.assertEquals("Wrong dict text", "1", rareTerm.getText());
    }

    @Test
    public void rare_word_by_code_test() {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, "CODE");
        Collection<RareWordTerm> result = dict.getRareWordHits("1");
        Assert.assertEquals("Wrong amount of rare word terms", 1, result.size());

        RareWordTerm rareTerm = result.stream().findFirst().get();
        Assert.assertEquals("Wrong tokens count", 1, rareTerm.getTokenCount());
        Assert.assertEquals("Wrong rare word index", 0, rareTerm.getRareWordIndex());
        Assert.assertEquals("Wrong rare word", "1", rareTerm.getRareWord());
        Assert.assertEquals("Wrong dict text", "1", rareTerm.getText());
    }

    @Test
    public void get_name_test() {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, null);
        Assert.assertEquals(dict_name, dict.getName());
    }

    @Test
    public void incorrect_fast_lookup_token_test() throws Exception {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, null);

        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        jcas.setDocumentText("Test term is red cell");
        WordToken tok = new WordToken(jcas, 13, 16);

        FastLookupToken flToken = new FastLookupToken(tok);
        Collection<RareWordTerm> result = dict.getRareWordHits(flToken);
        Assert.assertEquals("Wrong amount of rare word terms", 0, result.size());
    }

    @Test
    public void incorrect_rare_word_test() {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, null);
        Collection<RareWordTerm> result = dict.getRareWordHits(test_wrong_rare_word);
        Assert.assertEquals("Wrong amount of rare word terms", 0, result.size());
    }

    @Test
    public void incorrect_fast_lookup_token_by_code_test() throws Exception  {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, "CODE");

        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        jcas.setDocumentText("Test term code is 2");
        NumToken tok = new NumToken(jcas, 18, 19);

        FastLookupToken flToken = new FastLookupToken(tok);
        Collection<RareWordTerm> result = dict.getRareWordHits(flToken);
        Assert.assertEquals("Wrong amount of rare word terms", 0, result.size());
    }

    @Test
    public void incorrect_rare_word_by_code_test() {
        BsvRareWordDictionaryWithSAB dict = new BsvRareWordDictionaryWithSAB(dict_name, dict_path, "CODE");
        Collection<RareWordTerm> result = dict.getRareWordHits("2");
        Assert.assertEquals("Wrong amount of rare word terms", 0, result.size());
    }

}
