package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import com.text2phenotype.ctakes.rest.api.pipeline.model.AddressDataModel;
import org.codehaus.jackson.annotate.JsonProperty;

public class NPIAttributesModel {

    private String phone;
    private String fax;

    @JsonProperty("address")
    private AddressDataModel address = new AddressDataModel();

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public AddressDataModel getAddress() {
        return address;
    }
}
