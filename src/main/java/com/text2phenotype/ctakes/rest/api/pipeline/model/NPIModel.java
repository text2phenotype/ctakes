package com.text2phenotype.ctakes.rest.api.pipeline.model;

import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.NPIAttributesModel;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class NPIModel extends AnnotationModel {

    private List<Object> text = new ArrayList<Object>(3);
    private String match;
    private String sab;
    private String code;
    private String tui;
    private String prefText;

    @JsonProperty("mailingAddress")
    private NPIAttributesModel mailingAddress = new NPIAttributesModel();

    @JsonProperty("physicalAddress")
    private NPIAttributesModel physicalAddress = new NPIAttributesModel();

    public List<Object> getText() {
        return text;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getSab() {
        return sab;
    }

    public void setSab(String sab) {
        this.sab = sab;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTui() {
        return tui;
    }

    public void setTui(String tui) {
        this.tui = tui;
    }

    public String getPrefText() {
        return prefText;
    }

    public void setPrefText(String prefText) {
        this.prefText = prefText;
    }

    public NPIAttributesModel getMailingAddress() {
        return mailingAddress;
    }
    public NPIAttributesModel getPhysicalAddress() {
        return physicalAddress;
    }
}
