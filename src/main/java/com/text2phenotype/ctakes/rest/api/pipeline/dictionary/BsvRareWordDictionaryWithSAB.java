package com.text2phenotype.ctakes.rest.api.pipeline.dictionary;

import com.text2phenotype.ctakes.rest.api.pipeline.concept.BsvConceptFactoryWithSAB;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.dictionary.MemRareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordTermMapCreator;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.ctakes.dictionary.lookup2.util.LookupUtil;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


/**
 * BSV dictionary which contains information about coding scheme (SAB).
 */
public class BsvRareWordDictionaryWithSAB implements RareWordDictionary {

    static private final Logger LOGGER = Logger.getLogger( BsvRareWordDictionaryWithSAB.class );

    static private final String CODE_MODE = "CODE";
    static private final String TERM_MODE = "TERM";

    static private final String BSV_FILE_PATH = "bsvPath";
    static private final String LOOK_UP_MODE = "LookUpMode";

    static private HashMap<String, RareWordDictionary> _delegateDictionaries = new HashMap<String, RareWordDictionary>();
    private String name, sourceKey;
    private final Set<String> lookUpMode = new HashSet<>();

    public BsvRareWordDictionaryWithSAB(final String name, final UimaContext uimaContext, final Properties properties ) {
        this( name, properties.getProperty( BSV_FILE_PATH ), properties.getProperty( LOOK_UP_MODE ) );
    }

    public BsvRareWordDictionaryWithSAB( final String name, final String bsvFilePath, final String lookUpMode ) {
    	this.name = name;
    	
    	synchronized(this) {
			if (bsvFilePath != null) {
				this.sourceKey = bsvFilePath;
			} else if (name != null) {
				this.sourceKey = name;
			} else {
				this.sourceKey = "delegateDictionary_" + _delegateDictionaries.size();
			}
			
			if (lookUpMode == null) {
		        this.lookUpMode.add(TERM_MODE);
		    } else {
		        String[] modes = lookUpMode.split("\\|");
		        this.lookUpMode.addAll(Arrays.asList(modes));
		        this.sourceKey += "_" + lookUpMode;
		    }
    	
	    	if (!_delegateDictionaries.containsKey(this.sourceKey)) {
	    		LOGGER.info("Building dictionary with: " + name + "; " + bsvFilePath + "; " + lookUpMode);
	    		
		        final Collection<RareWordTermMapCreator.CuiTerm> cuiTerms = new HashSet<>();
		
		        try {
		            File bsvFile = new File(FileLocator.getFullPath(bsvFilePath));
		
		            // if it is folder load all files with *.bsv extension
		            if (bsvFile.isDirectory()) {
		
		                for (String fileName: bsvFile.list(BsvConceptFactoryWithSAB.fileFilter)) {
		
		                    cuiTerms.addAll(parseBsvFile( bsvFilePath + fileName, this.lookUpMode));
		                }
		            } else {
		                cuiTerms.addAll(parseBsvFile( bsvFilePath, this.lookUpMode ));
		            }
		
		            //final Collection<RareWordTermMapCreator.CuiTerm> cuiTerms = parseBsvFile( bsvFilePath );
		            final CollectionMap<String, RareWordTerm, ? extends Collection<RareWordTerm>> rareWordTermMap
		                    = RareWordTermMapCreator.createRareWordTermMap( cuiTerms );
		            _delegateDictionaries.put(this.sourceKey, new MemRareWordDictionary( name, rareWordTermMap ));
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
	    	} else {
	    		LOGGER.info("Using existing dictionary with: " + name + "; " + bsvFilePath + "; " + lookUpMode);
	    	}
    	}
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<RareWordTerm> getRareWordHits(FastLookupToken fastLookupToken) {
        return _delegateDictionaries.get(sourceKey).getRareWordHits(fastLookupToken);
    }

    @Override
    public Collection<RareWordTerm> getRareWordHits(String rareWordText) {
        return _delegateDictionaries.get(sourceKey).getRareWordHits(rareWordText);
    }

    static private Collection<RareWordTermMapCreator.CuiTerm> parseBsvFile(final String bsvFilePath, Set<String> modes) {
        final Collection<RareWordTermMapCreator.CuiTerm> cuiTerms = new ArrayList<>();
        try ( final BufferedReader reader
                      = new BufferedReader( new InputStreamReader( FileLocator.getAsStream( bsvFilePath ) ) ) ) {
            String line = reader.readLine();
            while ( line != null ) {
                if ( line.startsWith( "//" ) || line.startsWith( "#" ) ) {
                    line = reader.readLine();
                    continue;
                }
                final String[] columns = LookupUtil.fastSplit( line, '|' );

                if (modes.contains(TERM_MODE)) {
                    final RareWordTermMapCreator.CuiTerm cuiTerm = createCuiTuiTerm( columns );
                    if ( cuiTerm != null ) {
                        cuiTerms.add( cuiTerm );

                    } else {
                        LOGGER.warn( "Bad BSV line " + line + " in " + bsvFilePath );
                    }
                }

                if (modes.contains(CODE_MODE)) {
                    final RareWordTermMapCreator.CuiTerm codeCuiTerm = createCuiTuiTermForCode( columns );
                    if ( codeCuiTerm != null ) {
                        cuiTerms.add( codeCuiTerm );
                    }
                }

                line = reader.readLine();
            }
        } catch ( IOException ioE ) {
            LOGGER.error( ioE.getMessage() );
        }
        return cuiTerms;
    }

    static private RareWordTermMapCreator.CuiTerm createCuiTuiTerm(final String... columns ) {
        if ( columns.length < 5 ) {
            return null;
        }
        final int cuiIndex = 0;
        int termIndex = 4;

        if ( columns[ cuiIndex ].trim().isEmpty() || columns[ termIndex ].trim().isEmpty() ) {
            return null;
        }
        final String cui = columns[ cuiIndex ];
        final String term = columns[ termIndex ].trim().toLowerCase();
        return new RareWordTermMapCreator.CuiTerm( cui, term );
    }

    static private RareWordTermMapCreator.CuiTerm createCuiTuiTermForCode(final String... columns ) {
        if ( columns.length < 5 ) {
            return null;
        }
        columns[4] = columns[2];
        final int cuiIndex = 0;
        int termIndex = 4;

        if ( columns[ cuiIndex ].trim().isEmpty() || columns[ termIndex ].trim().isEmpty() ) {
            return null;
        }
        final String cui = columns[ cuiIndex ];
        final String term = columns[ termIndex ].trim().toLowerCase();
        return new CuiTermForCode( cui, term );
    }

    /**
     * cuiTerm for `code` terms. The term is not tokenized in this case.
     */
    static public class CuiTermForCode extends RareWordTermMapCreator.CuiTerm {

        private String __term;
        private int __hashcode;

        public CuiTermForCode( final String cui, final String term ) {
            super(cui, term);
            __term = term;
            __hashcode = (getCui() + "_" + __term).hashCode();
        }

        @Override
        public String getTerm() {
            return __term;
        }

        private void setTerm(String term) {
            __term = term;
            __hashcode = (getCui() + "_" + __term).hashCode();
        }

        public boolean equals( final Object value ) {
            return value instanceof RareWordTermMapCreator.CuiTerm
                    && __term.equals( ((RareWordTermMapCreator.CuiTerm)value).getTerm() )
                    && getCui().equals( ((RareWordTermMapCreator.CuiTerm)value).getCui() );
        }

        public int hashCode() {
            return __hashcode;
        }
    }
}
