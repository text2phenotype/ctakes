package com.text2phenotype.ctakes.test.utils;

import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.SentenceDetectorAnnotatorBIO;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.FastSentenceDetectorAnnotatorBIO;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.LVGAnnotatorFast;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.Text2phenotypeCasTermAnnotator;

public class PipelineFactory {

    private AggregateBuilder builder = new AggregateBuilder();

    private PipelineFactory() {

    }

    public static PipelineFactory create() {
        return new PipelineFactory();
    }

    public PipelineFactory SegmentAnnotator() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
        return this;
    }

    public PipelineFactory SentenceDetector() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(
                FastSentenceDetectorAnnotatorBIO.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/core/sentdetect/model.jar",
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                "CHAR"));
        return this;
    }

    public PipelineFactory POSTagger() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(POSTagger.createAnnotatorDescription()));
        return this;
    }

    public PipelineFactory Tokenizer() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class));
        return this;
    }

    public PipelineFactory CDTokenizer() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(ContextDependentTokenizerAnnotator.class));
        return this;
    }

    public PipelineFactory Chunker() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(Chunker.class));
        builder.add(ChunkAdjuster.createAnnotatorDescription(new String[]{"NP", "NP"}, 1));
        builder.add(ChunkAdjuster.createAnnotatorDescription(new String[]{"NP", "PP", "NP"}, 2));
        builder.add(AnalysisEngineFactory.createEngineDescription(Chunker.class));
        return this;
    }

    public PipelineFactory LVG() throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(LVGAnnotatorFast.class));
        return this;
    }

    public PipelineFactory FDL(String description) throws ResourceInitializationException {
        builder.add(AnalysisEngineFactory.createEngineDescription(
                Text2phenotypeCasTermAnnotator.class,
                Text2phenotypeCasTermAnnotator.PARAM_EXC_TAGS_KEY,
                "VB",
                ConfigParameterConstants.PARAM_LOOKUP_XML,
                description
        ));
        return this;
    }

    public PipelineFactory add(AnalysisEngineDescription description) throws ResourceInitializationException {
        builder.add(description);
        return this;
    }

    public AnalysisEngine build() throws ResourceInitializationException {
        return builder.createAggregate();
    }
}
