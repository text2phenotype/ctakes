package com.text2phenotype.ctakes.rest.api.pipeline.model;

public class AddressMentionModel extends EventModel {
    private String match;
    private AddressDataModel address = new AddressDataModel();

    public AddressDataModel getAddress() {
        return address;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }
}
