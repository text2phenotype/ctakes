package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import gov.nih.nlm.nls.lvg.Util.Str;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypePrioritiesFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FastPOSTagger extends JCasAnnotator_ImplBase {

    // LOG4J logger based on class name
    private Logger logger = Logger.getLogger(getClass().getName());

    public static final String POS_THEADS_COUNT_PARAM = "threadsCount";

    public static final String POS_MODEL_FILE_PARAM = "PosModelFile";
    public static final String PARAM_POS_MODEL_FILE = POS_MODEL_FILE_PARAM;
    @ConfigurationParameter(name = POS_MODEL_FILE_PARAM, mandatory = false, defaultValue = "org/apache/ctakes/postagger/models/mayo-pos.zip", description = "Model file for OpenNLP POS tagger")
    private String posModelPath;

    @ConfigurationParameter(name = POS_THEADS_COUNT_PARAM, mandatory = false, defaultValue = "10", description = "Count of threads")
    private int threadsCount = 10;

    private List<POSTaggerME> taggers;
    private ForkJoinPool threadPool;

    @Override
    public void initialize(UimaContext uimaContext)
            throws ResourceInitializationException {
        super.initialize(uimaContext);

        logger.info("POS tagger model file: " + posModelPath);
        threadPool = new ForkJoinPool(threadsCount);
        taggers = new ArrayList<>(threadsCount);
        for (int core = 0; core < threadsCount; core++) {
            try (InputStream fis = FileLocator.getAsStream(posModelPath)) {
                POSModel modelFile = new POSModel(fis);
                POSTaggerME tagger = new opennlp.tools.postag.POSTaggerME(modelFile);
                taggers.add(tagger);
//                taggers.put(tagger);
            } catch (Exception e) {
                logger.info("Error loading POS tagger model: " + posModelPath);
                throw new ResourceInitializationException(e);
            }
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        logger.info("process(JCas)");

        Map<Sentence, Collection<BaseToken>> tokensMap = JCasUtil.indexCovered(jCas, Sentence.class, BaseToken.class);
        Map<BaseToken, String> posMap = threadPool.invoke(new POSTask(new ArrayList<>(tokensMap.values()), taggers));
        for (BaseToken token: posMap.keySet()) {
            String posTag = posMap.get(token);
            token.setPartOfSpeech(posTag);
        }
        logger.info("Done");
    }

    public static AnalysisEngineDescription createAnnotatorDescription()
            throws ResourceInitializationException {
        return AnalysisEngineFactory.createEngineDescription(
                FastPOSTagger.class, TypeSystemDescriptionFactory
                        .createTypeSystemDescription(), TypePrioritiesFactory
                        .createTypePriorities(Segment.class, Sentence.class,
                                BaseToken.class));
    }

    public static AnalysisEngineDescription createAnnotatorDescription(
            String model) throws ResourceInitializationException {
        return AnalysisEngineFactory.createEngineDescription(
                FastPOSTagger.class, TypeSystemDescriptionFactory
                        .createTypeSystemDescription(), TypePrioritiesFactory
                        .createTypePriorities(Segment.class, Sentence.class,
                                BaseToken.class),
                FastPOSTagger.PARAM_POS_MODEL_FILE, model);
    }

    private class POSTask extends RecursiveTask<Map<BaseToken, String>> {
        private List<Collection<BaseToken>> tokensList;
        private List<POSTaggerME> taggers;

        public POSTask(List<Collection<BaseToken>> tokens, List<POSTaggerME> taggers) {
            this.tokensList = tokens;
            this.taggers = taggers;
        }

        @Override
        protected Map<BaseToken, String> compute() {
            Map<BaseToken, String> result = new HashMap<>();
            int batchSize = (int)Math.ceil((double) this.tokensList.size() / taggers.size());
            List<POSSentenceBatchTask> batchTasks = new ArrayList<>();

            for (int t=0; t < taggers.size(); t++) {
                POSTaggerME tagger = taggers.get(t);
                int startIndex = batchSize * t;
                int endIndex = Math.min(batchSize * (t + 1), this.tokensList.size());
                if ((endIndex == startIndex) || (startIndex >= this.tokensList.size()) || (endIndex > this.tokensList.size())) {
                    break;
                }
                List<Collection<BaseToken>> tokensBatch = tokensList.subList(startIndex, endIndex);
                POSSentenceBatchTask task = new POSSentenceBatchTask(tokensBatch, tagger);
                task.fork();
                batchTasks.add(task);
            }

            for (int t=0; t < batchTasks.size(); t++) {
                POSSentenceBatchTask subTask = batchTasks.get(t);
                result.putAll(subTask.join());

            }

            return result;
        }
    }

    private class POSSentenceBatchTask extends RecursiveTask<Map<BaseToken, String>> {
        private List<Collection<BaseToken>> tokensList;
        private POSTaggerME tagger;

        public POSSentenceBatchTask(List<Collection<BaseToken>> tokens, POSTaggerME tagger) {
            this.tokensList = tokens;
            this.tagger = tagger;
        }

        @Override
        protected Map<BaseToken, String> compute() {
            Map<BaseToken, String> result = new HashMap<>();
            for (Collection<BaseToken> baseTokens : tokensList) {
                List<BaseToken> printableTokens = new ArrayList<>();

                for(BaseToken token : baseTokens){
                    if(token instanceof NewlineToken) continue;
                    printableTokens.add(token);
                }

                String[] words = new String[printableTokens.size()];
                for (int i = 0; i < words.length; i++) {
                    words[i] = printableTokens.get(i).getCoveredText();
                }

                if (words.length > 0) {
                    String[] wordTagList = tagger.tag(words);
                    for (int i = 0; i < printableTokens.size(); i++) {
                        BaseToken token = printableTokens.get(i);
                        String posTag = wordTagList[i];
//                        token.setPartOfSpeech(posTag);
                        result.put(token, posTag);
                    }
                }
            }
            return result;
        }
    }
}
