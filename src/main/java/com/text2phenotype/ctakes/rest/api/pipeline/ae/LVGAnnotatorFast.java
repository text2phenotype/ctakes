package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.reader.AbstractReader;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.InputStream;
import java.net.URI;

public class LVGAnnotatorFast extends JCasAnnotator_ImplBase {

    private AbstractMPAnalyzer lemmatizer;

    public static final String ENG_LEMMATIZER_DATA_FILE = "org/apache/ctakes/dependency/parser/models/lemmatizer/dictionary-1.3.1.jar";

    public static final String PARAM_LEMMATIZER_DATA_FILE = "LemmatizerDataFile";

    @ConfigurationParameter(
            name = PARAM_LEMMATIZER_DATA_FILE,
            description = "This parameter provides the data file required for the MorphEnAnalyzer. If not "
                    + "specified, this analysis engine will use a default model from the resources directory",
            defaultValue = ENG_LEMMATIZER_DATA_FILE)
    protected URI lemmatizerDataFile;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

        try {
            InputStream lemmatizerModel = (this.lemmatizerDataFile == null)
                    ? FileLocator.getAsStream(ENG_LEMMATIZER_DATA_FILE)
                    : FileLocator.getAsStream(this.lemmatizerDataFile.getPath());

            this.lemmatizer = EngineGetter.getMPAnalyzer(AbstractReader.LANG_EN, lemmatizerModel);
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    /**
     * Invokes this annotator's analysis logic.
     */
    @Override
    public void process(JCas jcas)
            throws AnalysisEngineProcessException {
        Thread thread = Thread.currentThread();
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            // Convert CAS data into structures usable by ClearNLP
            for (WordToken wordToken : JCasUtil.selectCovered(jcas, WordToken.class, sentence)) {
                if (thread.isInterrupted()) {
                    throw new AnalysisEngineProcessException(new InterruptedException());
                }
                String lemma = lemmatizer.getLemma(wordToken.getCoveredText(), wordToken.getPartOfSpeech());
                if (lemma != null && !lemma.isEmpty()) {
                    wordToken.setCanonicalForm(lemma);
                    wordToken.setNormalizedForm(lemma);
                }

            }
        }

    }


}