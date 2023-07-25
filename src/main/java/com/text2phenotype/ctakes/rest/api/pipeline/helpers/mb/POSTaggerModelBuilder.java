package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.model.BaseTokenModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.BaseTokenAttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

/**
 * Model builder for POS Tagger pipeline
 */
public class POSTaggerModelBuilder extends ResponseModelBuilder_Impl<POSTaggerResponseModel> {

    public POSTaggerModelBuilder() {
        super(POSTaggerResponseModel.class);
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
        Map<BaseToken, Collection<Sentence>> sentenceIndex = JCasUtil.indexCovering(jCas, BaseToken.class, Sentence.class);
        Map<Sentence, Collection<Segment>> segmentIndex = JCasUtil.indexCovering(jCas, Sentence.class, Segment.class);

        List<BaseTokenModel> modelTokens = resultModel.getTokens();
        Collection<BaseToken> tokens = JCasUtil.select(jCas, BaseToken.class);
        for (BaseToken token: tokens) {

            String partOfSpeech = token.getPartOfSpeech();
            if (partOfSpeech != null) {
                BaseTokenModel newModel = new BaseTokenModel();

                newModel.setText(token);

                // set sentence data
                ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newModel, token);

                // create attributes model
                BaseTokenAttributesModel attrModel = new BaseTokenAttributesModel();

                attrModel.setPartOfSpeech(partOfSpeech);
                newModel.setAttributes(attrModel);

                modelTokens.add(newModel);
            }
        }
        Collections.sort(modelTokens);
        return resultModel;
    }
}
