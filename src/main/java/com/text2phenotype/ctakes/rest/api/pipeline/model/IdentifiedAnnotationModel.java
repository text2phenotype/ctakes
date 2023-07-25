package com.text2phenotype.ctakes.rest.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.AttributesModel;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Base model for annotations
 */
public abstract class IdentifiedAnnotationModel extends AnnotationModel implements Comparable<IdentifiedAnnotationModel>{


    private List<Integer> sentence = new ArrayList<Integer>(2);
    private List<Integer> sectionOffset = new ArrayList<>(2);
    private List<Object> text = new ArrayList<Object>(3);
    private String sectionOid = "";
    private AttributesModel attributes;


    // setters
    public void setSectionOid(String Oid) {
        sectionOid = Oid;
    }
    public void setAttributes(AttributesModel attributes) {
        this.attributes = attributes;
    }
    public void setText(Annotation event) {
        text.add(0, event.getCoveredText());
        text.add(1, event.getBegin());
        text.add(2, event.getEnd());
    }

    public void setSentenceData(Sentence sentence) {
        this.sentence.add(sentence.getBegin());
        this.sentence.add(sentence.getEnd());
    }

    public void setSectionOffset(Segment segment) {
        sectionOffset.add(segment.getBegin());
        sectionOffset.add(segment.getEnd());
        this.setSectionOid(segment.getId());
    }

    public List<Integer> getSentence() {
        return sentence;
    }

    public List<Integer> getSectionOffset() {
        return sectionOffset;
    }

    public List<Object> getText() {
        return text;
    }

    public String getSectionOid() {
        return sectionOid;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public AttributesModel getAttributes() {
        return attributes;
    }

    @Override
    public int compareTo(IdentifiedAnnotationModel o) {
        int b1 = (int)this.getText().get(1);
        int b2 = (int)o.getText().get(1);

        int e1 = (int)this.getText().get(1);
        int e2 = (int)o.getText().get(1);

        int result = Integer.compare(b1,b2);
        if (result == 0) {
            result = Integer.compare(e1,e2);
        }
        return result;
    }
}
