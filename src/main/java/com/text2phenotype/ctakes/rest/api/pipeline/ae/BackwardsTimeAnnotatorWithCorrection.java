package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.temporal.ae.BackwardsTimeAnnotator;
import org.apache.ctakes.typesystem.type.textsem.TimeAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.*;

public class BackwardsTimeAnnotatorWithCorrection extends BackwardsTimeAnnotator {

    private final List<Pattern> patterns = new ArrayList<>();

    public static final String PATTERNS_LIST_PATH = "patternsFilePath";

    @ConfigurationParameter(
            name = PATTERNS_LIST_PATH,
            mandatory = false,
            description = "Path to file with patterns",
            defaultValue = "com/text2phenotype/ctakes/resources/temporal/date_patterns.txt"
    )
    private String patternsFilePath;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        try {
            Files.lines(
                    Paths.get(FileLocator.getFullPath(patternsFilePath))
            ).forEach(line -> {
                patterns.add(Pattern.compile(line));
            });
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        super.process(jCas);

        Collection<TimeMention> timex = JCasUtil.select(jCas, TimeMention.class);
        for (TimeMention time: timex) {
            checkAnnotation(time);
        }
    }

    private void checkAnnotation(TimeMention timex) {
        String txt = timex.getCoveredText();
        for (Pattern pattern: patterns) {
            Matcher m = pattern.matcher(txt);
            if (m.find()) {
                timex.setEnd(timex.getBegin() + m.end());
                timex.setBegin(timex.getBegin() + m.start());
                return;
            }
        }

        // remove TimeX if it is not matched
        timex.removeFromIndexes();
    }

}
