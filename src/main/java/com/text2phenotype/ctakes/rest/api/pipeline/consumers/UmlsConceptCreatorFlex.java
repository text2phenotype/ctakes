package com.text2phenotype.ctakes.rest.api.pipeline.consumers;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.Concept2;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.consumer.UmlsConceptCreator;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.util.Pair;
import org.apache.ctakes.ytex.uima.types.KeyValuePair;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import java.util.ArrayList;
import java.util.Collection;

public class UmlsConceptCreatorFlex implements UmlsConceptCreator {

    @Override
    public Collection<UmlsConcept> createUmlsConcepts(JCas jcas, String codingScheme, String tui, Concept concept) {
        final Collection<UmlsConcept> concepts = new ArrayList<>();
        Concept2 extendedInterface = null;
        if (concept instanceof Concept2) {
            extendedInterface = (Concept2) concept;
        }
        for ( String codeName : concept.getCodeNames() ) {
            if ( codeName.equals( Concept.TUI ) ) {
                continue;
            }
            final Collection<String> codes = concept.getCodes( codeName );
            if ( codes == null || codes.isEmpty() ) {
                continue;
            }
            for ( String code : codes ) {
                concepts.add( createUmlsConcept( jcas, codeName, concept.getCui(), tui,
                        concept.getPreferredText(), code, extendedInterface ) );
            }
        }
        if ( concepts.isEmpty() ) {
            concepts.add( createUmlsConcept( jcas, codingScheme, concept.getCui(), tui,
                    concept.getPreferredText(), null, extendedInterface ) );
        }
        return concepts;
    }

    private UmlsConcept createUmlsConcept(final JCas jcas, final String codingScheme,
                                          final String cui, final String tui,
                                          final String preferredText, final String code, Concept2 extendedConcept) {
        final UmlsConceptFlex umlsConcept = new UmlsConceptFlex( jcas );
        umlsConcept.setCodingScheme( codingScheme );
        umlsConcept.setCui( cui );
        if ( tui != null ) {
            umlsConcept.setTui( tui );
        }
        if ( preferredText != null && !preferredText.isEmpty() ) {
            umlsConcept.setPreferredText( preferredText );
        }
        if ( code != null ) {
            umlsConcept.setCode( code );
        }
        if (extendedConcept != null && extendedConcept.getParams() != null && extendedConcept.getParams().size() > 0) {
            FSArray keyValuePairs = new FSArray(jcas, extendedConcept.getParams().size());

            int i=0;
            for (String paramName: extendedConcept.getParams().keySet()) {
                Pair pair = new Pair(jcas);
                pair.setAttribute(paramName);
                pair.setValue(extendedConcept.getParams().get(paramName));
                keyValuePairs.set(i, pair);
                i++;
            }
            umlsConcept.setParams(keyValuePairs);
        }
        return umlsConcept;
    }
}
