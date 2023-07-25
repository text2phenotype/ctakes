package com.text2phenotype.ctakes.rest.api.pipeline.ae.feature;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.FeatureExtractor2;

import java.util.Collections;
import java.util.List;

public class TokenDistanceExtractor<T extends Annotation, U extends Annotation> implements
        FeatureExtractor2<T, U> {

    private String name;

    private Class<? extends Annotation> unitClass;

    TokenDistanceExtractor(String name, Class<? extends Annotation> unitClass) {
        this.name = name;
        this.unitClass = unitClass;
    }

    public List<Feature> extract(JCas jCas, Annotation annotation1, Annotation annotation2) {

        Annotation firstAnnotation, secondAnnotation;

        if (annotation1.getBegin() <= annotation2.getBegin()) {
            firstAnnotation = annotation1;
            secondAnnotation = annotation2;
        } else {
            firstAnnotation = annotation2;
            secondAnnotation = annotation1;
        }

        String featureName = Feature.createName(this.name, "Distance", this.unitClass.getSimpleName());
        int featureValue;
        if (secondAnnotation.getBegin() <= firstAnnotation.getEnd()) {
            featureValue = 0;
        } else {
            List<? extends Annotation> annotations = JCasUtil.selectBetween(
                    jCas,
                    unitClass,
                    firstAnnotation,
                    secondAnnotation);
            featureValue = annotations.size();

        }

        return Collections.singletonList(new Feature(featureName, featureValue));
    }
}
