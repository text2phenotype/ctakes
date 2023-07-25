package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.core.util.JCasUtil;
import org.apache.ctakes.necontexts.ContextAnalyzer;
import org.apache.ctakes.necontexts.ContextAnnotator;
import org.apache.ctakes.necontexts.ContextHit;
import org.apache.ctakes.necontexts.ContextHitConsumer;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.*;

public class FastContextAnnotator extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {
    // LOG4J logger based on class name
    private Logger iv_logger = Logger.getLogger(getClass().getName());

    /**
     * "MaxLeftScopeSize" is a required, single, integer parameter that
     * specifies the maximum size of the left scope.
     */
    public static final String MAX_LEFT_SCOPE_SIZE_PARAM = "MaxLeftScopeSize";
    /**
     * "MaxRightScopeSize" is a required, single, integer parameter that
     * specifies the maximum size of the right scope.
     */
    public static final String MAX_RIGHT_SCOPE_SIZE_PARAM = "MaxRightScopeSize";
    /**
     * "ScopeOrder" is a required, multiple, string parameter that specifies the
     * order that the scopes should be processed in. Possible values are "LEFT",
     * "MIDDLE", "RIGHT", and "ALL".
     */
    public static final String SCOPE_ORDER_PARAM = "ScopeOrder";

    /**
     * "WindowAnnotationClass" is a required, single, string parameter that
     * specifies the annotation type of the windows that specify the hard
     * boundaries of scopes. Note that the entire window may not be used
     * depending on the location of the focus annotation, the maximum scope
     * size, and the boundary conditions specified by the context analyzer. A
     * window encompasses all of the scopes (left, middle, and right). Examples
     * of likely window types would be:
     * <ul>
     * <li>DocumentAnnotation</li>
     * <li>SentenceAnnotation</li>
     * <li>SegmentAnnotation</li>
     * <li>...</li>
     * </ul>
     *
     * @see DocumentAnnotation
    //	 * @see edu.mayo.bmi.common.type.Sentence
    //	 * @see edu.mayo.bmi.common.type.Segment
     *
     */
    public static final String WINDOW_ANNOTATION_CLASS_PARAM = "WindowAnnotationClass";
    /**
     * "FocusAnnotationClass" is a required, single, string parameter that
     * specifies the annotation type of the focus annotations that are going to
     * be examined by this annotator. Examples of likely focus types would be:
     * <ul>
     * <li>NamedEntityAnnotation</li>
     * <li>Token</li>
     * <li>...</li>
     * </ul>
     *
     //	 * @see edu.mayo.bmi.common.type.NamedEntity
     //	 * @see edu.mayo.bmi.common.type.BaseToken
     */

    public static final String FOCUS_ANNOTATION_CLASS_PARAM = "FocusAnnotationClass";
    /**
     * "ContextAnnotationClass" is a required, single, string parameter that
     * specifies the annotation type of the context annotations (often "tokens")
     * that make up the context relative to a focus annotation within a scope
     * that is being examined. The context annotations are examined for context
     * hits by the context analyzer. Examples of likely focus types would be:
     * <ul>
     * <li>BaseToken</li>
     * <li>WordToken</li>
     * <li>NamedEntity</li>
     * </ul>
     *
     //	 * @see edu.mayo.bmi.common.type.BaseToken
     //	 * @see edu.mayo.bmi.common.type.WordToken
     //	 * @see edu.mayo.bmi.common.type.NamedEntity
     *
     */

    public static final String CONTEXT_ANNOTATION_CLASS_PARAM = "ContextAnnotationClass";

    /**
     * "ContextAnalyzerClass" is a required, single, string parameter that
     * specifies the context analyzer class that determines if a "hit" is found
     * within a processed scope.
     *
     * @see ContextAnalyzer
     */
    public static final String CONTEXT_ANALYZER_CLASS_PARAM = "ContextAnalyzerClass";
    /**
     * "ContextHitConsumerClass" is a required, single, string parameter that
     * specifies the context hit consumer class that will process context hits
     * that are found.
     *
     * @see ContextHitConsumer
     */
    public static final String CONTEXT_HIT_CONSUMER_CLASS_PARAM = "ContextHitConsumerClass";

