package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.BackwardsTimeAnnotatorWithCorrection;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BIOMED_690_tests {
    private final String CORRECT_DATES_PATH = "BIOMED_690/correct_dates.txt";
    private final String INCORRECT_DATES_PATH = "BIOMED_690/incorrect_dates.txt";

    private final String textTemplate = "Testing date value:";

    private List<String> loadLines(String fileName) throws IOException {
        String fullFilePath = FileLocator.getFullPath(fileName);
        Path path = Paths.get(fullFilePath);
        return Files.readAllLines(path);
    }

    private AnalysisEngine buildAE() throws ResourceInitializationException {
        AggregateBuilder builder = new AggregateBuilder();
        builder.add(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class));
        builder.add(AnalysisEngineFactory.createEngineDescription(POSTagger.createAnnotatorDescription()));
        builder.add(AnalysisEngineFactory.createEngineDescription(
                BackwardsTimeAnnotatorWithCorrection.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/temporal/ae/timeannotator/model.jar"));

        return builder.createAggregate();
    }

    private Iterator<JCas> createIterator(List<String> dates) throws UIMAException {
        final JCas jcas_default = JCasFactory.createJCas();
        final AnalysisEngine ae = buildAE();
        return dates.stream().map(date -> {
            String txt = String.format("%s %s", textTemplate, date);
            jcas_default.reset();
            jcas_default.setDocumentText(txt);
            (new Segment(jcas_default,0, txt.length())).addToIndexes();
            (new Sentence(jcas_default,0, txt.length())).addToIndexes();
            try {
                ae.process(jcas_default);
            } catch (AnalysisEngineProcessException e) {
                Assert.fail(e.getMessage());
            }
            return jcas_default;
        }).iterator();
    }

    @Test
    public void correct_dates_test() {
        try {
            List<String> dates = loadLines(CORRECT_DATES_PATH);

            Iterator<JCas> itr = createIterator(dates);
            while (itr.hasNext()) {
                JCas jCas = itr.next();
                Collection<TimeMention> times = JCasUtil.select(jCas, TimeMention.class);
                Assert.assertEquals("Count of mentions is wrong for: " + jCas.getDocumentText(), 1, times.size());
                TimeMention mention = times.stream().findFirst().get();
                Assert.assertEquals("Begin position is wrong", textTemplate.length() + 1, mention.getBegin());
                Assert.assertEquals("End position is wrong", jCas.getDocumentText().length(), mention.getEnd());

            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void incorrect_dates_test() {
        try {
            List<String> dates = loadLines(INCORRECT_DATES_PATH);

            Iterator<JCas> itr = createIterator(dates);
            while (itr.hasNext()) {
                JCas jCas = itr.next();
                Collection<TimeMention> times = JCasUtil.select(jCas, TimeMention.class);
                Assert.assertEquals("Count of mentions is wrong for: " + jCas.getDocumentText(), 0, times.size());
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
