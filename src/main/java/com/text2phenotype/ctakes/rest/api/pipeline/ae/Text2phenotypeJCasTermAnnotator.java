package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.PhoneNumberMention;
import com.text2phenotype.ctakes.rest.api.pipeline.concept.ConceptFactory2;
import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.fsm.token.NumberToken;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.JCasUtil;
import org.apache.ctakes.core.util.collection.ArrayListMap;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.core.util.collection.HashSetMap;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.WindowProcessor;
import org.apache.ctakes.dictionary.lookup2.concept.Concept;
import org.apache.ctakes.dictionary.lookup2.concept.ConceptFactory;
import org.apache.ctakes.dictionary.lookup2.dictionary.DictionaryDescriptorParser;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.textspan.DefaultTextSpan;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.dictionary.lookup2.util.DictionarySpec;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.ctakes.typesystem.type.syntax.*;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Text2phenotypeCasTermAnnotator extends JCasAnnotator_ImplBase
        implements JCasTermAnnotator, WindowProcessor {

    // LOG4J logger based on interface name
    final private Logger LOGGER = Logger.getLogger(Text2phenotypeCasTermAnnotator.class);

    private int _lookupWindowType;
    private DictionarySpec _dictionarySpec;
    private final Set<String> _exclusionPartsOfSpeech = new HashSet<>();

    @ConfigurationParameter( name = ConfigParameterConstants.PARAM_LOOKUP_XML, mandatory = false,
            description = ConfigParameterConstants.DESC_LOOKUP_XML, defaultValue = "" )
    private String _lookupXml;

    // type of lookup window to use, typically "LookupWindowAnnotation" or "Sentence"
    @ConfigurationParameter( name = JCasTermAnnotator.PARAM_WINDOW_ANNOT_KEY, mandatory = false,
            description = "Type of Lookup window to use", defaultValue = DEFAULT_LOOKUP_WINDOW )
    private String _windowClassName;

    // set of exclusion POS tags (lower cased), may be null
    @ConfigurationParameter( name = JCasTermAnnotator.PARAM_EXC_TAGS_KEY, mandatory = false,
            description = "Set of exclusion POS tags", defaultValue = DEFAULT_EXCLUSION_TAGS )
    private String _exclusionPosTags;

    // minimum span required to accept a term
    @ConfigurationParameter( name = JCasTermAnnotator.PARAM_MIN_SPAN_KEY, mandatory = false,
            description = "Minimum number of characters for a term" )
    protected int _minimumLookupSpan = DEFAULT_MINIMUM_SPAN;

    // lookup mode: term/code
    @ConfigurationParameter( name = "LookupMode", mandatory = false,
            description = "Lookup mode", defaultValue = "term")
    protected String _lookup_mode = "term";

    // spans which should be skipped. e.g when in the `code` lookup mode annotation code is found as part of phone number
    private List<TextSpan> spansToExclude = null;

    private static final List<Class<? extends Annotation>> coveringClassesToExclude = Arrays.asList(
            PhoneNumberMention.class
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize( final UimaContext uimaContext ) throws ResourceInitializationException {
        super.initialize( uimaContext );
        LOGGER.info( "Using dictionary lookup window type: " + _windowClassName );
        _lookupWindowType = JCasUtil.getType( _windowClassName );
        final String[] tagArr = _exclusionPosTags.split( "," );
        for ( String tag : tagArr ) {
            _exclusionPartsOfSpeech.add( tag.toUpperCase() );
        }
        final List<String> posList = new ArrayList<>( _exclusionPartsOfSpeech );
        Collections.sort( posList );
        final StringBuilder sb = new StringBuilder();
        for ( String pos : posList ) {
            sb.append( pos ).append( " " );
        }
        LOGGER.info( "Exclusion tagset loaded: " + sb.toString() );

        // optional minimum span, default is 3
        final Object minimumSpan = uimaContext.getConfigParameterValue( PARAM_MIN_SPAN_KEY );
        if ( minimumSpan != null ) {
            _minimumLookupSpan = parseInt( minimumSpan, PARAM_MIN_SPAN_KEY, _minimumLookupSpan );
        }
        LOGGER.info( "Using minimum term text span: " + _minimumLookupSpan );
        String descriptorFilePath = _lookupXml;

        LOGGER.info( "Using Dictionary Descriptor: " + descriptorFilePath );
        try ( InputStream descriptorStream = FileLocator.getAsStream( descriptorFilePath ) ) {
            _dictionarySpec = DictionaryDescriptorParser.parseDescriptor( descriptorStream, uimaContext );
        } catch ( IOException | AnnotatorContextException multE ) {
            throw new ResourceInitializationException( multE );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void process( final JCas jcas ) throws AnalysisEngineProcessException {
        LOGGER.info( "Starting processing" );
        final JFSIndexRepository indexes = jcas.getJFSIndexRepository();
        final AnnotationIndex<Annotation> lookupWindows = indexes.getAnnotationIndex( _lookupWindowType );
        if ( lookupWindows == null ) {  // I don't trust AnnotationIndex.size(), so don't check
            return;
        }
        final Map<RareWordDictionary, CollectionMap<TextSpan, Long, ? extends Collection<Long>>> dictionaryTermsMap
                = new HashMap<>( getDictionaries().size() );
        for ( RareWordDictionary dictionary : getDictionaries() ) {
            final CollectionMap<TextSpan, Long, ? extends Collection<Long>> textSpanCuis = new HashSetMap<>();
            dictionaryTermsMap.put( dictionary, textSpanCuis );
        }

        if (this._lookup_mode.equals("code")) {
            this.spansToExclude = new ArrayList<>();
            for (Class<? extends  Annotation> cl : coveringClassesToExclude) {
                 this.spansToExclude.addAll(org.apache.uima.fit.util.JCasUtil
                        .select(jcas, cl)
                        .stream()
                        .map(a -> new DefaultTextSpan(a.getBegin(), a.getEnd()))
                        .collect(Collectors.toList()));
            }
        }

        try {
            for ( Object window : lookupWindows ) {
                if ( isWindowOk( (Annotation)window ) ) {
                    processWindow( jcas, (Annotation)window, dictionaryTermsMap );
                }
            }
        } catch ( ArrayIndexOutOfBoundsException iobE ) {
            // JCasHashMap will throw this every once in a while.  Assume the windows are done and move on
            LOGGER.warn( iobE.getMessage() );
        }
        // Let the consumer handle uniqueness and ordering - some may not care
        final Collection<Long> allDictionaryCuis = new HashSet<>();
        final CollectionMap<Long, Concept, ? extends Collection<Concept>> allConceptsMap = new ArrayListMap<>();
        for ( Map.Entry<RareWordDictionary, CollectionMap<TextSpan, Long, ? extends Collection<Long>>> dictionaryCuis : dictionaryTermsMap
                .entrySet() ) {
            allDictionaryCuis.clear();
            final RareWordDictionary dictionary = dictionaryCuis.getKey();
            final CollectionMap<TextSpan, Long, ? extends Collection<Long>> textSpanCuis = dictionaryCuis.getValue();
            for ( Collection<Long> cuiCodes : textSpanCuis.getAllCollections() ) {
                allDictionaryCuis.addAll( cuiCodes );
            }
            final Collection<ConceptFactory> conceptFactories
                    = _dictionarySpec.getPairedConceptFactories( dictionary.getName() );
            allConceptsMap.clear();
            for ( ConceptFactory conceptFactory : conceptFactories ) {
                if (conceptFactory instanceof ConceptFactory2) {
                    final ConceptFactory2 conceptFactory2 = (ConceptFactory2)conceptFactory;
                    final CollectionMap<Long, Concept, ? extends Collection<Concept>> conceptMap = conceptFactory2.createConceptsCollection( allDictionaryCuis );
                    for (Long cui: conceptMap.keySet()) {
                        allConceptsMap.addAllValues(cui, conceptMap.getCollection(cui));
                    }
                } else {
                    final Map<Long, Concept> conceptMap = conceptFactory.createConcepts( allDictionaryCuis );
                    allConceptsMap.placeMap( conceptMap );
                }
            }
            _dictionarySpec.getConsumer().consumeHits( jcas, dictionary, textSpanCuis, allConceptsMap );
        }
        LOGGER.info( "Finished processing" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<RareWordDictionary> getDictionaries() {
        return _dictionarySpec.getDictionaries();
    }

    /**
     * Skip windows that are section headers/footers.  Kludge, but worth doing
     * todo read these string values as parameters from uimaContext
     * {@inheritDoc}
     */
    @Override
    public boolean isWindowOk( final Annotation window ) {
        final String coveredText = window.getCoveredText();
        return !coveredText.equals( "section id" )
                && !coveredText.startsWith( "[start section id" )
                && !coveredText.startsWith( "[end section id" )
                && !coveredText.startsWith( "[meta rev_" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processWindow( final JCas jcas, final Annotation window,
                               final Map<RareWordDictionary, CollectionMap<TextSpan, Long, ? extends Collection<Long>>> dictionaryTerms ) {
        final List<FastLookupToken> allTokens = new ArrayList<>();
        final List<Integer> lookupTokenIndices = new ArrayList<>();
        getAnnotationsInWindow( jcas, window, allTokens, lookupTokenIndices );
        findTerms( getDictionaries(), allTokens, lookupTokenIndices, dictionaryTerms );
    }

    /**
     * Given a set of dictionaries, tokens, and lookup token indices, populate a terms map with discovered terms
     *
     * @param dictionaries       -
     * @param allTokens          -
     * @param lookupTokenIndices -
     * @param dictionaryTermsMap -
     */
    private void findTerms( final Iterable<RareWordDictionary> dictionaries,
                            final List<FastLookupToken> allTokens, final List<Integer> lookupTokenIndices,
                            final Map<RareWordDictionary, CollectionMap<TextSpan, Long, ? extends Collection<Long>>> dictionaryTermsMap ) {
        for ( RareWordDictionary dictionary : dictionaries ) {
            CollectionMap<TextSpan, Long, ? extends Collection<Long>> termsFromDictionary = dictionaryTermsMap
                    .get( dictionary );
            findTerms( dictionary, allTokens, lookupTokenIndices, termsFromDictionary );
        }
    }

    @Override
    public void findTerms( final RareWordDictionary dictionary,
                           final List<FastLookupToken> allTokens,
                           final List<Integer> lookupTokenIndices,
                           final CollectionMap<TextSpan, Long, ? extends Collection<Long>> termsFromDictionary ) {
        Collection<RareWordTerm> rareWordHits;
        for ( Integer lookupTokenIndex : lookupTokenIndices ) {
            final FastLookupToken lookupToken = allTokens.get( lookupTokenIndex );
            rareWordHits = dictionary.getRareWordHits( lookupToken );
            if ( rareWordHits == null || rareWordHits.isEmpty() ) {
                continue;
            }
            for ( RareWordTerm rareWordHit : rareWordHits ) {
                if ( rareWordHit.getText().length() < _minimumLookupSpan ) {
                    continue;
                }

                if (this.spansToExclude != null && this.spansToExclude.size() > 0) {
                    boolean isExcluded = false;
                    for (int i=0; i < this.spansToExclude.size(); i++) {
                        TextSpan span = this.spansToExclude.get(i);
                        int termStartIdx = lookupTokenIndex - rareWordHit.getRareWordIndex();
                        if (termStartIdx < 0) {
                            break;
                        }
                        int termBegin = allTokens.get(termStartIdx).getStart();
                        if (termBegin >= span.getStart() && termBegin < span.getEnd()) {
                            isExcluded = true;
                            break;
                        }
                    }

                    if (isExcluded) {
                        continue;
                    }
                }

                if ( rareWordHit.getTokenCount() == 1 ) {
                    // Single word term, add and move on
                    termsFromDictionary.placeValue( lookupToken.getTextSpan(), rareWordHit.getCuiCode() );
                    continue;
                }
                final int termStartIndex = lookupTokenIndex - rareWordHit.getRareWordIndex();
                if ( termStartIndex < 0 || termStartIndex + rareWordHit.getTokenCount() > allTokens.size() ) {
                    // term will extend beyond window
                    continue;
                }
                final int termEndIndex = termStartIndex + rareWordHit.getTokenCount() - 1;
                if ( isTermMatch( rareWordHit, allTokens, termStartIndex, termEndIndex ) ) {
                    final int spanStart = allTokens.get( termStartIndex ).getStart();
                    final int spanEnd = allTokens.get( termEndIndex ).getEnd();
                    termsFromDictionary.placeValue( new DefaultTextSpan( spanStart, spanEnd ), rareWordHit.getCuiCode() );
                }
            }
        }
    }

    /**
     * Hopefully the jit will inline this method
     *
     * @param rareWordHit    rare word term to check for match
     * @param allTokens      all tokens in a window
     * @param termStartIndex index of first token in allTokens to check
     * @param termEndIndex   index of last token in allTokens to check
     * @return true if the rare word term exists in allTokens within the given indices
     */
    public boolean isTermMatch( final RareWordTerm rareWordHit, final List<FastLookupToken> allTokens,
                                       final int termStartIndex, final int termEndIndex ) {
        final String[] hitTokens = rareWordHit.getTokens();
        int hit = 0;
        for ( int i = termStartIndex; i < termEndIndex + 1; i++ ) {
            if ( hitTokens[ hit ].equals( allTokens.get( i ).getText() )
                    || hitTokens[ hit ].equals( allTokens.get( i ).getVariant() ) ) {
                // the normal token or variant matched, move to the next token
                hit++;
                continue;
            }
            // the token normal didn't match and there is no matching variant
            return false;
        }
        // some combination of token and variant matched
        return true;
    }


    /**
     * For the given lookup window fills two collections with 1) All tokens in the window,
     * and 2) indexes of tokens in the window to be used for lookup
     *
     * @param jcas               -
     * @param window             annotation lookup window
     * @param allTokens          filled with all tokens, including punctuation, etc.
     * @param lookupTokenIndices filled with indices of tokens to use for lookup
     */
    protected void getAnnotationsInWindow( final JCas jcas, final AnnotationFS window,
                                           final List<FastLookupToken> allTokens,
                                           final Collection<Integer> lookupTokenIndices ) {
        final List<BaseToken> allBaseTokens = org.apache.uima.fit.util.JCasUtil
                .selectCovered( jcas, BaseToken.class, window );
        for ( BaseToken baseToken : allBaseTokens ) {
            if ( baseToken instanceof NewlineToken) {
                continue;
            }
            final boolean isNonLookup = baseToken instanceof PunctuationToken
                    || baseToken instanceof NumberToken
                    || baseToken instanceof ContractionToken;
                    //|| baseToken instanceof SymbolToken;
            // We are only interested in tokens that are -words-
            if ( !isNonLookup ) {
                // POS exclusion logic for first word lookup
                final String partOfSpeech = baseToken.getPartOfSpeech();
                if ( partOfSpeech == null || !_exclusionPartsOfSpeech.contains( partOfSpeech ) ) {
                    lookupTokenIndices.add( allTokens.size() );
                }
            }
            final FastLookupToken lookupToken = new FastLookupToken( baseToken );
            allTokens.add( lookupToken );
        }
    }


    protected int parseInt( final Object value, final String name, final int defaultValue ) {
        if ( value instanceof Integer ) {
            return (Integer)value;
        } else if ( value instanceof String ) {
            try {
                return Integer.parseInt( (String)value );
            } catch ( NumberFormatException nfE ) {
                LOGGER.warn( "Could not parse " + name + " " + value + " as an integer" );
            }
        } else {
            LOGGER.warn( "Could not parse " + name + " " + value + " as an integer" );
        }
        return defaultValue;
    }
}
