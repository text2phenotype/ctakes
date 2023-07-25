import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.core.ae.SentenceDetectorAnnotatorBIO;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.FastPOSTagger;

public class BIOMED_668_Tests {

    private static String txt;
    @BeforeClass
    public static void loadSample() {
        try {
            String samplePath = FileLocator.getFullPath("BIOMED_668/correctness_test_sample.txt");
            txt = new String(Files.readAllBytes(Paths.get(samplePath)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Compares results of improved and default POS taggers.
     */
    @Test
    public void testPOSTagger() throws UIMAException {
        AggregateBuilder builder_default = new AggregateBuilder();
        AggregateBuilder builder_improved = new AggregateBuilder();

        JCas jcas_default = JCasFactory.createJCas();
        jcas_default.setDocumentText(txt);

        JCas jcas_improved = JCasFactory.createJCas();
        jcas_improved.setDocumentText(txt);

        // create a pipeline with default sentence detector
        builder_default.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
        builder_default.add(AnalysisEngineFactory.createEngineDescription(
                SentenceDetectorAnnotatorBIO.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/core/sentdetect/model.jar",
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                "CHAR"));
        builder_default.add(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class));
        builder_default.add(POSTagger.createAnnotatorDescription());
        builder_default.createAggregate().process(jcas_default);

        // create a pipeline with improved sentence detector
        builder_improved.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
        builder_improved.add(AnalysisEngineFactory.createEngineDescription(
                SentenceDetectorAnnotatorBIO.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                "/org/apache/ctakes/core/sentdetect/model.jar",
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                "CHAR"));
        builder_improved.add(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class));
        builder_improved.add(FastPOSTagger.createAnnotatorDescription());
        builder_improved.createAggregate().process(jcas_improved);

        List<BaseToken> tokens_default = new ArrayList<>(JCasUtil.select(jcas_default, BaseToken.class));
        List<BaseToken> tokens_improved = new ArrayList<>(JCasUtil.select(jcas_improved, BaseToken.class));

        // compare POS values
        for (int i=0; i <tokens_default.size(); i++) {
            BaseToken token_d = tokens_default.get(i);
            BaseToken token_i = tokens_improved.get(i);

            Assert.assertEquals("Part of speach data is different", token_d.getPartOfSpeech(), token_i.getPartOfSpeech());
        }
    }
}
