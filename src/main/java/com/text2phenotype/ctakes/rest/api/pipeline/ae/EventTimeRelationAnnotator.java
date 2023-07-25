package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.google.common.collect.Lists;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.feature.*;
import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.temporal.ae.EventTimeSelfRelationAnnotator;
import org.apache.ctakes.temporal.ae.feature.*;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.util.ViewUriUtil;

import java.util.*;

public class EventTimeRelationAnnotator extends EventTimeSelfRelationAnnotator {
    final private org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger( "EventTimeRelationAnnotator" );

    private Map<Sentence, Collection<EventMention>> eventsIndex = null;
    private Map<Sentence, Collection<TimeMention>> timeIndex = null;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        eventsIndex = JCasUtil.indexCovered(jCas, Sentence.class, EventMention.class);
        timeIndex = JCasUtil.indexCovered(jCas, Sentence.class, TimeMention.class);

        super.process(jCas);
    }

    @Override
    public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
            JCas jCas,
            Annotation sentence) {

        if (!(sentence instanceof Sentence) || !eventsIndex.containsKey(sentence) || !timeIndex.containsKey(sentence)) {
            return new ArrayList<>();
        }

        Collection<TimeMention> timeMentions = timeIndex.get(sentence);
        Collection<EventMention> events = eventsIndex.get(sentence);
        List<IdentifiedAnnotationPair> pairs = new ArrayList<>(timeMentions.size() * events.size());

        for (EventMention event : events) {
            for (TimeMention time : timeMentions) {
                pairs.add(new IdentifiedAnnotationPair(event, time));
            }
        }
        return pairs;
    }

    private RelationSyntacticETEmbeddingFeatureExtractor embedingExtractor;

    @Override
    protected List<RelationFeaturesExtractor<IdentifiedAnnotation,IdentifiedAnnotation>> getFeatureExtractors() {
        final String vectorFile = "org/apache/ctakes/temporal/gloveresult_3";
        try {
            this.embedingExtractor = new RelationSyntacticETEmbeddingFeatureExtractor(vectorFile);
        } catch (CleartkExtractorException e) {
            System.err.println("cannot find file: "+ vectorFile);
            e.printStackTrace();
        }
        return Lists.newArrayList(
                // new optimization
                 embedingExtractor
                , new EventTimeFeaturesExtractor()

                // old optimization
//                new UnexpandedTokenFeaturesExtractor()//new TokenFeaturesExtractor()
//                , embedingExtractor
//                , new OptimizedNearestFlagFeatureExtractor()
//                , new DependencyPathFeaturesExtractor()
//                , new OptimizedEventArgumentPropertyExtractor()
//                , new OptimizedConjunctionRelationFeaturesExtractor()
//                , new OptimizedCheckSpecialWordRelationExtractor()
//                , new OptimizedTemporalAttributeFeatureExtractor()

                // original
//                new UnexpandedTokenFeaturesExtractor()//new TokenFeaturesExtractor()
//                , embedingExtractor
//                , new NearestFlagFeatureExtractor()
//                , new DependencyPathFeaturesExtractor()
//                , new EventArgumentPropertyExtractor()
//                , new ConjunctionRelationFeaturesExtractor()
//                , new CheckSpecialWordRelationExtractor()
//                , new TemporalAttributeFeatureExtractor()
        );
    }
}
