package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.AnnotationUtils;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

public class MergeSimilarAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        List<IdentifiedAnnotation> annotations = new ArrayList(JCasUtil.select(aJCas, IdentifiedAnnotation.class));
        AnnotationUtils.mergeIdentifiedAnnotations(
                aJCas,
                annotations
        );
    }
}
