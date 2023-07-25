package com.text2phenotype.ctakes.rest.api.pipeline.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

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

//import com.text2phenotype.ctakes.rest.api.pipeline.concept.BsvConceptFactoryWithSAB;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.BsvConceptFactoryFlex;

public class BsvRareWordDictionaryFlex implements RareWordDictionary {

    static private final Logger LOGGER = Logger.getLogger( BsvRareWordDictionaryFlex.class );

    static private final String BSV_FILE_PATH = "bsvPath";
    static private final String CUI_COLUMN_UNDEX = "CUI";
    static private final String TERM_COLUMN_UNDEX = "TERM";

    static private HashMap<String, RareWordDictionary> _delegateDictionaries = new HashMap<String, RareWordDictionary>();
    private RareWordDictionary _delegateDictionary;
    private String name;
    private int cuiIndex = 0;
    private int termIndex = 4;

    public BsvRareWordDictionaryFlex(final String name, final UimaContext uimaContext, final Properties properties ) {
    	this.name = name;
        final String bsvFilePath = properties.getProperty( BSV_FILE_PATH );

        if (!properties.getProperty(CUI_COLUMN_UNDEX, "").isEmpty()) {
            cuiIndex = Integer.parseInt(properties.getProperty(CUI_COLUMN_UNDEX));
        }

        if (!properties.getProperty(TERM_COLUMN_UNDEX, "").isEmpty()) {
            termIndex = Integer.parseInt(properties.getProperty(TERM_COLUMN_UNDEX));
        }

        synchronized(this) {
	        String sourceKey = "" + cuiIndex + "_" + termIndex + "_";
	        if (bsvFilePath != null) {
	    		sourceKey += bsvFilePath;
	    	} else if (name != null) {
	    		sourceKey += name;
	    	} else {
	    		sourceKey += "delegateDictionary_" + _delegateDictionaries.size();
	    	}

	    	if (!_delegateDictionaries.containsKey(sourceKey)) {
	    		LOGGER.info("Building dictionary with: " + name + "; " + bsvFilePath);
	    		
	    		final Collection<RareWordTermMapCreator.CuiTerm> cuiTerms = new HashSet<>();

	            File bsvFile = null;
	            try {
	                bsvFile = new File(FileLocator.getFullPath(bsvFilePath));

	                // if it is folder load all files with *.bsv extension
	                if (bsvFile.isDirectory()) {

	                    for (String fileName: bsvFile.list(BsvConceptFactoryFlex.fileFilter)) {
	                        cuiTerms.addAll(parseBsvFile( bsvFilePath + fileName));
	                    }
	                } else {
	                    cuiTerms.addAll(parseBsvFile( bsvFilePath ));
	                }
	                
	                LOGGER.info("Parsed " + cuiTerms.size() + " CUI terms.");

	                //final Collection<RareWordTermMapCreator.CuiTerm> cuiTerms = parseBsvFile( bsvFilePath );
	                final CollectionMap<String, RareWordTerm, ? extends Collection<RareWordTerm>> rareWordTermMap
	                        = RareWordTermMapCreator.createRareWordTermMap( cuiTerms );
	                _delegateDictionary = new MemRareWordDictionary( name, rareWordTermMap );
	                
	                _delegateDictionaries.put(sourceKey, _delegateDictionary);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	    	} else {
	    		LOGGER.info("Using existing dictionary with: " + name + "; " + bsvFilePath);
	    		
	    		_delegateDictionary = _delegateDictionaries.get(sourceKey);
	    	}
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<RareWordTerm> getRareWordHits(FastLookupToken fastLookupToken) {
        return _delegateDictionary.getRareWordHits(fastLookupToken);
    }

    @Override
    public Collection<RareWordTerm> getRareWordHits(String rareWordText) {
        return _delegateDictionary.getRareWordHits(rareWordText);
    }

    private Collection<RareWordTermMapCreator.CuiTerm> parseBsvFile(final String bsvFilePath) {
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

                final RareWordTermMapCreator.CuiTerm cuiTerm = createCuiTuiTerm( columns );
                if ( cuiTerm != null ) {
                    cuiTerms.add( cuiTerm );

                } else {
                    LOGGER.warn( "Bad BSV line " + line + " in " + bsvFilePath );
                }

                line = reader.readLine();
            }
        } catch ( IOException ioE ) {
            LOGGER.error( ioE.getMessage() );
        }
        return cuiTerms;
    }

    private RareWordTermMapCreator.CuiTerm createCuiTuiTerm(final String... columns ) {
        if ( columns.length < 5 ) {
            return null;
        }

        if ( columns[ cuiIndex ].trim().isEmpty() || columns[ termIndex ].trim().isEmpty() ) {
            return null;
        }
        
        return createTerm(columns[cuiIndex], columns[termIndex].trim().toLowerCase());
    }
    
    /**
     * Create an instance of a term.
     * @param cui The term CUI.
     * @param term The term text.
     * @return The term instance.
     */
    protected RareWordTermMapCreator.CuiTerm createTerm(final String cui, final String term) {
        return new RareWordTermMapCreator.CuiTerm(cui, term);
    }
}