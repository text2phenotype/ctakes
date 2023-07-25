package com.text2phenotype.ctakes.rest.api.pipeline.ae.feature;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.temporal.ae.feature.DependencyParseUtils;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.*;
import org.cleartk.timeml.util.TimeWordsExtractor;
import org.springframework.util.StringUtils;

import javax.print.DocFlavor;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EventTimeFeaturesExtractor implements
        RelationFeaturesExtractor<IdentifiedAnnotation,IdentifiedAnnotation> {

    private final String CLOSEST_PAIR_FEATURE = "ClosestPair";

    private final String DEPENDENCY_PATH_FEATURE = "dependency_path";
    private final String DEPENDENCY_PATH_LENGTH_FEATURE = "dependency_path_length";

    private final String ARG1_LEFT_MOST_EVENT_FEATURE = "Arg1LeftmostEvent";
    private final String ARG2_LEFT_MOST_EVENT_FEATURE = "Arg2LeftmostEvent";

    private final String MENTION1_PROPERTY_FEATURE = "mention1property";
    private final String MENTION2_PROPERTY_FEATURE = "mention2property";

    private final String CONJUNCTION_FEATURE = "ConjunctionFeature";
    private final String CONJUNCTION_FEATURE_VALUE = "Contain_Conjunction_inBetween";

    private final String SPECIALWD_IN_ARG1_FEATURE = "SpecialWd_InArg1";
    private final String SPECIALWD_IN_ARG2_FEATURE = "SpecialWd_InArg2";
    private final String SPECIALWD_IN_BETWEEN_FEATURE = "SpecialWd_InBetween";


    private FeatureExtractor1 coveredText = new CoveredTextExtractor();

    /**
     * First word of the mention, last word of the mention, all words of the mention as a bag, the
     * preceding 3 words, the following 3 words
     */
    private FeatureExtractor1 tokenContext = new CleartkExtractor(
            BaseToken.class,
            coveredText,
            new CleartkExtractor.FirstCovered(1),
            new CleartkExtractor.LastCovered(1),
            new CleartkExtractor.Bag(new CleartkExtractor.Covered()),
            new CleartkExtractor.Preceding(3),
            new CleartkExtractor.Following(3));

    /**
     * All extractors for mention 1, with features named to distinguish them from mention 2
     */
    private FeatureExtractor1 mention1FeaturesExtractor = new NamingExtractor1(
            "mention1",
            new CombinedExtractor1(coveredText, tokenContext));

    /**
     * All extractors for mention 2, with features named to distinguish them from mention 1
     */
    private FeatureExtractor1 mention2FeaturesExtractor = new NamingExtractor1(
            "mention2",
            new CombinedExtractor1(coveredText, tokenContext));

    /**
     * First word, last word, and all words between the mentions
     */
    private CleartkExtractor tokensBetween = new CleartkExtractor(
            BaseToken.class,
            new NamingExtractor1("BetweenMentions", coveredText),
            new CleartkExtractor.FirstCovered(1),
            new CleartkExtractor.LastCovered(1),
            new CleartkExtractor.Bag(new CleartkExtractor.Covered()));

    /**
     * Number of words between the mentions
     */
    //private DistanceExtractor nTokensBetween = new DistanceExtractor(null, BaseToken.class);
    private TokenDistanceExtractor nTokensBetween = new TokenDistanceExtractor(null, BaseToken.class);



    private static final String LOOKUP_PATH = "/org/apache/ctakes/temporal/TimeLexicon.csv";
    private Multimap<String, String> specialWd;
    public EventTimeFeaturesExtractor() {
        this.specialWd = ArrayListMultimap.create();
        URL url = TimeWordsExtractor.class.getResource(LOOKUP_PATH);
        try {
            for (String line : Resources.readLines(url, Charsets.US_ASCII)) {
                String[] WordAndType = line.split(",");
                if (WordAndType.length != 2) {
                    throw new IllegalArgumentException("Expected '<word>,<type>', found: " + line);
                }
                this.specialWd.put(WordAndType[0], WordAndType[1]);
            }
        } catch (IOException e) {
            System.err.println("TimeLexicon resource initialization error.");
        }
    }

    @Override
    public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {


        ArrayList<Feature> feats = new ArrayList<Feature>();
        EventMention event = null;
        TimeMention time = null;

        IdentifiedAnnotation originalArg1 = arg1;
        IdentifiedAnnotation originalArg2 = arg2;

        // swap the order if necessary:
        if(arg2.getBegin() <= arg1.getBegin() && arg2.getEnd() <= arg1.getEnd()){
            IdentifiedAnnotation temp = arg1;
            arg1 = arg2;
            arg2 = temp;
        }

        if(arg1 instanceof EventMention){
            event = (EventMention) arg1;
            time = (TimeMention) arg2;
        }else{
            time = (TimeMention) arg1;
            event = (EventMention) arg2;
        }

        // UnexpandedTokenFeaturesExtractor

        feats.addAll(this.mention1FeaturesExtractor.extract(jCas, originalArg1));
        feats.addAll(this.mention2FeaturesExtractor.extract(jCas, originalArg2));
        feats.addAll(this.tokensBetween.extractBetween(jCas, originalArg1, originalArg2));
        feats.addAll(this.nTokensBetween.extract(jCas, originalArg1, originalArg2));


        // NearestFlagFeatureExtractor
        List<? extends IdentifiedAnnotation> nextArg2Type = JCasUtil.selectFollowing(jCas, arg2.getClass(), arg1, 1);
        List<? extends IdentifiedAnnotation> nextArg1Type = JCasUtil.selectFollowing(jCas, arg1.getClass(), arg1, 1);
        if (nextArg1Type.size() == 0 || nextArg1Type.get(0).getBegin() >= arg2.getBegin()) {
            if (nextArg2Type.size() > 0 && nextArg2Type.get(0) == arg2) {
                feats.add(new Feature(CLOSEST_PAIR_FEATURE, CLOSEST_PAIR_FEATURE));
            }
        }

        // DependencyPathFeaturesExtractor

        ConllDependencyNode node1 = DependencyParseUtils.findAnnotationHead(jCas, originalArg1);
        ConllDependencyNode node2 = DependencyParseUtils.findAnnotationHead(jCas, originalArg2);
        if (node1 != null && node2 != null)
        {
            LinkedList<ConllDependencyNode> node1ToNode2Path = DependencyParseUtils.getPathBetweenNodes(node1, node2);
            feats.add(new Feature(DEPENDENCY_PATH_FEATURE, DependencyParseUtils.pathToString(node1ToNode2Path)));
            feats.add(new Feature(DEPENDENCY_PATH_LENGTH_FEATURE, node1ToNode2Path.size()));
        }

        //EventArgumentPropertyExtractor
        Iterator<Sentence> sentItr = JCasUtil.iterator(jCas, Sentence.class);
        while (sentItr.hasNext()) {
            Sentence sent = sentItr.next();
            if (sent.getBegin() <= arg1.getBegin() && sent.getEnd() >= arg2.getEnd()) {
                Iterator<EventMention> eventsItr = JCasUtil.subiterate(jCas, EventMention.class, sent, true, true).iterator();

                if (eventsItr.hasNext()) {
                    EventMention anchor = eventsItr.next();
                    if(originalArg1 == anchor){
                        feats.add(new Feature(ARG1_LEFT_MOST_EVENT_FEATURE));
                    }else if(originalArg2 == anchor){
                        feats.add(new Feature(ARG2_LEFT_MOST_EVENT_FEATURE));
                    }

                }
            }
        }

        feats.addAll(getEventFeats( originalArg1 == event ? MENTION1_PROPERTY_FEATURE : MENTION2_PROPERTY_FEATURE, event));

        // ConjunctionRelationFeaturesExtractor

        List<BaseToken> betweenTokens = JCasUtil.selectBetween(jCas, BaseToken.class, arg1, arg2);
        List<EventMention> eventsInBetween = JCasUtil.selectBetween(jCas, EventMention.class, arg1, arg2);
        if(eventsInBetween.size() == 0 ){
            for (BaseToken token: betweenTokens){
                String pos = token.getPartOfSpeech();
                if(pos == null) continue;
                if(pos.startsWith("CC")||pos.equals(",")||pos.startsWith("IN")){
                    feats.add(new Feature(CONJUNCTION_FEATURE, CONJUNCTION_FEATURE_VALUE));
                    feats.add(new Feature(CONJUNCTION_FEATURE, pos));
                }
            }
        }


        // CheckSpecialWordRelationExtractor

        int begin = arg1.getEnd();
        int end = arg2.getBegin();

        if (begin < end) {
            String textSpan = jCas.getDocumentText().substring(begin, end).toLowerCase();
            for (String lexicon : specialWd.keySet()) {

                int spanStart = textSpan.indexOf(lexicon);
                if (spanStart > -1) {
                    int pos = spanStart + lexicon.length();
                    if (pos < textSpan.length() && Character.isLetterOrDigit(textSpan.charAt(pos))) {
                        spanStart = -1;
                    } else {
                        spanStart = spanStart + begin;
                    }

                }


                if (spanStart >= begin) {
                    int spanEnd = spanStart + lexicon.length();
                    if (spanEnd <= end) {
                        if (spanEnd <= arg1.getEnd()) {
                            String type = StringUtils.collectionToCommaDelimitedString(specialWd.get(lexicon));
                            Feature feature = new Feature(SPECIALWD_IN_ARG1_FEATURE, type);
                            feats.add(feature);
                        } else {
                            if (spanStart >= arg2.getBegin()) {
                                String type = StringUtils.collectionToCommaDelimitedString(specialWd.get(lexicon));
                                Feature feature = new Feature(SPECIALWD_IN_ARG2_FEATURE, type);
                                feats.add(feature);
                            } else {
                                String type = StringUtils.collectionToCommaDelimitedString(specialWd.get(lexicon));
                                Feature feature = new Feature(SPECIALWD_IN_BETWEEN_FEATURE, type);
                                feats.add(feature);
                            }
                        }
                    }
                }
            }
        }

        // TemporalAttributeFeatureExtractor

        if(event.getEvent()!=null && event.getEvent().getProperties().getContextualModality()!=null)
            feats.add(new Feature("Event-Modality-", event.getEvent().getProperties().getContextualModality()));

        if (time == originalArg2) {
            feats.add(new Feature("Time-Class-", time.getTimeClass()));
        } else {
            feats.add(new Feature("Timex-Class-", time.getTimeClass()));
        }


        return feats;

    }

    private static Collection<? extends Feature> getEventFeats(String name, EventMention mention) {
        List<Feature> feats = new ArrayList<>();
        //add contextual modality as a feature
        if(mention.getEvent()==null || mention.getEvent().getProperties() == null){
            return feats;
        }
        String contextualModality = mention.getEvent().getProperties().getContextualModality();
        if (contextualModality != null)
            feats.add(new Feature(name + "_modality", contextualModality));

        //    feats.add(new Feature(name + "_aspect", mention.getEvent().getProperties().getContextualAspect()));//null
        //    feats.add(new Feature(name + "_permanence", mention.getEvent().getProperties().getPermanence()));//null
        Integer polarity = mention.getEvent().getProperties().getPolarity();
        if(polarity!=null )
            feats.add(new Feature(name + "_polarity", polarity));
        //    feats.add(new Feature(name + "_category", mention.getEvent().getProperties().getCategory()));//null
        //    feats.add(new Feature(name + "_degree", mention.getEvent().getProperties().getDegree()));//null
        String docTimeRel = mention.getEvent().getProperties().getDocTimeRel();
        if(docTimeRel!=null)
            feats.add(new Feature(name + "_doctimerel", docTimeRel));

        Integer typeId = mention.getEvent().getProperties().getTypeIndexID();
        if(typeId != null)
            feats.add(new Feature(name + "_typeId"));

        return feats;
    }
}
