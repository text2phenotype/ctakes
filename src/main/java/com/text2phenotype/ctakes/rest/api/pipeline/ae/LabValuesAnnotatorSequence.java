package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import java.util.*;

import org.apache.ctakes.typesystem.type.refsem.Lab;
import org.apache.ctakes.typesystem.type.refsem.LabValue;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.relation.ResultOfTextRelation;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
import org.apache.ctakes.typesystem.type.textsem.DateAnnotation;
import org.apache.ctakes.typesystem.type.textsem.FractionAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.MeasurementAnnotation;
import org.apache.ctakes.typesystem.type.textsem.RangeAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instances;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.SpecialLabValueWord;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitRelation;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;

/**
 * Lab values extraction annotator based on ML
 */
public class LabValuesAnnotatorSequence extends CleartkSequenceAnnotator<String> {
    private String NONE_CATEGORY = "NONE";
    private String LINKED_CATEGORY = "LINKED";

    private static final List<String> unitSymbols = Arrays.asList("%");

    private static final Logger LOGGER = Logger.getLogger(LabValuesAnnotatorSequence.class);
    private static final boolean DEBUG = LOGGER.isDebugEnabled();

    public static final Set<Class<? extends Annotation>> VALUE_CLASSES = new HashSet<Class<? extends Annotation>>(){{
            add(SpecialLabValueWord.class);
            add(RangeAnnotation.class);
            add(FractionAnnotation.class);
            add(NumToken.class);
            //UnitToken.class
    }};

    public static final Set<Class<? extends Annotation>> EXCLUDE_CLASSES = new HashSet<Class<? extends Annotation>>(){{
            add(DateAnnotation.class);
            add(TimeMention.class);
    }};

    public static final Set<Class<? extends Annotation>> TERM_CLASSES = new HashSet<Class<? extends Annotation>>(){{
        add(LabMention.class);
    }};

    private static final String PARAM_LAB_VALUE_WORDS = "labValueWords";
    @ConfigurationParameter(
            name = PARAM_LAB_VALUE_WORDS,
            mandatory = false,
            description = "Available lab values",
            defaultValue = {}
    )
    private Set<String> labValueWords;

    private static final String MAX_TOKEN_DISTANCE = "maxTokenDistance";
    @ConfigurationParameter(
            name = MAX_TOKEN_DISTANCE,
            mandatory = false,
            description = "Maximal distance between term and token",
            defaultValue = "15"
    )
    private int maxTokenDistance;

