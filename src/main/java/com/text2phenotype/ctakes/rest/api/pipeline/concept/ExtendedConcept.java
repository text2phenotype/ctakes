package com.text2phenotype.ctakes.rest.api.pipeline.concept;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ExtendedSemanticUtil;
import gov.nih.nlm.nls.lvg.Util.Str;
import org.apache.ctakes.core.util.collection.ArrayListMap;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.core.util.collection.HashSetMap;
import org.apache.ctakes.core.util.collection.ImmutableCollectionMap;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.typesystem.type.constants.CONST;

import java.util.*;
import java.util.stream.Collectors;

public class ExtendedConcept implements Concept2 {

    final private String _cui;
    final private String _preferredText;
    final private CollectionMap<String, String, ? extends Collection<String>> _codes;
    final private Map<String, String> _params;
    final private Collection<Integer> _ctakesSemantics;

    final private int _hashcode;

    /**
     * @param cui -
     */
    public ExtendedConcept( final String cui ) {
        this( cui, "" );
    }

    /**
     * @param cui           -
     * @param preferredText -
     */
    public ExtendedConcept( final String cui, final String preferredText ) {
        this( cui, preferredText, new HashSetMap<>(), null );
    }
    
    /**
     * @param cui           -
     * @param preferredText -
     * @param codes         collection of coding scheme names and this concept's codes for those schemes
     */
    public ExtendedConcept( final String cui, final String preferredText,
                           final CollectionMap<String, String, ? extends Collection<String>> codes ) {
    	this(cui, preferredText, codes, null);
    }

    /**
     * @param cui           -
     * @param preferredText -
     * @param codes         collection of coding scheme names and this concept's codes for those schemes
     * @param params        collection of additional parameters
     */
    public ExtendedConcept( final String cui, final String preferredText,
                           final CollectionMap<String, String, ? extends Collection<String>> codes, Map<String, String> params ) {
        _params = params;
        _cui = cui;
        _preferredText = preferredText;
        _codes = new ImmutableCollectionMap<>( codes );
        // Attempt to obtain one or more valid type ids from the tuis of the term
        final Collection<Integer> ctakesSemantics = getCodes( TUI ).stream()
                .map( ExtendedSemanticUtil::getTuiSemanticGroupId )
                .collect( Collectors.toSet() );
        if ( ctakesSemantics.isEmpty() ) {
            ctakesSemantics.add( CONST.NE_TYPE_ID_UNKNOWN );
        }
        _ctakesSemantics = Collections.unmodifiableCollection( ctakesSemantics );
        _hashcode = (cui + "_" + preferredText).hashCode();
    }

    @Override
    public String getCui() {
        return _cui;
    }

    @Override
    public String getPreferredText() {
        if ( _preferredText != null ) {
            return _preferredText;
        }
        return PREFERRED_TERM_UNKNOWN;
    }

    @Override
    public Collection<String> getCodeNames() {
        return _codes.keySet();
    }

    @Override
    public Collection<String> getCodes( final String codeType ) {
        return _codes.getCollection( codeType );
    }

    @Override
    public Collection<Integer> getCtakesSemantics() {
        return _ctakesSemantics;
    }

    @Override
    public boolean isEmpty() {
        return (_preferredText == null || _preferredText.isEmpty()) && _codes.isEmpty();
    }

    @Override
    public boolean equals( final Object value ) {
        return (value instanceof Concept) && (_hashcode == ((ExtendedConcept)value)._hashcode);
    }

    @Override
    public int hashCode() {
        return _hashcode;
    }

    @Override
    public Map<String, String> getParams() {
        return _params;
    }
}
