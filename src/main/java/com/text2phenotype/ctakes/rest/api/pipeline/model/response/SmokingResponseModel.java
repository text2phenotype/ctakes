package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.SentenceSmokingStatus;
import org.apache.ctakes.typesystem.type.textspan.Sentence;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for smoking status pipeline results
 */
public class SmokingResponseModel extends ResponseModel {

    private String smokingStatus;
    private List<SentenceSmokingStatus> sentences = new ArrayList<>();

    public String getSmokingStatus() {
        return smokingStatus;
    }

    public void setSmokingStatus(String smokingStatus) {
        this.smokingStatus = smokingStatus;
    }


    public List<SentenceSmokingStatus> getSentences() {
        return sentences;
    }
}
