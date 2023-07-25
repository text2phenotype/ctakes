package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textsem.DateAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Annotator to extract patient's data. (DOB, gender etc.)
 */
public class PatientDataExtractor extends JCasAnnotator_ImplBase {

    private static List<String> dobWords = Arrays.asList("dob", "birth");

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        Demographics demographics = null;

        Collection<Demographics> demograph = JCasUtil.select(aJCas, Demographics.class);
        if (demograph.size() > 0) {
            demographics = demograph.iterator().next();
        } else {
            demographics = new Demographics(aJCas);
        }

        boolean someDataIsFound = false;
        Collection<Sentence> sentences =  JCasUtil.select(aJCas, Sentence.class);
        for (Sentence sent: sentences) {
            Collection<WordToken> words = JCasUtil.selectCovered(aJCas, WordToken.class, sent);
            for (WordToken word: words) {
                if (demographics.getBirthDate() == null && dobWords.contains(word.getCoveredText().toLowerCase())) {

                    Collection<DateAnnotation> dates = JCasUtil.selectCovered(aJCas, DateAnnotation.class, word.getBegin(), sent.getEnd());
                    if (dates.size() > 0) {
                        demographics.setBirthDate(dates.iterator().next().getCoveredText());
                        someDataIsFound = true;
                        word.removeFromIndexes();
                        break;
                    }
                }
            }

            if (someDataIsFound) {
                demographics.addToIndexes();
                break;
            }
        }
    }
}
