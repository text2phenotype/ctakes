package com.text2phenotype.ctakes.rest.api.pipeline.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SabConceptModel {

    private String codingScheme;
    private Map<String, VocabConceptModel> vocabConcepts = new HashMap<>();

    private int hash;

    public SabConceptModel(String codingScheme) {
        this.codingScheme = codingScheme;
        hash = codingScheme.hashCode();
    }

    public String getCodingScheme() {
        return codingScheme;
    }

    public void setTty(String code, String tty) {
        if (!vocabConcepts.containsKey(code)) {
            vocabConcepts.put(code, new VocabConceptModel(code));
        }

        VocabConceptModel vocab = vocabConcepts.get(code);
        if (tty != null)
            vocab.setTty(tty);
    }

    public Collection<VocabConceptModel> getVocabConcepts() {
        return vocabConcepts.values();
    }

    public void addVocabConcepts(Collection<VocabConceptModel> vocabConcepts) {
        vocabConcepts.forEach(vocab -> {
            String code = vocab.getCode();
            if (this.vocabConcepts.containsKey(code)) {
                this.vocabConcepts.get(code).setTty(vocab.getTty());
            } else {
                this.vocabConcepts.put(code, vocab);
            }
        });
    }

    @Override
    public int hashCode() {
        int res = hash;
        if (vocabConcepts != null && vocabConcepts.size() > 0) {
            res += vocabConcepts.values().stream().mapToInt(Object::hashCode).sum();
        }

        return res;
    }
}
