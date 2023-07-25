package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.google.common.collect.Lists;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.timeml.time.TimeTypeAnnotator;

import org.cleartk.timeml.util.TimeWordsExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeClassAnnotator extends CleartkAnnotator<String> {

    private List<FeatureExtractor1<TimeMention>> featuresExtractors;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        this.featuresExtractors = Lists.newArrayList();
        this.featuresExtractors.add(new TimeClassAnnotator.LastWordExtractor<TimeMention>());
        FeatureExtractor1<TimeMention> ex = CharacterCategoryPatternFunction.createExtractor();
        this.featuresExtractors.add(ex);
        this.featuresExtractors.add(new TimeWordsExtractor<TimeMention>());
        this.featuresExtractors.add(new CleartkExtractor<TimeMention, BaseToken>(BaseToken.class, new CoveredTextExtractor<BaseToken>(), new CleartkExtractor.Bag(new CleartkExtractor.Covered())));
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        for (TimeMention time : JCasUtil.select(jCas, TimeMention.class)) {
            List<Feature> features = new ArrayList<Feature>();
            for (FeatureExtractor1<TimeMention> extractor : this.featuresExtractors) {
                features.addAll(extractor.extract(jCas, time));
            }
            if (this.isTraining()) {
                this.dataWriter.write(new Instance<String>(time.getTimeClass(), features));
            } else {
                String tclass = this.classifier.classify(features);
                time.setTimeClass(tclass);
            }
        }
    }

    private static class LastWordExtractor<T extends Annotation> implements NamedFeatureExtractor1<T> {

        private String featureName;

        public LastWordExtractor() {
            this.featureName = "LastWord";
        }

        @Override
        public String getFeatureName() {
            return this.featureName;
        }

        @Override
        public List<Feature> extract(JCas view, T focusAnnotation) {
            String[] words = focusAnnotation.getCoveredText().split("\\W+");
            return Arrays.asList(new Feature(this.featureName, words[words.length - 1]));
        }

    }
}
