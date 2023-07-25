package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.AnnotationUtils;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ERRORS;
import com.text2phenotype.ctakes.rest.api.pipeline.model.ContentModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.IdentifiedAnnotationModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.UmlsConceptModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.AttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.UnknownAttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.*;

/**
 * Model builder for Default Clinical pipeline
 */
public class DefaultModelBuilder extends ResponseModelBuilder_Impl<DefaultResponseModel> {

    private static final Logger LOGGER = Logger.getLogger(DefaultModelBuilder.class);


    public DefaultModelBuilder(){
        super(DefaultResponseModel.class);
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

        AnnotationUtils.mergeIdentifiedAnnotations(jCas, new ArrayList<>(JCasUtil.select(jCas, IdentifiedAnnotation.class)));

        List<ContentModel> contentModel = resultModel.getContent();
        Collection<IdentifiedAnnotation> events = new ArrayList<>();
        events.addAll(JCasUtil.select(jCas, EventMention.class));
        events.addAll(JCasUtil.select(jCas, EntityMention.class));

        // caches for performance optimization
        Map<IdentifiedAnnotation, Collection<Sentence>> sentenceIndex = JCasUtil.indexCovering(jCas, IdentifiedAnnotation.class, Sentence.class);
        Map<Sentence, Collection<Segment>> segmentIndex = JCasUtil.indexCovering(jCas, Sentence.class, Segment.class);

        int counter = 0;
        for (IdentifiedAnnotation event : events) {

            event.setId(counter++);

            // create new model of concept
            ContentModel newEventModel = new ContentModel();

            newEventModel.setAspect(ResponseModelBuilderUtils.getAspect(event));
            newEventModel.setName(event.getType().getShortName());

            // set text data
            newEventModel.setText(event);

            // set sentence data
            ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newEventModel, event);

            // create attributes model
            AttributesModel attrModel = new UnknownAttributesModel(); //AttributesModelFactory.createModel(event);
            try {
                attrModel.init(event);
            } catch (UIMAException e) {
                LOGGER.error(e);
                return new ErrorResponseModel(ERRORS.MODEL_BUILDER_INTERNAL_ERROR);
            }
            newEventModel.setAttributes(attrModel);

            ResponseModelBuilderUtils.createOntologyConcept(event, newEventModel);

            contentModel.add(newEventModel);
        }


        Collections.sort(contentModel);
        return resultModel;
    }

}
