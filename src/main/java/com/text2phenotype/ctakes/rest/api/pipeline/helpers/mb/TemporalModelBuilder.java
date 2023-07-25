package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.model.EventModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.IdentifiedAnnotationModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.TLinkModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.TimeExModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.relation.TemporalTextRelation;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Model builder for Temporal Module pipeline
 */
public class TemporalModelBuilder extends ResponseModelBuilder_Impl<TemporalResponseModel> {

    private static final Logger LOGGER = Logger.getLogger(TemporalModelBuilder.class);

    private static final String PREFIX_EVENT_ID = "E";
    private static final String PREFIX_TIMEX_ID = "T";
    private static final String PREFIX_TLINK_ID = "TL";

    public TemporalModelBuilder() {
        super(TemporalResponseModel.class);
    }

    @Override
    public ResponseData build(JCas jCas) {

//        TemporalResponseModel resultModel = beanFactory != null ? beanFactory.getBean(TemporalResponseModel.class) : new TemporalResponseModel();
//        TemporalResponseModel resultModel = this.model;//this.abf.createBean(TemporalResponseModel.class);

        // get demographics data
        if (JCasUtil.exists(jCas, Demographics.class)) {
            Demographics demographics = JCasUtil.selectSingle(jCas, Demographics.class);
            resultModel.setGender(demographics.getGender());
            resultModel.setDob(demographics.getBirthDate());
        }
        // caches for performance optimization
        Map<IdentifiedAnnotation, Collection<Sentence>> sentenceIndex = JCasUtil.indexCovering(jCas, IdentifiedAnnotation.class, Sentence.class);
        Map<Sentence, Collection<Segment>> segmentIndex = JCasUtil.indexCovering(jCas, Sentence.class, Segment.class);

        // events
        Collection<EventMention> events = JCasUtil.select(jCas, EventMention.class);
        int counter = 0;
        for (EventMention event : events) {

            event.setId(counter++);

            // create new model of concept
            EventModel newEventModel = new EventModel();
            newEventModel.setName(EventMention.class.getSimpleName());

            // set event id
            newEventModel.setId(PREFIX_EVENT_ID + event.getId());

            // set text data
            newEventModel.setText(event);

            // set sentence data
            ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newEventModel, event);

            resultModel.getEvents().add(newEventModel);
        }

        // timex
        counter = 0;
        Collection<TimeMention> timexes = JCasUtil.select(jCas, TimeMention.class);
        for (TimeMention timex : timexes) {

            TimeExModel newTimexModel = new TimeExModel();

            // set timex id
            timex.setId(counter++);
            newTimexModel.setId(PREFIX_TIMEX_ID + timex.getId());

            // set text
            newTimexModel.setText(timex);

            // set sentence data
            ResponseModelBuilderUtils.setSentenceSegmentData(sentenceIndex, segmentIndex, newTimexModel, timex);

            // set class. DATE by default
            String timeClass = timex.getTimeClass();
            if (timeClass == null)
                timeClass = CONST.TIME_CLASS_DATE;
            newTimexModel.setType(timeClass);

            resultModel.getTimex().add(newTimexModel);

        }

        // t-links
        counter = 0;
        Collection<TemporalTextRelation> tlinks = JCasUtil.select(jCas, TemporalTextRelation.class);
        for (TemporalTextRelation tlink : tlinks) {
            TLinkModel newTLinkModel = new TLinkModel();

            newTLinkModel.setId(PREFIX_TLINK_ID + counter++);

            // set from id
            StringBuilder idString = new StringBuilder();
            Annotation arg1 = tlink.getArg1().getArgument();
            if (arg1 instanceof EventMention) {
                idString.append(PREFIX_EVENT_ID).append(((IdentifiedAnnotation)arg1).getId());
            } else if (arg1 instanceof TimeMention) {
                idString.append(PREFIX_TIMEX_ID).append(((IdentifiedAnnotation)arg1).getId());
            }
            newTLinkModel.setFromId(idString.toString());
            newTLinkModel.setFromText(arg1.getCoveredText());

            // set to id
            idString = new StringBuilder();
            Annotation arg2 = tlink.getArg2().getArgument();
            if (arg2 instanceof EventMention) {
                idString.append(PREFIX_EVENT_ID).append(((IdentifiedAnnotation)arg2).getId());
            } else if (arg2 instanceof TimeMention) {
                idString.append(PREFIX_TIMEX_ID).append(((IdentifiedAnnotation)arg2).getId());
            }

            newTLinkModel.setToId(idString.toString());
            newTLinkModel.setToText(arg2.getCoveredText());

            newTLinkModel.setType(tlink.getCategory());
            resultModel.getTlinks().add(newTLinkModel);

        }
        return resultModel;
    }
}
