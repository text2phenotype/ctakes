package com.text2phenotype.ctakes.rest.api.pipeline.concept;

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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST.*;

public class BsvConceptFactoryFlex extends AbstractConceptFactory implements ConceptFactory2 {
    static private final Logger LOGGER = Logger.getLogger( BsvConceptFactoryFlex.class );

    // strictly required columns
    private static final Set<String> strictColumns = new HashSet<String>() {{
        add(CUI);
        add(TUI);
        add(CODE);
        add(SAB);
        add(STR);
        add(PREF);
    }};

    static private final String BSV_FILE_PATH = "bsvPath";
    static private final String BSV_ROW_SCHEME = "scheme";

    private static HashMap<String, CollectionMap<Long, Concept, List<Concept>>> _conceptMaps = new HashMap<>();
    private CollectionMap<Long, Concept, List<Concept>> _conceptMap;

    final public static FilenameFilter fileFilter = (dir, name) -> name.toLowerCase().endsWith("bsv");

    private List<String> scheme;

    public BsvConceptFactoryFlex(final String name, final UimaContext uimaContext, final Properties properties ) {
        this( name, properties.getProperty( BSV_FILE_PATH ), properties.getProperty(BSV_ROW_SCHEME) );
    }

    public BsvConceptFactoryFlex( final String name, final String bsvFilePath, String schemeProp ) {
        super( name );
        try {
            if (schemeProp != null) {
                scheme = Arrays.stream(schemeProp.split("\\|")).collect(Collectors.toList());
            } else {
                LOGGER.error("Row scheme is not defined");
                throw new AssertionError("Row scheme is not defined");
            }

            
            
            synchronized(this) {
	            String sourceKey;
	            if (bsvFilePath != null) {
	        		sourceKey = bsvFilePath;
	        		
	        		if (schemeProp != null) {
	        			sourceKey += "_" + schemeProp;
	        		}
	        	} else if (name != null) {
	        		sourceKey = name;
	        	} else {
	        		sourceKey = "conceptMap_" + _conceptMaps.size();
	        	}
            
        		if (!_conceptMaps.containsKey(sourceKey)) {
        			LOGGER.info("Building concept factory with: " + name + "; " + bsvFilePath + "; " + schemeProp + "; " + sourceKey);
        			
        			final Collection<FlexTerm> cuiTuiTerms = new ArrayList<>();
                    File bsvFile = null;

                    bsvFile = new File(FileLocator.getFullPath(bsvFilePath));

                    // if it is folder load all files with *.bsv extension
                    if (bsvFile.isDirectory()) {

                        for (String fileName : bsvFile.list(fileFilter)) {

                            cuiTuiTerms.addAll(parseBsvFile(bsvFilePath + fileName));
                        }
                    } else {
                        cuiTuiTerms.addAll(parseBsvFile(bsvFilePath));
                    }
                    //        final Collection<CuiTuiSABTerm> cuiTuiTerms = parseBsvFile( bsvFilePath );
                    _conceptMap = new ArrayListMap<>(cuiTuiTerms.size());

                    CuiCodeUtil cuiUtil = CuiCodeUtil.getInstance();
                    for (FlexTerm cuiTuiTerm : cuiTuiTerms) {
                        final CollectionMap<String, String, ? extends Collection<String>> codes
                                = new HashSetMap<>();
                        codes.placeValue(Concept.TUI, TuiCodeUtil.getAsTui(cuiTuiTerm.getTui()));

                        codes.placeValue(cuiTuiTerm.getSAB(), cuiTuiTerm.getCode());

                        Map<String, String> params = new HashMap<>();
                        for (String schemeItem: this.scheme) {
                            if (!strictColumns.contains(schemeItem)) {
                                params.put(schemeItem, cuiTuiTerm.getValue(schemeItem));
                            }
                        }

                        _conceptMap.placeValue(cuiUtil.getCuiCode(cuiTuiTerm.getCui()),
                                new ExtendedConcept(cuiTuiTerm.getCui(), cuiTuiTerm.getPrefTerm(), codes, params));
                    }
                    
                    _conceptMaps.put(sourceKey, _conceptMap);
        		} else {
        			LOGGER.info("Using existing concept factory with: " + name + "; " + bsvFilePath + "; " + schemeProp + "; " + sourceKey);
        			
        			_conceptMap = _conceptMaps.get(sourceKey);
        		}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Long, Concept> createConcepts(final Collection<Long> cuiCodes ) {

        final Map<Long, Concept> conceptMap = new HashMap<>( cuiCodes.size() );
        for ( Long cuiCode : cuiCodes ) {

            if (_conceptMap.containsKey(cuiCode)) {
                for (Concept concept: _conceptMap.getCollection(cuiCode)) {
                    conceptMap.put(cuiCode, concept);
                }
            }
        }
        return conceptMap;
    }

    public CollectionMap<Long, Concept, ? extends Collection<Concept>> createConceptsCollection( final Collection<Long> cuiCodes ) {

        final CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap = new ArrayListMap<>( cuiCodes.size() );
//        final CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap = new ArrayListMap<>( cuiCodes.size() );
        for ( Long cuiCode : cuiCodes ) {

            if (_conceptMap.containsKey(cuiCode)) {

                Collection<Concept> conc = _conceptMap.get(cuiCode);
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

    private Collection<FlexTerm> parseBsvFile(final String bsvFilePath ) {
        final Collection<FlexTerm> flexTerms = new ArrayList<>();
        try ( final BufferedReader reader
                      = new BufferedReader( new InputStreamReader( FileLocator.getAsStream( bsvFilePath ) ) ) ) {
            String line = reader.readLine();
            while ( line != null ) {
                if ( line.startsWith( "//" ) || line.startsWith( "#" ) ) {
                    line = reader.readLine();
                    continue;
                }
                final String[] columns = LookupUtil.fastSplit( line, '|' );
                final FlexTerm cuiTuiTerm = createFlexTerm( columns );
                if ( cuiTuiTerm != null ) {
                    flexTerms.add( cuiTuiTerm );
                } else {
                    LOGGER.warn( "Bad BSV line " + line + " in " + bsvFilePath );
                }
                line = reader.readLine();
            }
        } catch ( IOException ioE ) {
            LOGGER.error( ioE.getMessage() );
        }
        return flexTerms;
    }

    private FlexTerm createFlexTerm(final String... columns ) {
        if ( columns.length < this.scheme.size() ) {
            return null;
        }
        return new FlexTerm(scheme, columns);
    }

    public class FlexTerm {

        final private List<String> scheme;
        final private String[] data;
        final private int __hashcode;

        public FlexTerm(List<String> scheme, String[] data) {
            this.scheme = scheme;
            this.data = data;
            __hashcode = (Arrays.stream(data).reduce((p, n) -> p+"_"+n)).hashCode();
        }

        public String getValue(String name) {
            return data[scheme.indexOf(name)];
        }
        public String getCui() {
            return getValue(CUI);
        }

        public String getTui() {
            return getValue(TUI);
        }

        public String getPrefTerm() {
            return getValue(PREF);
        }

        public String getSAB() {
            return getValue(SAB);
        }

        public String getCode() {
            return getValue(CODE);
        }

        public boolean equals(final Object value ) {
            return value instanceof FlexTerm
                    && __hashcode == ((FlexTerm)value).__hashcode;
        }

        public int hashCode() {
            return __hashcode;
        }
    }
}
