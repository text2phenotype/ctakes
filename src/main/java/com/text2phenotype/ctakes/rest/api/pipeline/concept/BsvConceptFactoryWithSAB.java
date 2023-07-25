package com.text2phenotype.ctakes.rest.api.pipeline.concept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.ArrayListMap;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.core.util.collection.HashSetMap;
import org.apache.ctakes.dictionary.lookup2.concept.AbstractConceptFactory;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.util.CuiCodeUtil;
import org.apache.ctakes.dictionary.lookup2.util.LookupUtil;
import org.apache.ctakes.dictionary.lookup2.util.TuiCodeUtil;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;

/**
 * BSV concept factory for BSV with coding scheme
 */
public class BsvConceptFactoryWithSAB extends AbstractConceptFactory implements ConceptFactory2 {
    static private final Logger LOGGER = Logger.getLogger( BsvConceptFactoryWithSAB.class );
    static private final boolean DEBUG = LOGGER.isDebugEnabled();

    static private final String BSV_FILE_PATH = "bsvPath";

    private static HashMap<String, CollectionMap<Long, Concept, List<Concept>>> _conceptMaps = new HashMap<>();
    private String sourceKey;

    final public static FilenameFilter fileFilter = (dir, name) -> name.toLowerCase().endsWith("bsv");


    public BsvConceptFactoryWithSAB(final String name, final UimaContext uimaContext, final Properties properties ) {
        this( name, properties.getProperty( BSV_FILE_PATH ) );
    }
    
