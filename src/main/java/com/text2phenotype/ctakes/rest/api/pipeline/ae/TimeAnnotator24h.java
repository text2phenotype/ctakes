package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.fsm.Time24FSM;
import org.apache.ctakes.core.fsm.output.TimeToken;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.TimeAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TimeAnnotator24h extends JCasAnnotator_ImplBase {

    private Time24FSM timeFSM = new Time24FSM();

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);
        for (Sentence sentence: sentences) {
            List<BaseToken> tokens = JCasUtil.selectCovered(aJCas, BaseToken.class, sentence);
            try {
                Set<TimeToken> timeTokens = timeFSM.execute(tokens);
                for (TimeToken timeToken: timeTokens) {
                    TimeAnnotation newAnnotation = new TimeAnnotation(aJCas, timeToken.getStartOffset(), timeToken.getEndOffset());
                    newAnnotation.addToIndexes();
                }
            } catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }
}
