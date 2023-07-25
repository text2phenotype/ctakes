package com.text2phenotype.ctakes.rest.api.pipeline.model;


import java.util.*;

/**
 * Data model for UMLS concept
 */
public class UmlsConceptModel {

    private Set<String> tui = new HashSet<>();
    private String cui;
    private String preferredText;
    private Map<String, SabConceptModel> sabConcepts = new HashMap<>();

    private int hash;

    public UmlsConceptModel(String cui, String preferredText) {
        this.cui = cui;
        this.preferredText = preferredText;

        hash = (cui+preferredText).hashCode();
    }

    public void setTui(String tui) {
    	if (tui != null) {
    		this.tui.add(tui);
    	}
    }

    public void setTty(String codingScheme, String code, String tty) {
        if (!sabConcepts.containsKey(codingScheme)) {
            sabConcepts.put(codingScheme, new SabConceptModel(codingScheme));
        }

        SabConceptModel sab = sabConcepts.get(codingScheme);
        sab.setTty(code, tty);
    }

    public void setSabConcepts(SabConceptModel sabConcept) {
        if (this.sabConcepts.containsKey(sabConcept.getCodingScheme())) {
            SabConceptModel sab = this.sabConcepts.get(sabConcept.getCodingScheme());
            sab.addVocabConcepts(sabConcept.getVocabConcepts());
        } else {
            this.sabConcepts.put(sabConcept.getCodingScheme(), sabConcept);
        }

    }

    public Set<String> getTui() {
        return tui;
    }
    public String getCui() {
        return cui;
    }
    public String getPreferredText() {
        return preferredText;
    }

    public Collection<SabConceptModel> getSabConcepts() {
        return sabConcepts.values();
    }

    @Override
    public int hashCode() {
        int res = hash;
        if (sabConcepts != null && sabConcepts.size() > 0) {
            res += sabConcepts.values().stream().mapToInt(Object::hashCode).sum();
        }

        if (tui != null && tui.size() > 0) {
            res += tui.stream().mapToInt(Object::hashCode).sum();
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof UmlsConceptModel) && this.hash == ((UmlsConceptModel) obj).hash;
    }
}
