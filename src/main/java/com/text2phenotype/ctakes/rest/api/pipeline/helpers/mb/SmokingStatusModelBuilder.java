package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.SmokingStatusRelation;
import com.text2phenotype.ctakes.rest.api.pipeline.model.SentenceSmokingStatus;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.smokingstatus.type.SmokingDocumentClassification;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import scala.util.Sorting;

import java.util.Collection;

/**
 * Model builder for Smoking Status pipeline
 */
public class SmokingStatusModelBuilder extends ResponseModelBuilder_Impl<SmokingResponseModel> {

    public SmokingStatusModelBuilder() {
        super(SmokingResponseModel.class);
    }

    @Override
    public ResponseData build(JCas jCas) {

//        SmokingResponseModel resultModel = beanFactory != null ? beanFactory.getBean(SmokingResponseModel.class) : new SmokingResponseModel();

        // set document id
        resultModel.setDocId(DocumentIDAnnotationUtil.getDeepDocumentId(jCas));

        // get smoking status
        Collection<SmokingDocumentClassification> sdc = JCasUtil.select(jCas, SmokingDocumentClassification.class);
        if (sdc.size() > 0) {
            resultModel.setSmokingStatus(sdc.iterator().next().getClassification());
        }

        // get status for each sentence
        Collection<SmokingStatusRelation> relations = JCasUtil.select(jCas, SmokingStatusRelation.class);
        for (SmokingStatusRelation relation: relations) {
            Sentence sentence = relation.getSentence();
            StringArray statuses = relation.getStatuses();
            String statusValue = String.join(",", statuses.toStringArray());

            SentenceSmokingStatus status = new SentenceSmokingStatus();
            status.setStatus(statusValue);
            status.setText(new Object[] {
                    sentence.getCoveredText(),
                    sentence.getBegin(),
                    sentence.getEnd()
            });
            resultModel.getSentences().add(status);
        }
        return resultModel;
    }
}
