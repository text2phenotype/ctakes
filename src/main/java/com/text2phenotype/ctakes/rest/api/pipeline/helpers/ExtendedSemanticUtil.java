package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import org.apache.ctakes.dictionary.lookup2.util.SemanticUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

final public class ExtendedSemanticUtil {

    private ExtendedSemanticUtil() {
        throw new AssertionError();
    }

    // additional types
    static private final String[] DRUG = { "T053" };
    static private final String[] LAB = { "T034", "T059", "T201" };
    static private final String[] ACTIVITY = { "T058" };

    static private final Collection<String> LAB_TUIS = new HashSet<>( Arrays.asList( LAB ) );
    static private final Collection<String> DRUG_TUIS = new HashSet<>( Arrays.asList( DRUG ) );
    static private final Collection<String> ACTIVITY_TUIS = new HashSet<>( Arrays.asList( ACTIVITY ) );

    /**
     * Sometimes a
     *
     * @param tui a comma-delimited collection of tuis that apply to some annotation
     * @return the cTakes group for the given tui
     */
    static public Integer getTuiSemanticGroupId( final String tui ) {
        if ( LAB_TUIS.contains( tui ) ) {
            return CONST.NE_TYPE_ID_LAB;
        } else if ( DRUG_TUIS.contains( tui ) ) {
            return CONST.NE_TYPE_ID_DRUG;
        } else if (ACTIVITY_TUIS.contains( tui )) {
            return com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST.NE_TYPE_ID_ACTIVITY;
        }
        return SemanticUtil.getTuiSemanticGroupId(tui);
    }

}
