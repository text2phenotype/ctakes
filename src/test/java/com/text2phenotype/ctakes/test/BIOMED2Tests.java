package com.text2phenotype.ctakes.test;

import com.text2phenotype.ctakes.rest.api.pipeline.model.response.NPIResponseModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

@Ignore
public class BIOMED2Tests {
    private HttpURLConnection connection = null;

    @Before
    public void initServerURL() {

        String NLP_HOST = "NLP_HOST";
        String serverURL = System.getenv(NLP_HOST);
        Assert.assertNotNull(serverURL);
        serverURL = String.format("%s/rest/npi/npi_recognition", serverURL);
        try {
            String type = "application/x-www-form-urlencoded; charset=utf-8";
            URL url = new URL(serverURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty( "Content-Type", type );

        } catch (Exception e) {
            Assert.fail("Can't connect to " + serverURL);
        }

    }

    private NPIResponseModel doRequest(String txt) {
        try {
            String encodedData = String.format("inputText=%s", URLEncoder.encode( txt, "UTF-8" ));
//            this.connection.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
            OutputStream outStream = this.connection.getOutputStream();
            outStream.write(encodedData.getBytes());
            outStream.flush();
            outStream.close();

            Assert.assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_OK);

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(connection.getInputStream(), NPIResponseModel.class);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    @Test
    public void correctDoctorName() {
        String text = "The doctor name is Deleys Brandman";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 1);
        Assert.assertEquals(model.getProviders().get(0).getCode(), "1891927463");
    }

    @Test
    public void wrongDoctorName() {
        String text = "The doctor name is John Brandman";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 0);
    }

    @Test
    public void falsePositiveTerms() {
        String text = "False positive term is NONE or CAN";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 0);
    }

    @Test
    public void correctAddress() {
        String text = "Doctor address is 99 STONEGATE RD";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 2);
        Assert.assertTrue(model.getProviders().stream().anyMatch(provider -> provider.getCode().equals("1891927463"))); // DELAYS
        Assert.assertTrue(model.getProviders().stream().anyMatch(provider -> provider.getCode().equals("1295004687"))); // CRAIG
    }

    @Test
    public void wrongAddress() {
        String text = "Doctor address is 0 STONEGATE RD";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 0);
    }

    @Test
    public void correctPhone() {
        String text = "Phone number: +1 650 529 0801";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 1);
        Assert.assertEquals(model.getProviders().get(0).getCode(), "1891927463");
    }

    @Test
    public void wrongPhone() {
        String text = "Phone number: +1 000 000 0000";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 0);
    }

    @Test
    public void correctFax() {
        String text = "Fax number: 903-675-2333";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertTrue(model.getProviders().size() > 0);

        List<Object> textData = model.getProviders().get(0).getText();
        Assert.assertTrue((Integer.parseInt(textData.get(1).toString()) == 12) && (Integer.parseInt(textData.get(2).toString()) == 24));
        Assert.assertEquals(model.getProviders().get(0).getMailingAddress().getFax(), "9036752333");
    }

    @Test
    public void wrongFax() {
        String text = "Fax number: +1 000 000 0000";
        NPIResponseModel model = doRequest(text);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getProviders().size(), 0);
    }

    @Test
    public void phoneNumberFormat() throws IOException {
        List<String> phones = Arrays.asList(
                "Phone number: 650-529-0801",
                "Phone number: 650 529 0801",
                "Phone number: (650)529-0801",
                "Phone number: (650) 529-0801",
                "Phone number: (650) 529 0801",
                "Phone number: 6505290801",
                "Phone number: 16505290801",
                "Phone number: +16505290801"
        );
        for (String text: phones) {
            NPIResponseModel model = doRequest(text);
            Assert.assertNotNull(model);
            Assert.assertEquals(model.getProviders().size(), 1);
            Assert.assertEquals(model.getProviders().get(0).getCode(), "1891927463");
            connection.disconnect();
            this.initServerURL();
        }
    }
}
