package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.ctakes.core.ae.SentenceDetectorAnnotatorBIO;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.utils.struct.CounterMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

public class FastSentenceDetectorAnnotatorBIO extends CleartkAnnotator<String> {
    private static final int WINDOW_SIZE = 3;

    public static enum FEAT_CONFIG {GILLICK, CHAR, SHAPE, LINE_POS, CHAR_SHAPE, CHAR_POS, CHAR_SHAPE_POS }
    public static final String PARAM_FEAT_CONFIG = "FeatureConfiguration";
    @ConfigurationParameter(name=PARAM_FEAT_CONFIG,mandatory=false)
    private SentenceDetectorAnnotatorBIO.FEAT_CONFIG featConfig= SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR;

    public static final String PARAM_TOKEN_FILE = "TokenFilename";
    @ConfigurationParameter(name=PARAM_TOKEN_FILE,mandatory=false)
    private String tokenCountFile = "org/apache/ctakes/core/sentdetect/tokenCounts.txt";
    CounterMap<String> tokenCounts = new CounterMap<>();

    private Map<String, List<String>> prefixMap = new HashMap<>();
    private Map<Character, List<Object>> charFeaturesCacheMap = new HashMap<>();
    private Map<String, String> predictionsCache = new HashMap<>();
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException {
        super.initialize(context);
        try{
            Scanner scanner = new Scanner(FileLocator.getAsStream(tokenCountFile));
            while(scanner.hasNextLine()){
                String[] pair = scanner.nextLine().trim().split(" : ");
                if(pair.length == 2){
                    tokenCounts.put(pair[0], Integer.parseInt(pair[1]));
                }
            }
            scanner.close();

            // init feature names cache
            prefixMap.put("Character", Arrays.asList("Character_Id", "Character_Upper", "Character_Lower", "Character_Digit", "Character_Space"));
            prefixMap.put("CharOffset_-3", Arrays.asList("CharOffset_-3_Id", "CharOffset_-3_Upper", "CharOffset_-3_Lower", "CharOffset_-3_Digit", "CharOffset_-3_Space"));
            prefixMap.put("CharOffset_-2", Arrays.asList("CharOffset_-2_Id", "CharOffset_-2_Upper", "CharOffset_-2_Lower", "CharOffset_-2_Digit", "CharOffset_-2_Space"));
            prefixMap.put("CharOffset_-1", Arrays.asList("CharOffset_-1_Id", "CharOffset_-1_Upper", "CharOffset_-1_Lower", "CharOffset_-1_Digit", "CharOffset_-1_Space"));
            prefixMap.put("CharOffset_0", Arrays.asList("CharOffset_0_Id", "CharOffset_0_Upper", "CharOffset_0_Lower", "CharOffset_0_Digit", "CharOffset_0_Space"));
            prefixMap.put("CharOffset_1", Arrays.asList("CharOffset_1_Id", "CharOffset_1_Upper", "CharOffset_1_Lower", "CharOffset_1_Digit", "CharOffset_1_Space"));
            prefixMap.put("CharOffset_2", Arrays.asList("CharOffset_2_Id", "CharOffset_2_Upper", "CharOffset_2_Lower", "CharOffset_2_Digit", "CharOffset_2_Space"));
            prefixMap.put("CharOffset_3", Arrays.asList("CharOffset_3_Id", "CharOffset_3_Upper", "CharOffset_3_Lower", "CharOffset_3_Digit", "CharOffset_3_Space"));
        }catch(FileNotFoundException e){
            throw new ResourceInitializationException(e);
        }
    }

    private List<WordCacheData> createWordsCache(String segmentText, char[] chars) {
        List<WordCacheData> result = new ArrayList<>();
        int startIndex = 0;
        // find first non whitespace char
        while (startIndex < chars.length && Character.isWhitespace(chars[startIndex])) {
            startIndex++;
        }

        for (int i = startIndex + 1; i < chars.length; i++) {
            char c = chars[i];

            if (!Character.isWhitespace(c) && (i < chars.length - 1)) {
                continue;
            }

            WordCacheData cacheItem = new WordCacheData();
            cacheItem.begin = startIndex;
            cacheItem.end = i;
            cacheItem.text = segmentText.substring(cacheItem.begin, cacheItem.end);
            result.add(cacheItem);

            while (i < chars.length && Character.isWhitespace(chars[i])) {
                i++;
            }
            startIndex = i;

        }
        return result;
    }

