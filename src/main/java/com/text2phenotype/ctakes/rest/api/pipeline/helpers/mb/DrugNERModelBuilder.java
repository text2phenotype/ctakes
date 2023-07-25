package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.AnnotationUtils;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ERRORS;
import com.text2phenotype.ctakes.rest.api.pipeline.model.ContentModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.EventModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.AttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.AttributesModelFactory;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

/**
 * Model builder for Drug NER pipeline
 */
public class DrugNERModelBuilder extends ResponseModelBuilder_Impl<DrugResponseModel> {

    private static final Logger LOGGER = Logger.getLogger(DrugNERModelBuilder.class);

    public DrugNERModelBuilder() {
        super(DrugResponseModel.class);
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

        // caches for performance optimization
        Map<MedicationMention, Collection<Sentence>> sentenceIndex = JCasUtil.indexCovering(jCas, MedicationMention.class, Sentence.class);
        Map<Sentence, Collection<Segment>> segmentIndex = JCasUtil.indexCovering(jCas, Sentence.class, Segment.class);

        // events
        Collection<MedicationMention> events = JCasUtil.select(jCas, MedicationMention.class);
        for (MedicationMention event : events) {

            ContentModel newEventModel = new ContentModel();
            newEventModel.setName(EventMention.class.getSimpleName());

            // set text data
            newEventModel.setText(event);

            // set sentence data
            ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newEventModel, event);

            // create attributes model
            AttributesModel attrModel = AttributesModelFactory.createModel(event);
            try {
                if (!attrModel.init(event)) {
                    continue;
                }
            } catch (UIMAException e) {
                LOGGER.error(e);
                return new ErrorResponseModel(ERRORS.MODEL_BUILDER_INTERNAL_ERROR);
            }
            newEventModel.setAttributes(attrModel);

            ResponseModelBuilderUtils.createOntologyConcept(event, newEventModel);

            resultModel.getDrugEntities().add(newEventModel);
        }

        Collections.sort(resultModel.getDrugEntities());
        return resultModel;
    }
}