    public static final int LEFT_SCOPE = 1;
    /**
     * Provides context annotations that are "inside" the focus annotation. For
     * example, if the focus annotation type is a named entity mention and the
     * context annotation is a token type, then the middle scope will examine
     * the tokens that fall within the named entity mention.
     */
    public static final int MIDDLE_SCOPE = 2;
    public static final int RIGHT_SCOPE = 3;
    /**
     * The ALL_SCOPE scope provides the context annotation that are found in all three of the other scopes (LEFT, MIDDLE, and RIGHT).
     */
    public static final int ALL_SCOPE = 4;

    @ConfigurationParameter( name = MAX_LEFT_SCOPE_SIZE_PARAM, mandatory = false,
            description = "", defaultValue = "7" )
    protected int leftScopeSize;

    @ConfigurationParameter( name = MAX_RIGHT_SCOPE_SIZE_PARAM, mandatory = false,
            description = "", defaultValue = "7" )
    protected int rightScopeSize;

    @ConfigurationParameter( name = SCOPE_ORDER_PARAM, mandatory = false,
            description = "", defaultValue = "LEFT,RIGHT" )
    private String _scopeOrder;

    @ConfigurationParameter( name = WINDOW_ANNOTATION_CLASS_PARAM, mandatory = false,
            description = "Type of Lookup window to use",
            defaultValue = "org.apache.ctakes.typesystem.type.textspan.Sentence" )
    private String windowClassName;

    @ConfigurationParameter( name = FOCUS_ANNOTATION_CLASS_PARAM, mandatory = false,
            description = "", defaultValue = "org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation" )
    private String focusClassName;

    @ConfigurationParameter( name = CONTEXT_ANNOTATION_CLASS_PARAM, mandatory = false,
            description = "", defaultValue = "org.apache.ctakes.typesystem.type.syntax.BaseToken" )
    private String contextClassName;

    @ConfigurationParameter( name = CONTEXT_ANALYZER_CLASS_PARAM, mandatory = false,
            description = "", defaultValue = "org.apache.ctakes.necontexts.negation.NegationContextAnalyzer" )
    private String contextAnalyzerClassName;

    @ConfigurationParameter( name = CONTEXT_HIT_CONSUMER_CLASS_PARAM, mandatory = false,
            description = "", defaultValue = "org.apache.ctakes.necontexts.negation.NegationContextHitConsumer" )
    private String contextConsumerClassName;


    protected List<Integer> scopes = new ArrayList<Integer>();

    protected ContextAnalyzer contextAnalyzer;
    protected ContextHitConsumer contextConsumer;

    int windowType;
    int focusType;
    int contextType;

