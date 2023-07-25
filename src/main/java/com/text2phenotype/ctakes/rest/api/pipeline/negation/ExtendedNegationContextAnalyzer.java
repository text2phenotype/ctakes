package com.text2phenotype.ctakes.rest.api.pipeline.negation;

import com.text2phenotype.ctakes.rest.api.pipeline.fsm.PolarityFSM;
import org.apache.ctakes.core.fsm.output.NegationIndicator;
import org.apache.ctakes.core.fsm.token.TextToken;
import org.apache.ctakes.necontexts.ContextHit;
import org.apache.ctakes.necontexts.negation.NegationContextAnalyzer;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default negation context analyzer with additional checking
 */
public class ExtendedNegationContextAnalyzer extends NegationContextAnalyzer {

    private PolarityFSM _negIndicatorFSM = new PolarityFSM();
    private Set<String> _boundaryWordSet;

    @Override
    public void initialize(UimaContext annotatorContext) throws ResourceInitializationException {
        initBoundaryData();
    }

    private void initBoundaryData() {
        _boundaryWordSet = new HashSet<String>();
        _boundaryWordSet.add("but");
        _boundaryWordSet.add("however");
        _boundaryWordSet.add("nevertheless");
        _boundaryWordSet.add("notwithstanding");
        _boundaryWordSet.add("though");
        _boundaryWordSet.add("although");
//        _boundaryWordSet.add("if");
        _boundaryWordSet.add("when");
        _boundaryWordSet.add("how");
        _boundaryWordSet.add("what");
        _boundaryWordSet.add("which");
        _boundaryWordSet.add("while");
        _boundaryWordSet.add("since");
        _boundaryWordSet.add("then");
        _boundaryWordSet.add("i");
        _boundaryWordSet.add("he");
        _boundaryWordSet.add("she");
        _boundaryWordSet.add("they");
        _boundaryWordSet.add("we");

        _boundaryWordSet.add(";");
//        _boundaryWordSet.add(":");
        _boundaryWordSet.add(".");
        _boundaryWordSet.add(")");
    }

    @Override
    public boolean isBoundary(Annotation contextAnnotation, int scopeOrientation) throws AnalysisEngineProcessException {
        String lcText = contextAnnotation.getCoveredText().toLowerCase();
        return _boundaryWordSet.contains(lcText);
    }

    @Override
    public ContextHit analyzeContext(List<? extends Annotation> contextTokens, int scopeOrientation) throws AnalysisEngineProcessException {
        ContextHit hit = super.analyzeContext(contextTokens, scopeOrientation);

        if (hit == null) {
            List<TextToken> fsmTokenList = wrapAsFsmTokens(contextTokens);

            try {
                Set<NegationIndicator> s = _negIndicatorFSM.execute(fsmTokenList);

                if (s.size() > 0) {
                    NegationIndicator neg = s.iterator().next();
                    hit = new ContextHit(neg.getStartOffset(), neg.getEndOffset());
                }

            } catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        return hit;
    }
}