    private static String getFeaturesKey(List<Feature> features){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < features.size(); ++i) {
            Feature feat = features.get(i);
            sb.append(feat.getName());
            sb.append(feat.getValue());
        }
        return sb.toString();
    }
    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
    	for(Segment seg : JCasUtil.select(jcas, Segment.class)) {
    		char[] chars = seg.getCoveredText().toCharArray();
            String segText = seg.getCoveredText();
            List<WordCacheData> wordCache = createWordsCache(segText, chars);
            if (wordCache.size() == 0) {
                continue;
            }
            WordCacheData prevTokenCache = new WordCacheData();
            prevTokenCache.begin = 0;
            prevTokenCache.end = 1;
            prevTokenCache.text = segText.substring(0,1);

            WordCacheData nextTokenCache = null;
            Iterator<WordCacheData> wcItr = wordCache.iterator();
            if (wcItr.hasNext()) {
                nextTokenCache = wcItr.next();
            }

            // Iterate over every character in the Segment and classify it as Begin, Inside, or Outside a Sentence
            int startInd=0;
            String prevOutcome = "O";
            boolean randColonStart = false;
            Thread thread = Thread.currentThread();
            for (int ind = 0; ind < chars.length; ind++) {
                if (thread.isInterrupted()) {
                    throw new AnalysisEngineProcessException(new InterruptedException());
                }

                if (ind >= nextTokenCache.end) {
                    prevTokenCache = nextTokenCache;
                    if (wcItr.hasNext()) {
                        nextTokenCache = wcItr.next();
                    } else {
                        nextTokenCache = new WordCacheData();
                        nextTokenCache.begin = chars.length-1;
                        nextTokenCache.end = nextTokenCache.begin;
                        nextTokenCache.text = "";
                    }
                }
                List<Feature> feats = getFeatures(chars, ind, prevOutcome, prevTokenCache, nextTokenCache);

                String outcome;
                int casInd = seg.getBegin() + ind;
                if (!prevOutcome.equals("O") && Character.isLetterOrDigit(chars[ind])){
                    outcome = "I";
                } else {
                    String key = getFeaturesKey(feats);
                    if (predictionsCache.containsKey(key)) {
                        outcome = predictionsCache.get(key);
                    } else {
                        outcome = this.classifier.classify(feats);
                        predictionsCache.put(key, outcome);
                    }
                    
                    boolean isRandomColon = (ind > 0 && chars[ind] == ':' && chars[ind - 1] == '\n');
                    if (isRandomColon) {
                    	outcome = "O";
                    	randColonStart = true;
                    }

                    if (outcome.equals("B")) {
                    	if (!randColonStart) {
                    		startInd = casInd;
                    	}
                    } else if (outcome.equals("O") && (prevOutcome.equals("I") || prevOutcome.equals("B"))) {
                        // just ended a sentence
                        int endInd = casInd;
                        while (endInd > startInd && Character.isWhitespace(chars[endInd - seg.getBegin() - 1])) {
                            endInd--;
                        }

                        if (endInd > startInd) {
                            if (makeSentence(jcas, startInd, endInd)) {
                            	randColonStart = false;
                            }
                        }
                    }
                    
                    if (isRandomColon) {
                    	startInd = casInd;
                    	randColonStart = true;
                    }
                }

                prevOutcome = outcome;
            }
            // One final sentence at the end of the segment if we were in the middle of one when we ran out of characters.
            if(!this.isTraining() && !prevOutcome.equals("O")){
                // segment ended with a sentence
                makeSentence(jcas, startInd, seg.getEnd());
            }
        }
    }

    // Create UIMA annotation after cleaning up begin and end of sentence.
    public static boolean makeSentence(JCas jcas, int begin, int end){
        String docText = jcas.getDocumentText();
        while(begin < docText.length() && Character.isWhitespace(docText.charAt(begin))){
            begin++;
        }
        
        while(end > 0 && Character.isWhitespace(docText.charAt(end-1))){
            end--;
        }
        
        if (begin < end) {
        	Sentence sent = new Sentence(jcas, begin, end);
            sent.addToIndexes();
            
            return true;
        }
        
        return false;
    }

    static CharacterCategoryPatternFunction<Annotation> shapeFun = new CharacterCategoryPatternFunction<>(CharacterCategoryPatternFunction.PatternType.REPEATS_AS_KLEENE_PLUS);
    
    private ArrayList<Feature> getFeatures(final char[] chars, final int index, final String prevOutcome, final WordCacheData prevTokenCache, 
    		final WordCacheData nextTokenCache) {
    	ArrayList<Feature> features = new ArrayList<>();

        // Start collecting features:
        features.add(new Feature("PrevOutcome", prevOutcome));

        // all systems get to know about the current char they're classifying (i.e. is this a period)
        features.addAll(getCharFeatures(chars[index], "Character"));

        if (featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR || featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR_POS || 
        		featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR_SHAPE || featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR_SHAPE_POS) {
            for (int window = -WINDOW_SIZE; window <= WINDOW_SIZE; window++) {
                if (index + window >= 0 && index + window < chars.length) {
                    char conChar = chars[index + window];
                    features.addAll(getCharFeatures(conChar, "CharOffset_" + window));
                }
            }
        }

        features.addAll(getTokenFeatures(prevTokenCache, nextTokenCache, "Token"));
        
        return features;
    }

    private Collection<? extends Feature> getTokenFeatures(WordCacheData prevToken, WordCacheData nextToken, String prefix) {
        List<Feature> feats = new ArrayList<>();

        // identity features (1 & 2 in Table 1, Gillick 2009)
        Feature prevTokenFeat = new Feature(prefix + "PrevIdentity", prevToken.text);
        feats.add(prevTokenFeat);
        Feature nextTokenFeat = new Feature(prefix + "NextIdentity", nextToken.text);
        feats.add(nextTokenFeat);

        // length features (3 in Gillick but only for the left token to approximately model abbreviations)
        if(featConfig != SentenceDetectorAnnotatorBIO.FEAT_CONFIG.GILLICK){
            feats.add(new Feature(prefix+"NextLength="+nextToken.text.length(), true));
        }
        feats.add(new Feature(prefix+"PrevLength="+prevToken.text.length(), true));

        // capitalzation of right word (4 in gillick)
        feats.add(new Feature(prefix+"cap", nextToken.text.length() > 0 && Character.isUpperCase(nextToken.text.charAt(0))));

        // shape features for word identity
        if(featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR_SHAPE_POS || featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR_SHAPE || featConfig == SentenceDetectorAnnotatorBIO.FEAT_CONFIG.SHAPE){
            feats.addAll(shapeFun.apply(prevTokenFeat));
            feats.addAll(shapeFun.apply(nextTokenFeat));
        }

        // token count features (5 & 6 in gillick)
        int rightLower = (int) Math.round(Math.log(tokenCounts.get(nextToken.text.toLowerCase())));
        feats.add(new Feature(prefix + "_RightLower_"+ rightLower, true));

        String prevDotless = prevToken.text;
        if(prevToken.text.endsWith(".")){
            prevDotless = prevToken.text.substring(0, prevToken.length()-1);
        }
        int leftDotless = (int) Math.round(Math.log(tokenCounts.get(prevDotless)));
        feats.add(new Feature(prefix + "_LeftDotless_" + leftDotless, true));

        // token joint features: identity pair (7 in gillick) and left_identity-right_is_capitalized (8 in gillick)
        feats.add(new Feature("TokenContextCat_" + prevToken.text + "_" + nextToken.text));
        feats.add(new Feature("LeftWordRightCap", prevToken.text + "_" + (nextToken.text.length() > 0 && Character.isUpperCase(nextToken.text.charAt(0)))));

        return feats;
    }

    public List<Feature> getCharFeatures(char ch, String prefix) {
        List<Feature> feats = new ArrayList<>(6);
        List<String> featureNames = prefixMap.get(prefix);
        if (!charFeaturesCacheMap.containsKey(ch)) {
            Object id = (ch == '\n' ? "<LF>" : ch);
            boolean isUpper = Character.isUpperCase(ch);
            boolean isLower = Character.isLowerCase(ch);
            boolean isDigit = Character.isDigit(ch);
            boolean isWSpace = Character.isWhitespace(ch);
            feats.add(new Feature(featureNames.get(0), id));
            feats.add(new Feature(featureNames.get(1), isUpper));
            feats.add(new Feature(featureNames.get(2), isLower));
            feats.add(new Feature(featureNames.get(3), isDigit));
            feats.add(new Feature(featureNames.get(4), isWSpace));

            charFeaturesCacheMap.put(ch, Arrays.asList(id, isUpper, isLower, isDigit, isWSpace));
        } else {
            List<Object> cachedFeatures = charFeaturesCacheMap.get(ch);
            for (int i=0; i < featureNames.size(); i++) {
                String fname = featureNames.get(i);
                Object fvalue = cachedFeatures.get(i);
                feats.add(new Feature(fname, fvalue));
            }
        }
        feats.add(new Feature(prefix+"_Type"+Character.getType(ch), true));
        return feats;
    }

    public static AnalysisEngineDescription getDescription(String modelPath) throws ResourceInitializationException {
        return AnalysisEngineFactory.createEngineDescription(
                SentenceDetectorAnnotatorBIO.class,
                SentenceDetectorAnnotatorBIO.PARAM_IS_TRAINING,
                false,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                modelPath,
                SentenceDetectorAnnotatorBIO.PARAM_FEAT_CONFIG,
                SentenceDetectorAnnotatorBIO.FEAT_CONFIG.CHAR);
    }

    public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
        return getDescription("/org/apache/ctakes/core/sentdetect/model.jar");
    }

    private class WordCacheData {
        public int begin;
        public int end;
        public String text;

        public int length() {
            return end - begin;
        }
    }
}