    Class<Annotation> contextClass;

    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        try {
//			leftScopeSize = ((Integer) uimaContext.getConfigParameterValue(MAX_LEFT_SCOPE_SIZE_PARAM)).intValue();
//			rightScopeSize = ((Integer) uimaContext.getConfigParameterValue(MAX_RIGHT_SCOPE_SIZE_PARAM)).intValue();
            iv_logger.info( "Using left , right scope sizes: " + leftScopeSize + " , " + rightScopeSize );

//			String[] scopeOrderArr = (String[]) uimaContext.getConfigParameterValue(SCOPE_ORDER_PARAM);
//			parseScopeOrder(scopeOrderArr);
            iv_logger.info( "Using scope order: " + _scopeOrder );
            parseScopeOrder( _scopeOrder );

//			String contextAnalyzerClassName = (String) uimaContext.getConfigParameterValue(CONTEXT_ANALYZER_CLASS_PARAM);
//			String contextConsumerClassName = (String) uimaContext.getConfigParameterValue(CONTEXT_HIT_CONSUMER_CLASS_PARAM);
            iv_logger.info( "Using context analyzer: " + contextAnalyzerClassName );
            contextAnalyzer = (ContextAnalyzer) Class.forName(contextAnalyzerClassName).newInstance();
            contextAnalyzer.initialize(uimaContext);
            iv_logger.info( "Using context consumer: " + contextConsumerClassName );
            contextConsumer = (ContextHitConsumer) Class.forName(contextConsumerClassName).newInstance();

//			windowType = JCasUtil.getType((String) uimaContext.getConfigParameterValue(WINDOW_ANNOTATION_CLASS_PARAM));
            iv_logger.info( "Using lookup window type: " + windowClassName );
            windowType = JCasUtil.getType( windowClassName );
//			focusType = JCasUtil.getType((String) uimaContext.getConfigParameterValue(FOCUS_ANNOTATION_CLASS_PARAM));
            iv_logger.info( "Using focus type: " + focusClassName );
            focusType = JCasUtil.getType( focusClassName );
//			contextType = JCasUtil.getType((String) uimaContext.getConfigParameterValue(CONTEXT_ANNOTATION_CLASS_PARAM));
            iv_logger.info( "Using context type: " + contextClassName );
            contextType = JCasUtil.getType( contextClassName );

            contextClass = (Class<Annotation>)Class.forName(contextClassName);
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void parseScopeOrder( final String scopeString ) throws AnnotatorConfigurationException {
        parseScopeOrder( scopeString.split( "," ) );
    }

    void parseScopeOrder(String[] scopeStrings) throws AnnotatorConfigurationException {
        scopes.clear();
        for (int i = 0; i < scopeStrings.length; i++) {
            if (scopeStrings[i].equals("LEFT")) {
                scopes.add(new Integer(LEFT_SCOPE));
            } else if (scopeStrings[i].equals("MIDDLE")) {
                scopes.add(new Integer(MIDDLE_SCOPE));
            } else if (scopeStrings[i].equals("RIGHT")) {
                scopes.add(new Integer(RIGHT_SCOPE));
            } else if (scopeStrings[i].equals("ALL")) {
                scopes.add(new Integer(ALL_SCOPE));
            } else {
                Exception e = new Exception("Invalid scope value: " + scopeStrings[i]);
                throw new AnnotatorConfigurationException(e);
            }
        }
        iv_logger.info("SCOPE ORDER: " + scopes);
    }

    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            Class<Annotation> focusedType = (Class<Annotation>)Class.forName(focusClassName);
            FSIterator windowIterator = jCas.getAnnotationIndex(windowType).iterator();
            while (windowIterator.hasNext()) {
                Annotation window = (Annotation) windowIterator.next();
                FSIterator focusSubiterator = jCas.getAnnotationIndex(focusType).subiterator(window, true, false);
                focusSubiterator.moveToLast();
                // why is this list reversed?

                while (focusSubiterator.isValid()) {
                    Iterator<Integer> scopeIterator = scopes.iterator();
                    while (scopeIterator.hasNext()) {
                        int scope = scopeIterator.next();
                        Annotation focus = (Annotation)focusSubiterator.get();
                        List<Annotation> scopeContextAnnotations = getScopeContextAnnotations(jCas, focus, window,
                                scope);
                        ContextHit contextHit = contextAnalyzer.analyzeContext(scopeContextAnnotations, scope);
                        if (contextHit != null) {
                            contextConsumer.consumeHit(jCas, focus, scope, contextHit);
                        }
                    }
                    focusSubiterator.moveToPrevious();
                }
            }
        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    protected List<Annotation> getScopeContextAnnotations(JCas jCas, Annotation focus, Annotation window, int scope)
            throws AnalysisEngineProcessException {
        List<Annotation> scopeContextAnnotations = new ArrayList<Annotation>();

        List<Annotation> ctxItr = org.apache.uima.fit.util.JCasUtil.selectCovered(jCas, contextClass, focus);

        if (ctxItr.size() > 0) {
            Annotation leftFocus = ctxItr.get(0);
            Annotation rightFocus = ctxItr.get(ctxItr.size() - 1);

            switch (scope) {
                case LEFT_SCOPE:

                    scopeContextAnnotations = getLeftScopeContextAnnotations(jCas, leftFocus, window);

                    break;
                case MIDDLE_SCOPE:
                    scopeContextAnnotations = getMiddleScopeContextAnnotations(jCas, focus);
                    break;
                case RIGHT_SCOPE:
                    scopeContextAnnotations = getRightScopeContextAnnotations(jCas, rightFocus, window);
                    break;
                case ALL_SCOPE:
                    scopeContextAnnotations.addAll(getLeftScopeContextAnnotations(jCas, leftFocus, window));
                    scopeContextAnnotations.addAll(getMiddleScopeContextAnnotations(jCas, focus));
                    scopeContextAnnotations.addAll(getRightScopeContextAnnotations(jCas, rightFocus, window));
                    break;
            }
        }
        return scopeContextAnnotations;
    }

    protected List<Annotation> getLeftScopeContextAnnotations(JCas jCas, Annotation focus, Annotation window)
            throws AnalysisEngineProcessException {

        List<Annotation> scopeContextAnnotations = new ArrayList<Annotation>();

        // if focus is not completely contained inside the window annotation,
        // then return empty list.
        if (focus.getBegin() < window.getBegin() || focus.getEnd() > window.getEnd())
            return scopeContextAnnotations;
        FSIterator subiterator = jCas.getAnnotationIndex(contextType).subiterator(window, true, false);
        subiterator.moveTo(focus);
        subiterator.moveToNext();
        if (!subiterator.isValid())
            subiterator.moveTo(focus);

        while (scopeContextAnnotations.size() < leftScopeSize) {
            subiterator.moveToPrevious();
            if (subiterator.isValid()) {
                Annotation contextAnnotation = (Annotation) subiterator.get();
                if (contextAnnotation.getEnd() > focus.getBegin()) {
                    continue;
                }
                if (!contextAnalyzer.isBoundary(contextAnnotation, LEFT_SCOPE)) {
                    scopeContextAnnotations.add(contextAnnotation);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        Collections.reverse(scopeContextAnnotations);
        return scopeContextAnnotations;
    }

    protected List<Annotation> getRightScopeContextAnnotations(JCas jCas, Annotation focus, Annotation window)
            throws AnalysisEngineProcessException {

        List<Annotation> scopeContextAnnotations = new ArrayList<Annotation>();

        // if focus is not completely contained inside the window annotation,
        // then return empty list.
        if (focus.getBegin() < window.getBegin() || focus.getEnd() > window.getEnd())
            return scopeContextAnnotations;

        FSIterator subiterator = jCas.getAnnotationIndex(contextType).subiterator(window, true, false);
        subiterator.moveTo(focus);
        subiterator.moveToPrevious();
        if (!subiterator.isValid())
            subiterator.moveTo(focus);

        while (scopeContextAnnotations.size() < rightScopeSize) {
            subiterator.moveToNext();
            if (subiterator.isValid()) {
                Annotation contextAnnotation = (Annotation) subiterator.get();
                if (contextAnnotation.getBegin() < focus.getEnd()) {
                    continue;
                }
                if (!contextAnalyzer.isBoundary(contextAnnotation, RIGHT_SCOPE)) {
                    scopeContextAnnotations.add(contextAnnotation);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return scopeContextAnnotations;
    }

    protected List<Annotation> getMiddleScopeContextAnnotations(JCas jCas, Annotation focus)
            throws AnalysisEngineProcessException {

        List<Annotation> scopeContextAnnotations = new ArrayList<Annotation>();

        FSIterator subiterator = jCas.getAnnotationIndex(contextType).subiterator(focus, true, false);
        while (subiterator.hasNext()) {
            scopeContextAnnotations.add((Annotation) subiterator.next());
        }
        if (scopeContextAnnotations.size() == 0 && JCasUtil.getType(focus.getClass()) == contextType)
            scopeContextAnnotations.add(focus);
        else if (scopeContextAnnotations.size() == 0) {
            TypeSystem typeSystem = jCas.getTypeSystem();
            Type superType = jCas.getType(focusType).casType;
            Type subType = focus.getType();
            if (typeSystem.subsumes(superType, subType))
                scopeContextAnnotations.add(focus);
        }
        return scopeContextAnnotations;
    }

    /**
     * Gets a list of annotations within the specified window annotation.
     *
     //	 * @param annotItr
     * @param jCas
     * @param window
     * @return
     * @throws Exception
     */
    private List<Annotation> constrainToWindow(JCas jCas, int type, Annotation window) {

        List<Annotation> list = new ArrayList<Annotation>();

        FSIterator subiterator = jCas.getAnnotationIndex(type).subiterator(window);

        while (subiterator.hasNext()) {
            Annotation annot = (Annotation) subiterator.next();
            list.add(annot);
        }
        return list;
    }


    static public AnalysisEngineDescription createAnnotatorDescription() throws ResourceInitializationException {
        return AnalysisEngineFactory.createEngineDescription( ContextAnnotator.class );
    }


}