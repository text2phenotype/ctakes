package com.text2phenotype.ctakes.rest.api.pipeline.consumers;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.ActivityMention;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ExtendedSemanticUtil;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.consumer.*;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.dictionary.lookup2.util.CuiCodeUtil;
import org.apache.ctakes.dictionary.lookup2.util.SemanticUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.ctakes.typesystem.type.constants.CONST.*;
import static org.apache.ctakes.typesystem.type.constants.CONST.NE_TYPE_ID_LAB;
import static org.apache.ctakes.typesystem.type.constants.CONST.NE_TYPE_ID_PROCEDURE;

import static com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST.*;

/**
 * Default term consumer with filtering
 */
public class FilteredDefaultTermConsumer extends AbstractTermConsumer {

    public final String PARAM_TARGET_SEMANTICS = "targetSemantics";
    public final String PARAM_PRECISION_MODE = "precisionMode";

    private Set<String> targetSemantics = null;
    final private UmlsConceptCreator _umlsConceptCreator;

    /**
     * If True a collection of dictionary terms will be refined to only contain the most specific variations:
     * "colon cancer" instead of "cancer", performed by span inclusion /complete containment, not overlap
     */
    private boolean precisionMode = false;

    public FilteredDefaultTermConsumer(UimaContext uimaContext, Properties properties) {
        super(uimaContext, properties);

        _umlsConceptCreator = new UmlsConceptCreatorFlex();

        Object semanticsValue = properties.getProperty(PARAM_TARGET_SEMANTICS);
        if (semanticsValue != null) {
            String semanticsStr = semanticsValue.toString();
            if (!semanticsStr.isEmpty()) {
                String[] semantics = semanticsStr.split(",");
                targetSemantics = Arrays.stream(semantics).map(String::trim).collect(Collectors.toSet());
            }
        }

        Object precisionModeParam = properties.getProperty(PARAM_PRECISION_MODE);
        if (precisionModeParam != null) {
            precisionMode = Boolean.parseBoolean(precisionModeParam.toString());
        }
    }



    @Override
    public void consumeHits(JCas jcas, RareWordDictionary dictionary, CollectionMap<TextSpan, Long, ? extends Collection<Long>> textSpanCuis, CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConcepts) throws AnalysisEngineProcessException {
        if (targetSemantics != null) {
            for (Long cui: cuiConcepts.keySet()) {
                cuiConcepts.getCollection(cui).removeIf(concept -> !targetSemantics.containsAll(concept.getCodes(Concept.TUI)));
            }
        }

        super.consumeHits(jcas, dictionary, textSpanCuis, cuiConcepts);
    }

    @Override
    public void consumeTypeIdHits( final JCas jcas, final String codingScheme, final int cTakesSemantic,
                                   final CollectionMap<TextSpan, Long, ? extends Collection<Long>> textSpanCuis,
                                   final CollectionMap<Long, Concept, ? extends Collection<Concept>> cuiConcepts )
            throws AnalysisEngineProcessException {


        CollectionMap<TextSpan, Long, ? extends Collection<Long>> preciseTerms = textSpanCuis;
        if (precisionMode) {
            preciseTerms = PrecisionTermConsumer.createPreciseTerms( textSpanCuis );
        }

        // Collection of UmlsConcept objects
        final Collection<UmlsConcept> umlsConceptList = new ArrayList<>();
        try {
            for ( Map.Entry<TextSpan, ? extends Collection<Long>> spanCuis : preciseTerms ) {
                umlsConceptList.clear();
                for ( Long cuiCode : spanCuis.getValue() ) {
                    umlsConceptList.addAll(
                            createUmlsConcepts( jcas, codingScheme, cTakesSemantic, cuiCode, cuiConcepts ) );
                }
                final FSArray conceptArr = new FSArray( jcas, umlsConceptList.size() );
                int arrIdx = 0;
                for ( UmlsConcept umlsConcept : umlsConceptList ) {
                    conceptArr.set( arrIdx, umlsConcept );
                    arrIdx++;
                }
                final IdentifiedAnnotation annotation = createSemanticAnnotation( jcas, cTakesSemantic );
                annotation.setTypeID( cTakesSemantic );
                annotation.setBegin( spanCuis.getKey().getStart() );
                annotation.setEnd( spanCuis.getKey().getEnd() );
                annotation.setDiscoveryTechnique( CONST.NE_DISCOVERY_TECH_DICT_LOOKUP );
                annotation.setOntologyConceptArr( conceptArr );
                annotation.addToIndexes();
            }
        } catch ( CASRuntimeException crtE ) {
            // What is really thrown?  The jcas "throwFeatMissing" is not a great help
            throw new AnalysisEngineProcessException( crtE );
        }
    }

    static private IdentifiedAnnotation createSemanticAnnotation( final JCas jcas, final int cTakesSemantic ) {
        switch ( cTakesSemantic ) {
            case NE_TYPE_ID_DRUG: {
                return new MedicationMention( jcas );
            }
            case NE_TYPE_ID_ANATOMICAL_SITE: {
                return new AnatomicalSiteMention( jcas );
            }
            case NE_TYPE_ID_DISORDER: {
                return new DiseaseDisorderMention( jcas );
            }
            case NE_TYPE_ID_FINDING: {
                return new SignSymptomMention( jcas );
            }
            case NE_TYPE_ID_LAB: {
                return new LabMention( jcas );
            }
            case NE_TYPE_ID_PROCEDURE: {
                return new ProcedureMention( jcas );
            }
            case NE_TYPE_ID_ACTIVITY: {
                return new ActivityMention( jcas );
            }
        }
        return new EntityMention( jcas );
    }

    private Collection<UmlsConcept> createUmlsConcepts( final JCas jcas,
                                                        final String codingScheme,
                                                        final int cTakesSemantic,
                                                        final Long cuiCode,
                                                        final CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap ) {
        final Collection<Concept> concepts = conceptMap.getCollection( cuiCode );
        if ( concepts == null || concepts.isEmpty() ) {
            return Collections.singletonList( createSimpleUmlsConcept( jcas, codingScheme,
                    CuiCodeUtil.getInstance().getAsCui( cuiCode ) ) );
        }
        final Collection<UmlsConcept> umlsConcepts = new HashSet<>();
        for ( Concept concept : concepts ) {
            final Collection<Integer> allSemantics = concept.getCtakesSemantics();
            if ( !allSemantics.contains( cTakesSemantic ) ) {
                continue;
            }
            boolean added = false;
            final Collection<String> tuis = concept.getCodes( Concept.TUI );
            if ( !tuis.isEmpty() ) {
                for ( String tui : tuis ) {
                    // the concept could have tuis outside this cTakes semantic group
                    if ( ExtendedSemanticUtil.getTuiSemanticGroupId( tui ) == cTakesSemantic ) {
                        umlsConcepts.addAll( _umlsConceptCreator.createUmlsConcepts( jcas, codingScheme, tui, concept ) );
                        added = true;
                    }
                }
            }
            if ( !added ) {
                umlsConcepts.addAll( _umlsConceptCreator.createUmlsConcepts( jcas, codingScheme, null, concept ) );
            }
        }
        return umlsConcepts;
    }

    static private UmlsConcept createSimpleUmlsConcept( final JCas jcas, final String codingScheme, final String cui ) {
        final UmlsConcept umlsConcept = new UmlsConcept( jcas );
        umlsConcept.setCodingScheme( codingScheme );
        umlsConcept.setCui( cui );
        return umlsConcept;
    }
}
