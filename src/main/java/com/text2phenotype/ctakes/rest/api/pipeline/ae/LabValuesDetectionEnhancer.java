package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.SpecialLabValueWord;
import org.apache.ctakes.typesystem.type.syntax.*;
import org.apache.ctakes.typesystem.type.textsem.MeasurementAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates annotation when lab values and unit are combined (e.g "50mg" instead of "50 mg")
 */
public class LabValuesDetectionEnhancer extends JCasAnnotator_ImplBase {

    private final static List<String> lessMoreTokens = Arrays.asList("<", ">");

    private final static Pattern VALUES_PATTERN = Pattern.compile("^([\\d.]+)([\\w%]+)$");
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        // find all word tokens and match them for RegEx pattern
        Collection<WordToken> tokens = JCasUtil.select(aJCas, WordToken.class);

        for (WordToken token: tokens) {
            Matcher m = VALUES_PATTERN.matcher(token.getCoveredText());
            if (m.matches() && m.groupCount() == 2) {

                NumToken numToken = new NumToken(aJCas);
                numToken.setBegin(token.getBegin() + m.start(1));
                numToken.setEnd(token.getBegin() + m.end(1));
                numToken.addToIndexes();

                WordToken unitToken = new WordToken(aJCas);
                unitToken.setBegin(token.getBegin() + m.start(2));
                unitToken.setEnd(token.getBegin() + m.end(2));
                unitToken.addToIndexes();

                token.removeFromIndexes();
            }
        }

        // mark all potential lab values like ">1.0" or "<1.0"
        Collection<Sentence> sents = JCasUtil.select(aJCas, Sentence.class);
        for (Sentence sent : sents) {
            Iterator<BaseToken> sentTokens = JCasUtil.iterator(sent, BaseToken.class, false, false);
            if (sentTokens.hasNext()) {
                BaseToken currentToken = sentTokens.next();
                while (sentTokens.hasNext()) {
                    if (currentToken instanceof PunctuationToken) {
                        if (lessMoreTokens.contains(currentToken.getCoveredText())) {
                            BaseToken nextToken = sentTokens.next();
                            if (nextToken instanceof NumToken) {
                                SpecialLabValueWord specialToken = new SpecialLabValueWord(aJCas, currentToken.getBegin(), nextToken.getEnd());
                                specialToken.addToIndexes();
                            } else {
                                currentToken = nextToken;
                            }
                            continue;
                        }

                    }
                    currentToken = sentTokens.next();
                }
            }
        }
    }
}
