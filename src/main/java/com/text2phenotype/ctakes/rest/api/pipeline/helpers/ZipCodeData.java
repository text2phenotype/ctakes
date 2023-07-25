package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

/**
 * Additional data for Zip code
 */
public class ZipCodeData {
    private String city;
    private String state;

    public ZipCodeData() {

    }

    public ZipCodeData(String city, String state) {
        this.city = city;
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