    private final int LOOKUP_KEY_ANNOTATIONS_COUNT = 5; // how many key annotations (terms or values) should be looked up for prediction
    private final int MAX_WINDOW_SIZE = 50; // maximum tokens count which should be used for prediction

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        try {
            // converting to lowercase
            if (!labValueWords.isEmpty()) {
                Set<String> lowerCaseValues = new HashSet<>(labValueWords.size());
                for (String value : labValueWords) {
                    lowerCaseValues.add(value.toLowerCase());
                }

                labValueWords = lowerCaseValues;
                
                if (DEBUG)  LOGGER.debug("labValueWords total number of words: " + labValueWords.size());
            }
        } catch (Exception e) {
        	LOGGER.error(e);
        	
            throw new ResourceInitializationException(e);
        }

    }

    private void addLabValue(JCas aJCas, LabMention labMention, Annotation valueAnnotation) {
        RelationArgument arg2 = new RelationArgument(aJCas);
        arg2.setArgument(valueAnnotation);
        labMention.getLabValue().setArg2(arg2);

        LabValue labValue = new LabValue(aJCas);

        if (valueAnnotation instanceof MeasurementAnnotation) {
            List<UnitToken> units = JCasUtil.selectCovered(aJCas, UnitToken.class, valueAnnotation);
            if (units.size() > 0) {
                UnitToken unit = units.get(0);
                labValue.setUnit(unit.getCoveredText());

                String fullText = valueAnnotation.getCoveredText();
                String onlyValueText = fullText.substring(0, unit.getBegin() - valueAnnotation.getBegin()).trim();
                labValue.setNumber(onlyValueText);
            } else {
                labValue.setNumber(valueAnnotation.getCoveredText());
            }
        } else {
            // try to get unit

            labValue.setNumber(valueAnnotation.getCoveredText());

            Boolean isFound = false;

            List<UnitToken> potentialUnits = JCasUtil.selectFollowing(aJCas, UnitToken.class, valueAnnotation, 1);

            if (potentialUnits.size() > 0) {
                UnitToken nextAnnotation = potentialUnits.get(0);
                if (JCasUtil.selectBetween(Annotation.class, valueAnnotation, nextAnnotation).size() == 0) {
                    labValue.setUnit(nextAnnotation.getCoveredText());

                    UnitRelation unitRel = new UnitRelation(aJCas);
                    RelationArgument valueArg = new RelationArgument(aJCas);
                    valueArg.setArgument(valueAnnotation);
                    RelationArgument unitArg = new RelationArgument(aJCas);
                    unitArg.setArgument(nextAnnotation);
                    unitRel.setArg1(valueArg);
                    unitRel.setArg2(unitArg);
                    unitRel.addToIndexes();

                    isFound = true;
                }
            }

            // check special symbols
            if (!isFound) {
                List<SymbolToken> potentialSymbols = JCasUtil.selectFollowing(aJCas, SymbolToken.class, valueAnnotation, 1);

                if (potentialSymbols.size() > 0) {
                    SymbolToken nextSymbol = potentialSymbols.get(0);
                    if (unitSymbols.contains(nextSymbol.getCoveredText()) && JCasUtil.selectBetween(Annotation.class, valueAnnotation, nextSymbol).size() == 0) {
                        labValue.setUnit(nextSymbol.getCoveredText());

                        UnitRelation unitRel = new UnitRelation(aJCas);
                        RelationArgument valueArg = new RelationArgument(aJCas);
                        valueArg.setArgument(valueAnnotation);
                        RelationArgument unitArg = new RelationArgument(aJCas);

                        UnitToken unitToken = new UnitToken(aJCas, nextSymbol.getBegin(), nextSymbol.getEnd());
                        unitToken.addToIndexes();

                        unitArg.setArgument(unitToken);
                        unitRel.setArg1(valueArg);
                        unitRel.setArg2(unitArg);
                        unitRel.addToIndexes();
                    }
                }
            }
        }

        Lab labEvent = new Lab(aJCas);
        labEvent.setLabValue(labValue);
        labMention.setEvent(labEvent);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    	final int hashCode = aJCas.hashCode();
    	
    	if (DEBUG)  LOGGER.debug("[" + hashCode + "] Processing...");
    	
        // <LabMention, List<Value>>
        Map<Annotation, Set<Annotation>> relationLookup = new HashMap<>();
        if (this.isTraining()) {
            for (BinaryTextRelation relation : JCasUtil.select(aJCas, BinaryTextRelation.class)) {
                Annotation lab = relation.getArg1().getArgument();
                Annotation val = relation.getArg2().getArgument();
                Set<Annotation> vals = relationLookup.getOrDefault(lab, new HashSet<>());
                vals.add(val);
                relationLookup.put(lab, vals);
            }
        }

        StringBuilder sb = null;
        if (labValueWords.size() > 0) {
            sb = new StringBuilder();
            sb.append(aJCas.getDocumentText().toLowerCase());
        }

        Set<BaseToken> tokensToRemove = new HashSet<>();
        for (Class<? extends Annotation> c: TERM_CLASSES) {
            tokensToRemove.addAll(JCasUtil.indexCovering(aJCas, BaseToken.class, c).keySet());
        }

        for (Class<? extends Annotation> c: EXCLUDE_CLASSES) {
            tokensToRemove.addAll(JCasUtil.indexCovering(aJCas, BaseToken.class, c).keySet());
        }

        for (Class<? extends Annotation> c: VALUE_CLASSES) {
            tokensToRemove.addAll(JCasUtil.indexCovering(aJCas, BaseToken.class, c).keySet());
        }
        
        if (DEBUG)  LOGGER.debug("Found " + tokensToRemove.size() + " tokens to remove.");

        Map<Sentence, Collection<Annotation>> coveredMap = JCasUtil.indexCovered(aJCas, Sentence.class, Annotation.class);
        
        if (DEBUG)  LOGGER.debug("[" + hashCode + "] Found " + coveredMap.keySet().size() + " sentences.");
        
        for (Sentence sent: coveredMap.keySet()) {
            // find special value words
            if (labValueWords.size() > 0) {
                for (String valueWord: labValueWords) {
                    int i = sb.indexOf(valueWord, sent.getBegin());
                    if ((i != -1) && (i <= sent.getEnd()-valueWord.length())) {

                        SpecialLabValueWord m = new SpecialLabValueWord(aJCas, i, i + valueWord.length());
                        m.addToIndexes();
                    }
                }
            }

            List<Annotation> annotations = new ArrayList<>(JCasUtil.selectCovered(aJCas, Annotation.class, sent));
            annotations.removeAll(tokensToRemove);

            createPriorityCache(aJCas, annotations, relationLookup);
        }
        
        if (DEBUG)  LOGGER.debug("[" + hashCode + "] Complete.");
    }

    private boolean checkMaximalDistance(List<Annotation> annotations, LabMention term, Annotation value) {
        Annotation first = term;
        Annotation second = value;
        if (value.getBegin() < term.getBegin()) {
            first = value;
            second = term;
        }

        int cursor = first.getEnd();
        int dist = 0;
        for (int i = annotations.indexOf(first) + 1; i < annotations.indexOf(second); ++i) {
            Annotation ann = annotations.get(i);
            if (ann.getEnd() > second.getBegin()) {
                break;
            }

            if (ann.getBegin() > cursor) {
                cursor = ann.getEnd();
                dist++;
            }
        }
        return dist <= maxTokenDistance;
    }

    private void createPriorityCache(JCas jcas, List<Annotation> annotations, Map<Annotation, Set<Annotation>> relationLookupForTraining) throws AnalysisEngineProcessException {

    	
        List<String> categoriesList = new ArrayList<>();

        // create potential pairs
        try {

            List<Integer> termsIdx = new ArrayList<>(annotations.size());
            List<Integer> valuesIdx = new ArrayList<>(annotations.size());
            List<List<Feature>> allFeatures = new ArrayList<>(annotations.size());
            List<int[]> pairs = new ArrayList<>(annotations.size());

            // index all terms and values
            for (int f = 0; f < annotations.size(); f++) {
                Annotation fwd = annotations.get(f);

                if (TERM_CLASSES.contains(fwd.getClass())) {
                    termsIdx.add(f);
                } else {
                    if (VALUE_CLASSES.contains(fwd.getClass())) {
                        valuesIdx.add(f);
                    }
                }
            }

            if (DEBUG) {
                LOGGER.debug("Creating priority cache for " + annotations.size() +
                        " annotations (" + termsIdx.size() + " terms, " + valuesIdx.size() + " values)...");
            }

            // make pairs
            for (int t : termsIdx) {
                int pairsCount = pairs.size();
                if (valuesIdx.size() > 0) {
                    int valuesWereUsed = 0;
                    for (int vIdn = 0; vIdn < valuesIdx.size(); vIdn++) {
                        int v = valuesIdx.get(vIdn);
                        if (Math.abs(t - v) <= MAX_WINDOW_SIZE) {
                            Annotation term = annotations.get(t);
                            Annotation value = annotations.get(v);
                            
                            List<Feature> features = extractFeatures(term, value, annotations);
                            if (features.size() == 0)  continue;
                            
                            pairs.add(pairsCount, new int[]{t, v});
                            allFeatures.add(pairsCount, features);

                            if (this.isTraining()) {

                                String category = NONE_CATEGORY;
                                if (relationLookupForTraining.containsKey(term)) {
                                    if (relationLookupForTraining.get(term).contains(value)) {
                                        category = LINKED_CATEGORY;
                                    }
                                }
                                categoriesList.add(pairsCount, category);
                            }
                            valuesWereUsed++;
                        }
                        if (valuesWereUsed > 2 * LOOKUP_KEY_ANNOTATIONS_COUNT) {
                            break;
                        }
                    }
                }
            }

            if (DEBUG)  LOGGER.debug("Created " + allFeatures.size() + " term/value candidates.");
            
            if (allFeatures.size() > 0) {

                if (this.isTraining()) {
                    this.dataWriter.write(Instances.toInstances(categoriesList, allFeatures));
                } else{
                    List<String> predictedCategories = this.classifier.classify(allFeatures);

                    for (int i = 0; i < predictedCategories.size(); i++) {
                        if (LINKED_CATEGORY.equals(predictedCategories.get(i))) {
                            Annotation term = annotations.get(pairs.get(i)[0]);
                            if (term instanceof LabMention) {
                                LabMention labMention = (LabMention) term;
                                Annotation value = annotations.get(pairs.get(i)[1]);

                                if (checkMaximalDistance(annotations, labMention, value)) {
                                    ResultOfTextRelation labValRel = new ResultOfTextRelation(jcas);
                                    RelationArgument arg1 = new RelationArgument(jcas);
                                    arg1.setArgument(labMention);
                                    labValRel.setArg1(arg1);
                                    labMention.setLabValue(labValRel);
                                    addLabValue(jcas, labMention, value);
                                }
                            }

                        }
                    }
                }

            }
        } catch (Exception e) {
        	LOGGER.error(e);
        	
            throw new AnalysisEngineProcessException(e);
        }


    }

    // features extracting
    public List<Feature> extractFeatures(Annotation term, Annotation value, List<Annotation> annotations) throws AnalysisEngineProcessException {
    	final int hashCode = annotations.hashCode();
    	
    	if (DEBUG)  LOGGER.debug("[" + hashCode + "] Extracting features for " + annotations.size() + " annotations...");
    	
        List<Feature> features = new ArrayList<>();

        // ensure that annotations are not overlapped
        int len = value.getEnd() - value.getBegin() + term.getEnd() - term.getBegin();
        int k = Math.max(term.getEnd(), value.getEnd()) - Math.min(term.getBegin() , value.getBegin());
        if (k < len) {
        	if (DEBUG)  LOGGER.debug("[" + hashCode + "] (" + k + " < " + len + ") Returning 0 features.");
        	
            return features;
        }

        Annotation first = term;
        Annotation second = value;

        int order = 1;
        if (second.getBegin() < first.getBegin()) {
            first = value;
            second = term;
            order = -1;
        }

        int distance = 0;
        int terms_between = 0;
        int values_between = 0;

        final String CPOS = "CC";

        for (Annotation current: annotations) {

            if (current.getBegin() < first.getEnd() || current == first) {
                continue;
            }

            if (current == second) {
                features.add(new Feature("LV_DISTANCE", distance * order));
                features.add(new Feature("LV_TERMS_BETWEEN", terms_between));
                features.add(new Feature("LV_VALUES_BETWEEN", values_between));
                features.add(new Feature("LV_IS_CLOSEST", terms_between + values_between == 0));
                break;
            }

            distance++;

            if (LabValuesAnnotatorSequence.VALUE_CLASSES.contains(current.getClass())) {
                values_between++;
                features.add(new Feature("LV_POINT", "VALUE_" + values_between));

            }
            else if (LabValuesAnnotatorSequence.TERM_CLASSES.contains(current.getClass())) {
                terms_between++;
                features.add(new Feature("LV_POINT", "TERM_" + terms_between));

            }
            else if (current instanceof BaseToken) {
                if (current instanceof NewlineToken) {
                    features.add(new Feature("LV_NEW_LINE", distance));
                    continue;
                }

                if (current instanceof PunctuationToken) {
                    features.add(new Feature("LV_PUNCTUATION", current.getCoveredText()));
                }

                BaseToken token = (BaseToken)current;
                String pos = token.getPartOfSpeech();
                if(pos == null) continue;
                if(pos.startsWith(CPOS)){
                    features.add(new Feature("LV_CONJ", current.getCoveredText().toUpperCase()));
                }

            }
        }
        
        if (DEBUG)  LOGGER.debug("[" + hashCode + "] Returning " + features.size() + " features.");
        
        return features;
    }

}
