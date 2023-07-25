package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.EventModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.TLinkModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.TimeExModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for temporal module pipeline results
 */
public class TemporalResponseModel extends ResponseModel {

    private List<EventModel> events = new ArrayList<EventModel>();
    private List<TLinkModel> tlinks = new ArrayList<TLinkModel>();
    private List<TimeExModel> timex = new ArrayList<TimeExModel>();

    public List<EventModel> getEvents() {
        return events;
    }

    public List<TLinkModel> getTlinks() {
        return tlinks;
    }

    public List<TimeExModel> getTimex() {
        return timex;
    }
}
