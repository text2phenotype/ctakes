package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import com.text2phenotype.ctakes.rest.api.pipeline.model.AddressMentionModel;

import java.util.ArrayList;
import java.util.List;

public class AddressesResponceModel extends ResponseModel {

    private List<AddressMentionModel> addresses = new ArrayList<>();

    public List<AddressMentionModel> getAddresses() {
        return addresses;
    }
}