import com.text2phenotype.ctakes.rest.api.pipeline.ae.FastContextAnnotator;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.Text2phenotypeCasTermAnnotator;
import org.apache.ctakes.core.ae.SentenceDetectorAnnotatorBIO;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class BIOMED_968_tests {

    private AnalysisEngine createEngine() throws Exception {
        AggregateBuilder builder = new AggregateBuilder();

        // create a pipeline with default sentence detector
        builder.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
        builder.add(AnalysisEngineFactory.createEngineDescription(
                SentenceDetectorAnnotatorBIO.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/core/sentdetect/model.jar",
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                "CHAR"));
        builder.add(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class));
        builder.add(POSTagger.createAnnotatorDescription());
        builder.add(AnalysisEngineFactory.createEngineDescription(
                Text2phenotypeCasTermAnnotator.class,
                "LookupXml",
                "BIOMED_968/dict.xml",
                "exclusionTags",
                "VB",
                "minimumSpan",
                1
        ));

        builder.add(AnalysisEngineFactory.createEngineDescription(
                FastContextAnnotator.class,
                "MaxLeftScopeSize",
                20,
                "MaxRightScopeSize",
                10,
                "ScopeOrder",
                "LEFT,RIGHT",
                "ContextAnalyzerClass",
                "com.text2phenotype.ctakes.rest.api.pipeline.negation.ExtendedNegationContextAnalyzer",
                "ContextHitConsumerClass",
                "org.apache.ctakes.necontexts.negation.NegationContextHitConsumer",
                "WindowAnnotationClass",
                "org.apache.ctakes.typesystem.type.textspan.Sentence",
                "FocusAnnotationClass",
                "org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation",
                "ContextAnnotationClass",
                "org.apache.ctakes.typesystem.type.syntax.BaseToken"
        ));
        return builder.createAggregate();
    }

    @Test
    public void positivePolarityTest() throws Exception {

        AnalysisEngine ae = createEngine();

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Patient is taking aspirin");
        ae.process(jcas);

        Collection<MedicationMention> medication = JCasUtil.select(jcas, MedicationMention.class);

        Assert.assertEquals("Annotation is not found", 1, medication.size());

        medication.forEach(a -> {
            Assert.assertEquals(0, a.getPolarity());
        });


        jcas.reset();
        jcas.setDocumentText("heart attack");
        ae.process(jcas);

        Collection<DiseaseDisorderMention> disease = JCasUtil.select(jcas, DiseaseDisorderMention.class);

        Assert.assertEquals("Annotation is not found", 1, disease.size());

        disease.forEach(a -> {
            Assert.assertEquals(0, a.getPolarity());
        });

    }

    @Test
    public void negativePolarityTest() throws Exception {

        AnalysisEngine ae = createEngine();

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Patient is not taking aspirin");
        ae.process(jcas);

        Collection<MedicationMention> medication = JCasUtil.select(jcas, MedicationMention.class);

        Assert.assertEquals("Annotation is not found", 1, medication.size());

        medication.forEach(a -> {
            Assert.assertEquals(-1, a.getPolarity());
        });


        jcas.reset();
        jcas.setDocumentText("No evidence of COPD");
        ae.process(jcas);

        medication = JCasUtil.select(jcas, MedicationMention.class);

        Assert.assertEquals("Annotation is not found", 1, medication.size());

        medication.forEach(a -> {
            Assert.assertEquals(-1, a.getPolarity());
        });

    }
}
