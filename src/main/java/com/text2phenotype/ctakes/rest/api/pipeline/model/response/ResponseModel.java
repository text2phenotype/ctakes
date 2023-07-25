package com.text2phenotype.ctakes.rest.api.pipeline.model.response;

import org.springframework.beans.factory.annotation.Value;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Data model for pipeline
 */
public class ResponseModel implements ResponseData {


    private String jira;
    private String user;
    private String timestamp;
    private String date;
    private String version;

    private String docId;
    private String dob;
    private String gender;
    private String age;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getJira() {
        return jira;
    }

    @Value("${changes.jira:#{null}}")
    public void setJira(String jira) {
        this.jira = jira;
    }

    public String getUser() {
        return user;
    }

    @Value("${changes.user:#{null}}")
    public void setUser(String user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Value("${changes.date:#{null}}")
    public void setTimestamp(String timestamp) {
//        date="2018-05-17 10:41:14 +0700";
        if (timestamp != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss zz");
            try {
                this.timestamp = String.format("%d", df.parse(timestamp).getTime());
            } catch (ParseException e) {

            }

        }

    }

    public String getDate() {
        return date;
    }

    @Value("${changes.date:#{null}}")
    public void setDate(String date) {

//        date="2018-05-17 10:41:14 +0700";
        if (date != null) {
            this.date = date.substring(0, 10);
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss zz");
//            try {
//                this.date = date.substring(0, 10);
//            } catch (ParseException e) {
//
//            }

        }

    }

    public String getVersion() {
        return version;
    }

    @Value("${changes.version}")
    public void setVersion(String version) {
        this.version = version;
    }
}
