package com.text2phenotype.ctakes.rest.api.pipeline.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VocabConceptModel {

    private Set<String> tty = new HashSet<>();
    private String code;

    private int hash;

    public VocabConceptModel(String code) {
    	this.code = (code != null) ? code : "";

        hash = this.code.hashCode();
    }

    public Set<String> getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty.add(tty);
    }

    public void setTty(Collection<String> tty) {
        tty.forEach(this::setTty);
    }

    public String getCode() {
        return code;
    }

    @Override
    public int hashCode() {
        int res = hash;
        if (tty != null && tty.size() > 0) {
            res += tty.stream().mapToInt(String::hashCode).sum();
        }
        return res;
    }
}
