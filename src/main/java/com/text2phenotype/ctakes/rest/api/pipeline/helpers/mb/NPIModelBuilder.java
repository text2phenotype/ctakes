package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes;
import com.text2phenotype.ctakes.rest.api.pipeline.model.NPIModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.NPIAttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.NPIResponseModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.refsem.Entity;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.List;

public class NPIModelBuilder extends ResponseModelBuilder_Impl<NPIResponseModel> {

    public NPIModelBuilder() {
        super(NPIResponseModel.class);
    }

    @Override
    public ResponseData build(JCas jCas) {
        // set document id
        resultModel.setDocId(DocumentIDAnnotationUtil.getDeepDocumentId(jCas));

        // get demographics data
        if (JCasUtil.exists(jCas, Demographics.class)) {
            Demographics demographics = JCasUtil.selectSingle(jCas, Demographics.class);
            resultModel.setGender(demographics.getGender());
            resultModel.setDob(demographics.getBirthDate());
        }

        Collection<NationalProviderMention> mentions = JCasUtil.select(jCas, NationalProviderMention.class);

        List<NPIModel> providersList = resultModel.getProviders();
        int cnt = 0;
        for (NationalProviderMention mention: mentions) {
            NPIModel newModel = new NPIModel();
            newModel.setId(String.valueOf(cnt));
            cnt++;
            newModel.setMatch(mention.getMatchType());
            newModel.setCode(mention.getCode());
            newModel.setSab(mention.getSAB());
            newModel.setTui(mention.getTUI());
            newModel.setPrefText(mention.getPrefText());

            List<Object> txt = newModel.getText();
            txt.add(0, mention.getCoveredText());
            txt.add(1, mention.getBegin());
            txt.add(2, mention.getEnd());

            NPIAttributes mailingAddress = mention.getMailingAddress();
            if (mailingAddress != null) {

                NPIAttributesModel mailingAddressModel = newModel.getMailingAddress();

                if (mailingAddress.getFax() > 0)
                    mailingAddressModel.setFax(mailingAddress.getFax().toString());
                if (mailingAddress.getPhone() > 0)
                    mailingAddressModel.setPhone(mailingAddress.getPhone().toString());

                mailingAddressModel.getAddress().setCity(mailingAddress.getCity());
                mailingAddressModel.getAddress().setState(mailingAddress.getState());
                mailingAddressModel.getAddress().setStreet(mailingAddress.getStreet());

                if (mailingAddress.getZip() > 0)
                    mailingAddressModel.getAddress().setZip(mailingAddress.getZip().toString());
            }

            NPIAttributes physicalAddress = mention.getPhysicalAddress();
            if (physicalAddress != null) {

                NPIAttributesModel physicalAddressModel = newModel.getPhysicalAddress();

                if (physicalAddress.getFax() > 0)
                    physicalAddressModel.setFax(physicalAddress.getFax().toString());
                if (physicalAddress.getPhone() > 0)
                    physicalAddressModel.setPhone(physicalAddress.getPhone().toString());

                physicalAddressModel.getAddress().setCity(physicalAddress.getCity());
                physicalAddressModel.getAddress().setState(physicalAddress.getState());
                physicalAddressModel.getAddress().setStreet(physicalAddress.getStreet());

                if (physicalAddress.getZip() > 0)
                    physicalAddressModel.getAddress().setZip(physicalAddress.getZip().toString());
            }

            providersList.add(newModel);
        }
        return resultModel;
    }

}
