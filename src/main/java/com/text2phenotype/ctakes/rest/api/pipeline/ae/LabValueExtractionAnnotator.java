//package com.text2phenotype.ctakes.rest.api.pipeline.ae;
//
//import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitAnnotation;
//import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitRelation;
//import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;
//import com.text2phenotype.ctakes.rest.api.pipeline.helpers.AnnotationUtils;
//import opennlp.tools.util.StringUtil;
//import org.apache.ctakes.typesystem.type.refsem.Lab;
//import org.apache.ctakes.typesystem.type.refsem.LabValue;
//import org.apache.ctakes.typesystem.type.relation.RelationArgument;
//import org.apache.ctakes.typesystem.type.relation.ResultOfTextRelation;
//import org.apache.ctakes.typesystem.type.syntax.BaseToken;
//import org.apache.ctakes.typesystem.type.syntax.NumToken;
//import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
//import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
//import org.apache.ctakes.typesystem.type.textsem.*;
//import org.apache.ctakes.typesystem.type.textspan.Sentence;
//import org.apache.log4j.Logger;
//import org.apache.uima.UimaContext;
//import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
//import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
//import org.apache.uima.fit.descriptor.ConfigurationParameter;
//import org.apache.uima.fit.util.JCasUtil;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.jcas.tcas.Annotation;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.springframework.util.StringUtils;
//
//import java.util.*;
//
///**
// * Annotator for extraction of Lab Values
// */
//public class LabValueExtractionAnnotator extends JCasAnnotator_ImplBase {
//
//    private static final Logger LOGGER = Logger.getLogger(LabValueExtractionAnnotator.class);
//    private static  final List<String> FORBIDDEN_PUNCTUATION = Arrays.asList(";", ",");
//    private static final String PARAM_LAB_VALUE_WORDS = "labValueWords";
//
//    private static final int MAX_TOKEN_DICTANCE = 3;
//    private static final List<String> unitSymbols = Arrays.asList("%");
//
//
//    @ConfigurationParameter(
//            name = PARAM_LAB_VALUE_WORDS,
//            mandatory = false,
//            description = "Available lab values",
//            defaultValue = {}
//    )
//    private Set<String> labValueWords;
//
//    // classes constants
//
//    private final List<Class<? extends Annotation>> valueClasses = Arrays.asList(
//            MeasurementAnnotation.class,
//            RangeAnnotation.class,
//            FractionAnnotation.class,
//            NumToken.class
//    );
//
//    private final List<Class<? extends Annotation>> allAnnotationClasses = Arrays.asList(
//            DateAnnotation.class,
//            TimeAnnotation.class,
//            RangeAnnotation.class,
//            FractionAnnotation.class,
//            LabMention.class,
//            //UnitAnnotation.class
//            UnitToken.class
//    );
//
//    // comparators
//    private static Comparator<Annotation> startEndComparator = (lm1, lm2) -> {
//        int beginSortResult = Integer.compare(lm1.getBegin(), lm2.getBegin());
//        return (beginSortResult != 0) ? beginSortResult : Integer.compare(lm1.getEnd(), lm2.getEnd());
//    };
//
//    @Override
//    public void initialize(UimaContext aContext) throws ResourceInitializationException {
//        super.initialize(aContext);
//        try {
//            // converting to lowercase
//            if (!labValueWords.isEmpty()) {
//                Set<String> lowerCaseValues = new HashSet<>(labValueWords.size());
//                for (String value : labValueWords) {
//                    lowerCaseValues.add(value.toLowerCase());
//                }
//
//                labValueWords = lowerCaseValues;
//            }
//
//
//        } catch (Exception e) {
//            throw new ResourceInitializationException(e);
//        }
//    }
//
//    private void addLabValue(JCas aJCas, LabMention labMention, Annotation valueAnnotation) {
//        RelationArgument arg2 = new RelationArgument(aJCas);
//        arg2.setArgument(valueAnnotation);
//        labMention.getLabValue().setArg2(arg2);
//
//        LabValue labValue = new LabValue(aJCas);
//
//        if (valueAnnotation instanceof MeasurementAnnotation) {
//            List<UnitToken> units = JCasUtil.selectCovered(aJCas, UnitToken.class, valueAnnotation);
//            if (units.size() > 0) {
//                UnitToken unit = units.get(0);
//                labValue.setUnit(unit.getCoveredText());
//
//                String fullText = valueAnnotation.getCoveredText();
//                String onlyValueText = fullText.substring(0, unit.getBegin() - valueAnnotation.getBegin()).trim();
//                labValue.setNumber(onlyValueText);
//            } else {
//                labValue.setNumber(valueAnnotation.getCoveredText());
//            }
//        } else {
//            // try to get unit
//
//            labValue.setNumber(valueAnnotation.getCoveredText());
//
//            Boolean isFound = false;
//
//            List<UnitToken> potentialUnits = JCasUtil.selectFollowing(aJCas, UnitToken.class, valueAnnotation, 1);
//
//            if (potentialUnits.size() > 0) {
//                UnitToken nextAnnotation = potentialUnits.get(0);
//                if (JCasUtil.selectBetween(Annotation.class, valueAnnotation, nextAnnotation).size() == 0) {
//                    labValue.setUnit(nextAnnotation.getCoveredText());
//
//                    UnitRelation unitRel = new UnitRelation(aJCas);
//                    RelationArgument valueArg = new RelationArgument(aJCas);
//                    valueArg.setArgument(valueAnnotation);
//                    RelationArgument unitArg = new RelationArgument(aJCas);
//                    unitArg.setArgument(nextAnnotation);
//                    unitRel.setArg1(valueArg);
//                    unitRel.setArg2(unitArg);
//                    unitRel.addToIndexes();
//
//                    isFound = true;
//                }
//            }
//
//            // check special symbols
//            if (!isFound) {
//                List<SymbolToken> potentialSymbols = JCasUtil.selectFollowing(aJCas, SymbolToken.class, valueAnnotation, 1);
//
//                if (potentialSymbols.size() > 0) {
//                    SymbolToken nextSymbol = potentialSymbols.get(0);
//                    if (unitSymbols.contains(nextSymbol.getCoveredText()) && JCasUtil.selectBetween(Annotation.class, valueAnnotation, nextSymbol).size() == 0) {
//                        labValue.setUnit(nextSymbol.getCoveredText());
//
//                        UnitRelation unitRel = new UnitRelation(aJCas);
//                        RelationArgument valueArg = new RelationArgument(aJCas);
//                        valueArg.setArgument(valueAnnotation);
//                        RelationArgument unitArg = new RelationArgument(aJCas);
//                        unitArg.setArgument(nextSymbol);
//                        unitRel.setArg1(valueArg);
//                        unitRel.setArg2(unitArg);
//                        unitRel.addToIndexes();
//                    }
//                }
//            }
//        }
//
//        Lab labEvent = new Lab(aJCas);
//        labEvent.setLabValue(labValue);
//        labMention.setEvent(labEvent);
//    }
//
//    private boolean isValue(Annotation annotation) {
//        return valueClasses.contains(annotation.getClass());
//    }
//
//    private boolean isTerm(Annotation annotation) {
//        return annotation instanceof LabMention;
//    }
//
//    @Override
//    public void process(JCas aJCas) throws AnalysisEngineProcessException {
//
//        LOGGER.info("Start processing");
//
//
//        AnnotationUtils.mergeIdentifiedAnnotations(aJCas, new ArrayList<>(JCasUtil.select(aJCas, LabMention.class)));
//
//        Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);
//
//        Map<Annotation, Collection<Annotation>> overlappedAnnotations = createCoveringMap(aJCas, Arrays.asList(BaseToken.class), allAnnotationClasses);
////        Map<Annotation, Collection<IdentifiedAnnotation>> overlappedAnnotations = createCoveringMap(aJCas, Arrays.asList(BaseToken.class), Arrays.asList(LabMention.class));
//
//
//        StringBuilder sb = null;
//        if (labValueWords.size() > 0) {
//            sb = new StringBuilder();
//            sb.append(aJCas.getDocumentText().toLowerCase());
//        }
//
//        for (Sentence sentence : sentences){
//
//            SortedSet<Annotation> annotations = new TreeSet<>(startEndComparator);
//
//            // find special value words
//            if (labValueWords.size() > 0) {
//                for (String valueWord: labValueWords) {
//                    int i = sb.indexOf(valueWord, sentence.getBegin());
//                    if ((i != -1) && (i <= sentence.getEnd()-valueWord.length())) {
//                        annotations.add(new MeasurementAnnotation(aJCas, i, i + valueWord.length()));
//                    }
//                }
//            }
//
//            annotations.addAll(JCasUtil.selectCovered(aJCas, BaseToken.class, sentence));
//
//            Iterator<Annotation> annotationIterator = annotations.iterator();
//            Set<Annotation> annotationsToAdd = new HashSet<>();
//            while (annotationIterator.hasNext()) {
//                Annotation a = annotationIterator.next();
//                if (overlappedAnnotations.containsKey(a) && overlappedAnnotations.get(a).size() > 0){
//                    annotationIterator.remove();
//                    annotationsToAdd.addAll(overlappedAnnotations.get(a));
//                }
//            }
//
//            annotations.addAll(annotationsToAdd);
//
//            // create priority cache
//            Map<Annotation, SortedSet<AnnotationNode>> terms = new HashMap<>();
//            Map<Annotation, SortedSet<AnnotationNode>> values = new HashMap<>();
//            Annotation previousAnnotation = null;
//            int priority=0;
//            for (Annotation currentAnnotation : annotations) {
//
//                if (isTerm(currentAnnotation) || isValue(currentAnnotation)) {
//
//                    if (previousAnnotation != null) {
//
//                        Annotation termAnnotation = null;
//                        Annotation valueAnnotation = null;
//
//                        if (isTerm(currentAnnotation) && isValue(previousAnnotation)){
//                            termAnnotation = currentAnnotation;
//                            valueAnnotation = previousAnnotation;
//                        }
//
//                        if (isValue(currentAnnotation) && isTerm(previousAnnotation)){
//                            termAnnotation = previousAnnotation;
//                            valueAnnotation = currentAnnotation;
//                        }
//
//                        if (termAnnotation!= null && valueAnnotation != null) {
//                            SortedSet<AnnotationNode> termsPriorities = values.getOrDefault(valueAnnotation, new TreeSet<>());
//                            termsPriorities.add(new AnnotationNode(termAnnotation, priority));
//                            values.put(valueAnnotation, termsPriorities);
//
//                            SortedSet<AnnotationNode> valuesPriorities = terms.getOrDefault(termAnnotation, new TreeSet<>());
//                            valuesPriorities.add(new AnnotationNode(valueAnnotation, priority));
//                            terms.put(termAnnotation, valuesPriorities);
//                        }
//                    }
//
//                    previousAnnotation = currentAnnotation;
//                    priority = 0;
//
//                } else {
//                    if (currentAnnotation instanceof BaseToken) {
//                        BaseToken token = (BaseToken)currentAnnotation;
//                        if (((currentAnnotation instanceof PunctuationToken) && FORBIDDEN_PUNCTUATION.contains(currentAnnotation.getCoveredText())) || priority > MAX_TOKEN_DICTANCE) {
//                            previousAnnotation = null;
//                            priority = 0;
//                        } else {
//                            priority++;
//
//                            // decrease the priority for conjunctions
//                            if (!token.getPartOfSpeech().startsWith("CC")) {
//                                priority--;
//                            }
//                        }
//                    }
//                }
//
//            }
//
//            // Map<Term, Value>
//            Map<Annotation, Annotation> pairs = new HashMap<>();
//
//            // go iterations
//            while (terms.size() > 0) {
//                // Map<Value, Terms>
//                Map<Annotation, SortedSet<AnnotationNode>> potentialPairs = new HashMap<>();
//
//                // find potential pairs
//                Iterator<Annotation> termItr = terms.keySet().iterator();
//                while (termItr.hasNext()) {
//                    Annotation term = termItr.next();
//                    SortedSet<AnnotationNode> q = terms.get(term);
//                    if (q.isEmpty()) {
//                        termItr.remove();
//                        continue;
//                    }
//
//                    // get the nearest value and create potential pair
//                    AnnotationNode valueNode = q.first();
//                    if (!potentialPairs.containsKey(valueNode.getKey())) {
//                        potentialPairs.put(valueNode.getKey(), new TreeSet<>());
//                    }
//
//                    AnnotationNode termNode = null;
//                    if (values.containsKey(valueNode.getKey())) {
//                        Set<AnnotationNode> s = values.get(valueNode.getKey());
//                        for (AnnotationNode node : s) {
//                            if (node.getKey() == term) {
//                                termNode = node;
//                                break;
//                            }
//                        }
//                    } else {
//                        termItr.remove();
//                        continue;
//                    }
//
//                    if (termNode != null) {
//                        potentialPairs.get(valueNode.getKey()).add(termNode);
//                    }
//                }
//
//                // check potential pairs
//                Iterator<Annotation> valueItr = potentialPairs.keySet().iterator();
//                while (valueItr.hasNext()) {
//                    Annotation value = valueItr.next();
//                    SortedSet<AnnotationNode> offers = potentialPairs.get(value);
//                    if (offers.size() == 1) { // create a pair with single term
//                        Annotation term = offers.first().getKey();
//                        pairs.put(term, value);
//                        terms.remove(term);
//                        values.remove(value);
//                    } else {
//                        if (offers.size() > 1) { // create a pair with the nearest term
//
//                            // find nearest (first) of offers by priority
//                            AnnotationNode nearestTermNode = values.get(value)
//                                    .stream()
//                                    .filter(offers::contains)
//                                    .findFirst()
//                                    .orElse(null);
//
//                            if (nearestTermNode != null) {
//                                pairs.put(nearestTermNode.getKey(), value);
//
//
//                                // remove links to the value
//                                for (AnnotationNode termNode: values.get(value)) {
//                                    terms.get(termNode.getKey()).removeIf(valueNode -> valueNode.getKey() == value);
//                                }
//
//
//                                terms.remove(nearestTermNode.getKey());
//                                values.remove(value);
//
//
//                            }
//                        }
//                    }
//                }
//            }
//
//            List<LabMention> labMentions = JCasUtil.selectCovered(aJCas, LabMention.class, sentence);
//
//            for (LabMention labMention: labMentions) {
//
//                if (pairs.containsKey(labMention)) {
//                    Annotation value = pairs.get(labMention);
//                    ResultOfTextRelation labValRel = new ResultOfTextRelation(aJCas);
//                    RelationArgument arg1 = new RelationArgument(aJCas);
//                    arg1.setArgument(labMention);
//                    labValRel.setArg1(arg1);
//                    labMention.setLabValue(labValRel);
//                    addLabValue(aJCas, labMention, value);
//                } else {
////                    labMention.removeFromIndexes();
//                }
//
//            }
//        }
//
//        LOGGER.info("End processing");
//    }
//
//    /**
//     * Creates covering map
//     * @param jCas
//     * @param coveredClasses
//     * @param coveringClasses
//     * @return
//     */
//    private Map<Annotation, Collection<Annotation>> createCoveringMap(JCas jCas,
//                                                                    List<Class<? extends Annotation>> coveredClasses,
//                                                                    List<Class<? extends Annotation>> coveringClasses) {
//
//        Map<Annotation, Collection<Annotation>> result = new HashMap<>();
//        for (Class<? extends Annotation> covered: coveredClasses) {
//            for (Class<? extends Annotation> covering: coveringClasses) {
//                result.putAll(JCasUtil.indexCovering(jCas, covered, covering));
//            }
//        }
//
//        Map<Annotation, Collection<Annotation>> superCovering = new HashMap<>();
//        for (Class<? extends Annotation> covered: coveringClasses) {
//            for (Class<? extends Annotation> covering: coveringClasses) {
//                superCovering.putAll(JCasUtil.indexCovering(jCas, covered, covering));
//            }
//        }
//
//        for (Annotation key: result.keySet()) {
//            Collection<Annotation> values = result.get(key);
//            List<Annotation> valuesToRemove = new ArrayList<>();
//            for (Annotation value : values) {
//                if (superCovering.containsKey(value)) {
//                    valuesToRemove.add(value);
//                }
//            }
//
//            values.removeAll(valuesToRemove);
//        }
//
//        return result;
//    }
//
//    /**
//     * Helper for the sorting of annotations
//     */
//    private class AnnotationNode implements Map.Entry<Annotation, Integer>, Comparable<AnnotationNode> {
//
//        private int left = Integer.MAX_VALUE;
//        private int right = Integer.MAX_VALUE;
//
//        private Annotation key;
//        private int value;
//
//        public AnnotationNode(Annotation annotation, int priority) {
//            key = annotation;
//            value = priority;
//        }
//
//        @Override
//        public Annotation getKey() {
//            return key;
//        }
//
//        @Override
//        public Integer getValue() {
//            return value;
//        }
//
//        @Override
//        public Integer setValue(Integer value) {
//            this.value = value;
//            return value;
//        }
//
//        @Override
//        public int compareTo(AnnotationNode o) {
//            int ind = Integer.compare(this.value, o.getValue());
//            return ind != 0 ? ind : Integer.compare(o.getKey().getBegin(), key.getBegin());
//        }
//    }
//}
