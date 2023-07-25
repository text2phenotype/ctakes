package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SentenceDetectorWithCorrection extends SentenceDetector {

    private static final List<String> potentialWrongSymbols = Arrays.asList(")", "(", ":");

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        super.process(jcas);

        // combine sentences with potential wrong symbols in the end
        Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);

        if (sentences.size() > 0) {

            Iterator<Sentence> itr = sentences.iterator();
            Sentence current = itr.next();
            while (itr.hasNext()) {
                Sentence next = itr.next();
                String currentText = current.getCoveredText();
                String nextText = next.getCoveredText();
                if (potentialWrongSymbols.contains(currentText.substring(currentText.length() - 1)) ||
                        potentialWrongSymbols.contains(nextText.substring(0,1))) {
                    current.setEnd(next.getEnd());
                    next.removeFromIndexes();
                } else {
                    current = next;
                }
            }
        }
    }
}
