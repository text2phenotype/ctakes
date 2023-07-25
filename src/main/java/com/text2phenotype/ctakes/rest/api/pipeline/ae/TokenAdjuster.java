package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;
import com.text2phenotype.ctakes.rest.api.pipeline.fsm.LabUnitsFSM;
import org.apache.ctakes.core.fsm.output.BaseTokenImpl;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TokenAdjuster extends JCasAnnotator_ImplBase {

    public static final String UNITS_LIST_PATH = "unitsFilePath";

    @ConfigurationParameter(
            name = UNITS_LIST_PATH,
            mandatory = false,
            description = "Path to file with Units",
            defaultValue = "com/text2phenotype/ctakes/resources/tokenAdjuster/units.txt"
    )
    private String unitsFilePath;

    private LabUnitsFSM labUnitsFSM;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        Set<String> units = new HashSet<>();

        try {
            Files.lines(
                    Paths.get(FileLocator.getFullPath(unitsFilePath))
            ).forEach(units::add);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        labUnitsFSM = new LabUnitsFSM(units);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        Map<Sentence, Collection<BaseToken>> tokensIndex = JCasUtil.indexCovered(aJCas, Sentence.class, BaseToken.class);

        for (Sentence sent: tokensIndex.keySet()) {
            List<BaseToken> tokens = new ArrayList<>(tokensIndex.get(sent));
            Set unitTokens = labUnitsFSM.execute(tokens);
            for (Object tokenDescr: unitTokens) {
                BaseTokenImpl unitTokenDescr = (BaseTokenImpl)tokenDescr;

                int tokenNumber = -1;
                for (int i=0; i<tokens.size(); i++) {
                    BaseToken token = tokens.get(i);
                    if (token.getEnd() <= unitTokenDescr.getStartOffset() || token.getBegin() > unitTokenDescr.getEndOffset() || (token instanceof NewlineToken)) {
                        continue;
                    }

                    if (tokenNumber == -1) {
                        tokenNumber = token.getTokenNumber();
                    }
                    token.removeFromIndexes();
                }

                UnitToken unitToken = new UnitToken(aJCas, unitTokenDescr.getStartOffset(), unitTokenDescr.getEndOffset());
                unitToken.setTokenNumber(tokenNumber);
                unitToken.setPartOfSpeech("NP");
                unitToken.addToIndexes();
            }
        }

    }
}