    public BsvConceptFactoryWithSAB( final String name, final String bsvFilePath ) {
        super( name );
    	
        synchronized(this) {
	    	if (bsvFilePath != null) {
	    		this.sourceKey = bsvFilePath;
	    	} else if (name != null) {
	    		this.sourceKey = name;
	    	} else {
	    		this.sourceKey = "conceptMap_" + _conceptMaps.size();
	    	}
    	
    		if (!_conceptMaps.containsKey(this.sourceKey)) {
    			LOGGER.info("Building concept factory with: " + name + "; " + bsvFilePath);
    			
		        final Collection<CuiTuiSABTerm> cuiTuiTerms = new ArrayList<>();
		        File bsvFile = null;
		        try {
		            bsvFile = new File(FileLocator.getFullPath(bsvFilePath));
		
		            // if it is folder load all files with *.bsv extension
		            if (bsvFile.isDirectory()) {
		
		                for (String fileName : bsvFile.list(BsvConceptFactoryWithSAB.fileFilter)) {
		
		                    cuiTuiTerms.addAll(parseBsvFile(bsvFilePath + fileName));
		                }
		            } else {
		                cuiTuiTerms.addAll(parseBsvFile(bsvFilePath));
		            }
		            //        final Collection<CuiTuiSABTerm> cuiTuiTerms = parseBsvFile( bsvFilePath );
		            CollectionMap<Long, Concept, List<Concept>>conceptMap = new ArrayListMap<>(cuiTuiTerms.size());
		            
		            CuiCodeUtil cuiUtil = CuiCodeUtil.getInstance();
		            for (CuiTuiSABTerm cuiTuiTerm : cuiTuiTerms) {
		                final CollectionMap<String, String, ? extends Collection<String>> codes
		                        = new HashSetMap<>();
		                codes.placeValue(Concept.TUI, TuiCodeUtil.getAsTui(cuiTuiTerm.getTui()));
		
		                codes.placeValue(cuiTuiTerm.getSAB(), cuiTuiTerm.getCode());
		
		
		                conceptMap.placeValue(cuiUtil.getCuiCode(cuiTuiTerm.getCui()),
		                        new ExtendedConcept(cuiTuiTerm.getCui(), cuiTuiTerm.getPrefTerm(), codes, null));
		            }
		            
		            _conceptMaps.put(this.sourceKey, conceptMap);
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
    		} else {
    			LOGGER.info("Using existing concept factory with: " + name + "; " + bsvFilePath);
    		}
    	}
    }

    @Override
    public Map<Long, Concept> createConcepts( final Collection<Long> cuiCodes ) {
    	long startTime = System.nanoTime();

        final Map<Long, Concept> conceptMap = new HashMap<>( cuiCodes.size() );
        final CollectionMap<Long, Concept, List<Concept>> conceptCollection = _conceptMaps.get(this.sourceKey);
        for ( Long cuiCode : cuiCodes ) {

            if (conceptCollection.containsKey(cuiCode)) {
                for (Concept concept : conceptCollection.getCollection(cuiCode)) {
                    conceptMap.put(cuiCode, concept);
                }
            }
        }
        
        if (DEBUG)  LOGGER.debug("createConcepts finished in " + ((System.nanoTime() - startTime) / 1000000) + "ms");
        
        return conceptMap;
    }

    public CollectionMap<Long, Concept, ? extends Collection<Concept>> createConceptsCollection( final Collection<Long> cuiCodes ) {
    	final CollectionMap<Long, Concept, List<Concept>> conceptCollection = _conceptMaps.get(this.sourceKey);
        final CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap = new HashSetMap<>( cuiCodes.size() );
//        final CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap = new ArrayListMap<>( cuiCodes.size() );
        for ( Long cuiCode : cuiCodes ) {

            if (conceptCollection.containsKey(cuiCode)) {

                Collection<Concept> conc = conceptCollection.get(cuiCode);
                for (Concept concept: conc) {

                    conceptMap.placeValue(cuiCode, concept);

                }
            }
        }
        return conceptMap;
    }

    @Override
    public Concept createConcept( final Long cuiCode ) {
        throw new AssertionError("Not implemented");
    }

    static private Collection<CuiTuiSABTerm> parseBsvFile(final String bsvFilePath ) {
        final Collection<CuiTuiSABTerm> cuiTuiTerms = new ArrayList<>();
        try ( final BufferedReader reader
                      = new BufferedReader( new InputStreamReader( FileLocator.getAsStream( bsvFilePath ) ) ) ) {
            String line = reader.readLine();
            while ( line != null ) {
                if ( line.startsWith( "//" ) || line.startsWith( "#" ) ) {
                    line = reader.readLine();
                    continue;
                }
                final String[] columns = LookupUtil.fastSplit( line, '|' );
                final CuiTuiSABTerm cuiTuiTerm = createCuiTuiTerm( columns );
                if ( cuiTuiTerm != null ) {
                    cuiTuiTerms.add( cuiTuiTerm );
                } else {
                    LOGGER.warn( "Bad BSV line " + line + " in " + bsvFilePath );
                }
                line = reader.readLine();
            }
        } catch ( IOException ioE ) {
            LOGGER.error( ioE.getMessage() );
        }
        return cuiTuiTerms;
    }

    static private CuiTuiSABTerm createCuiTuiTerm(final String... columns ) {
        if ( columns.length < 4 ) {
            return null;
        }
        final int cuiIndex = 0;
        final int tuiIndex = 1;
        final int codeIndex = 2;
        final int codingSchemeIndex = 3;
        int termIndex = 4; // use text by default
        if ( columns.length > 5 && !columns[5].trim().isEmpty()) {
            // if there is a special column for pref text use it
            termIndex = 5;
        }
        if ( columns[ cuiIndex ].trim().isEmpty() || columns[ tuiIndex ].trim().isEmpty() ) {
            return null;
        }
        final String cui = columns[ cuiIndex ];
        final String tui = (columns[ tuiIndex ].trim().isEmpty()) ? "T000" : columns[ tuiIndex ].trim();
        final String preferredTerm = columns[termIndex].trim();
        final String codingScheme = columns[ codingSchemeIndex ];
        final String code = columns[ codeIndex ];
        return new CuiTuiSABTerm( cui, tui, preferredTerm, codingScheme, code );
    }

    static public class CuiTuiSABTerm {

        final private String __cui;
        final private String __tui;
        final private String __prefTerm;
        final private String __sab;
        final private String __code;
        final private int __hashcode;


        public CuiTuiSABTerm( final String cui, final String tui, final String preferredTerm, final String sab, final String code ) {
            __cui = cui;
            __tui = tui;
            __prefTerm = preferredTerm;
            __sab = sab;
            __hashcode = (__cui + "_" + __tui + "_" + __prefTerm + "_" + __sab).hashCode();
            __code = code;
        }

        public CuiTuiSABTerm( final String cui, final String tui, final String preferredTerm, final String sab ) {
            this(cui, tui, preferredTerm, sab, "");
        }

        public String getCui() {
            return __cui;
        }

        public String getTui() {
            return __tui;
        }

        public String getPrefTerm() {
            return __prefTerm;
        }

        public String getSAB() {
            return __sab;
        }

        public String getCode() {
            return __code;
        }

        public boolean equals(final Object value ) {
            return value instanceof CuiTuiSABTerm
                    && __prefTerm.equals( ((CuiTuiSABTerm)value).__prefTerm )
                    && __tui.equals( ((CuiTuiSABTerm)value).__tui )
                    && __cui.equals( ((CuiTuiSABTerm)value).__cui )
                    && __sab.equals( ((CuiTuiSABTerm)value).__sab );
        }

        public int hashCode() {
            return __hashcode;
        }
    }
}
