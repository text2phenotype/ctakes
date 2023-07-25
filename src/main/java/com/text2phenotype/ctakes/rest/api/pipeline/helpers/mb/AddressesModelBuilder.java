package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.AddressMention;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.AnnotationUtils;
import com.text2phenotype.ctakes.rest.api.pipeline.model.AddressMentionModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.AddressesResponceModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.*;

public class AddressesModelBuilder extends ResponseModelBuilder_Impl<AddressesResponceModel> {

    private static final Logger LOGGER = Logger.getLogger(AddressesModelBuilder.class);


    public AddressesModelBuilder(){
        super(AddressesResponceModel.class);
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

        AnnotationUtils.mergeIdentifiedAnnotations(jCas, new ArrayList<>(JCasUtil.select(jCas, AddressMention.class)));

        List<AddressMentionModel> addressModel = resultModel.getAddresses();
        Collection<AddressMention> addresses = new ArrayList<>(JCasUtil.select(jCas, AddressMention.class));

        // caches for performance optimization
        Map<AddressMention, Collection<Sentence>> sentenceIndex = JCasUtil.indexCovering(jCas, AddressMention.class, Sentence.class);
        Map<Sentence, Collection<Segment>> segmentIndex = JCasUtil.indexCovering(jCas, Sentence.class, Segment.class);

        int counter = 0;
        for (AddressMention address : addresses) {

            address.setId(counter++);

            // create new model of address data
            AddressMentionModel newAddrModel = new AddressMentionModel();

            newAddrModel.setAspect(ResponseModelBuilderUtils.getAspect(address));
            newAddrModel.setName(address.getType().getShortName());

            // set text data
            newAddrModel.setText(address);

            // set sentence data
            ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newAddrModel, address);

//            // create attributes model
//            AttributesModel attrModel = new UnknownAttributesModel(); //AttributesModelFactory.createModel(event);
//            try {
//                attrModel.init(address);
//            } catch (UIMAException e) {
//                LOGGER.error(e);
//                return new ErrorResponseModel(ERRORS.MODEL_BUILDER_INTERNAL_ERROR);
//            }
//            newAddrModel.setAttributes(attrModel);

//            ResponseModelBuilderUtils.createOntologyConcept(address, newAddrModel);

            newAddrModel.getAddress().setStreet(address.getStreet());
            newAddrModel.getAddress().setCity(address.getCity());
            newAddrModel.getAddress().setState(address.getState());
            newAddrModel.getAddress().setZip(address.getZip());

            newAddrModel.setMatch("complete");
            if (
                    address.getStreet() == null ||
                    address.getCity() == null ||
                    address.getState() == null ||
                    address.getZip() == null
            ) {
                newAddrModel.setMatch("partial");
            }
            addressModel.add(newAddrModel);
        }


        Collections.sort(addressModel);
        return resultModel;
    }

}