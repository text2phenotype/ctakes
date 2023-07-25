package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ERRORS;
import com.text2phenotype.ctakes.rest.api.pipeline.model.LabValueModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.UmlsConceptModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.AttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.AttributesModelFactory;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.annotation.RequestScope;

import java.util.*;

/**
 * Model builder for Lab Value pipeline
 */
public class LabValuesModelBuilder extends ResponseModelBuilder_Impl<LabResponseModel> {

    private static final Logger LOGGER = Logger.getLogger(LabValuesModelBuilder.class);

    public LabValuesModelBuilder() {
        super(LabResponseModel.class);
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
        Map<LabMention, Collection<Sentence>> sentenceIndex = JCasUtil.indexCovering(jCas, LabMention.class, Sentence.class);
        Map<Sentence, Collection<Segment>> segmentIndex = JCasUtil.indexCovering(jCas, Sentence.class, Segment.class);

        List<LabValueModel> labModels = resultModel.getLabValues();
        Collection<LabMention> labMentions = JCasUtil.select(jCas, LabMention.class);
        for (LabMention labMention: labMentions) {
            LabValueModel newModel = new LabValueModel();

            newModel.setText(labMention);

            // set sentence data
            ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newModel, labMention);

            // create attributes model
            AttributesModel attrModel = AttributesModelFactory.createModel(labMention);
            try {
                attrModel.init(labMention);
            } catch (UIMAException e) {
                LOGGER.error(e);
                return new ErrorResponseModel(ERRORS.MODEL_BUILDER_INTERNAL_ERROR);
            }
            newModel.setAttributes(attrModel);

            ResponseModelBuilderUtils.createOntologyConcept(labMention, newModel);

            labModels.add(newModel);

        }

        Collections.sort(labModels);
        return resultModel;
    }
}
