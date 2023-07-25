package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.address.AddressChunk;
import com.text2phenotype.ctakes.rest.api.pipeline.address.AddressChunkTree;
import com.text2phenotype.ctakes.rest.api.pipeline.address.AddressFastLookupToken;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.AddressMention;
import com.text2phenotype.ctakes.rest.api.pipeline.fsm.AddressChunkFSM;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ZipCodeData;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.dictionary.BsvRareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.textspan.DefaultTextSpan;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.Chunk;
import org.apache.ctakes.typesystem.type.syntax.ContractionToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;
import scala.Int;

import java.util.*;
import java.util.stream.Collectors;

public class AddressesAnnotator extends JCasAnnotator_ImplBase{
    private Logger logger = Logger.getLogger(getClass().getName());

    public final static List<String> STATES = Arrays.asList(
            "ak","al","ar","az","ca","co","ct","de","fl","ga","hi","ia","id","il","in","ks","ky","la","ma","md",
            "me","mi","mn","mo","ms","mt","nc","nd","ne","nh","nj","nm","nv","ny","oh","ok","or","pa","ri","sc",
            "sd","tn","tx","ut","va","vt","wa","wi","wv","wy"
    );

    public static final String CHUNK_TYPE_STREET = "STREET";
    public static final String CHUNK_TYPE_CITY = "CITY";
    public static final String CHUNK_TYPE_STATE = "STATE";
    public static final String CHUNK_TYPE_ZIP = "ZIP";

    private BsvRareWordDictionary streetDict;
    private BsvRareWordDictionary cityDict;
    private Map<String, ZipCodeData> zipCodeData;

    private final int MAX_TOKEN_DISTANCE = 3;

    private Comparator<? super RareWordTerm> cmp = Comparator.comparingInt(RareWordTerm::getTokenCount).reversed();

    private Comparator<? super AddressChunk> overlapsCmp = (x,y) -> {
        int xStart = x.getSpan().getStart();
        int xEnd = x.getSpan().getEnd();
        int yStart = y.getSpan().getStart();
        int yEnd = y.getSpan().getEnd();

        if (xStart < yStart) {
            if (xEnd < yEnd) {
                return 0;
            } else {
                return 1;
            }
        }

        if (xStart > yStart) {
            if (xEnd <= yEnd) {
                return -1;
            } else {
                return 0;
            }
        }

        if (xEnd <= yEnd) {
            return -1;
        } else {
            return 1;
        }

    };
    private Comparator<? super Integer> tstCmp = (x,y) -> {
        if (y < x)
            return 1;

        if (y > x)
            return -1;
        return 0;
    };


    @ConfigurationParameter(
            name = "streetDictPath",
            description = "Path to streets dictionary",
            mandatory = true,
            defaultValue = "com/text2phenotype/ctakes/resources/address/streets.bsv"
    )
    private String streetDictPath;

    @ConfigurationParameter(
            name = "cityDictPath",
            description = "Path to cities dictionary",
            mandatory = true,
            defaultValue = "com/text2phenotype/ctakes/resources/address/cities.bsv"
    )
    private String cityDictPath;

    @ConfigurationParameter(
            name = "zipCodeDBPath",
            description = "Path to cities dictionary",
            mandatory = true,
            defaultValue = "com/text2phenotype/ctakes/resources/address/free-zipcode-database.json"
    )
    private String zipCodeDBPath;

    private AddressChunkFSM fsm = new AddressChunkFSM(MAX_TOKEN_DISTANCE);

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);


        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION, false);
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            zipCodeData = mapper.readValue(FileLocator.getAsStream(zipCodeDBPath), new TypeReference<HashMap<String, ZipCodeData>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }

        streetDict = new BsvRareWordDictionary("StreetDict", streetDictPath);
        cityDict = new BsvRareWordDictionary("CityDict", cityDictPath);

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        List<AddressFastLookupToken> lookupTokens = JCasUtil.select(aJCas, BaseToken.class)
                .stream()
                .filter(this::isLookup)
                .map(AddressFastLookupToken::new)
                .collect(Collectors.toList());

        if (lookupTokens.size() == 0) {
            return;
        }

        AddressChunkTree chunkTree = new AddressChunkTree();
        for (int i = 0; i < lookupTokens.size(); i++) {

            // check streets
            AddressChunk streetChunk = checkChunks(aJCas, streetDict, lookupTokens, i, CHUNK_TYPE_STREET);
            if (streetChunk != null) {
                chunkTree.addChunk(streetChunk);
            }
            // check cities
            AddressChunk cityChunk = checkChunks(aJCas, cityDict, lookupTokens, i, CHUNK_TYPE_CITY);
            if (cityChunk != null) {
                chunkTree.addChunk(cityChunk);
            }

            // check state
            AddressFastLookupToken lookupToken = lookupTokens.get(i);
            if (STATES.contains(lookupToken.getText())) {
                Chunk chunk = new Chunk(aJCas, lookupToken.getStart(), lookupToken.getEnd());
                chunk.setChunkType(CHUNK_TYPE_STATE);
                chunkTree.addChunk(new AddressChunk(chunk, new DefaultTextSpan(i, i+1)));
            } else {
                // check ZIP (5 digits)
                if (lookupToken.isNumber() && lookupToken.getLength() - 1 == 5) {
                    Chunk chunk = new Chunk(aJCas, lookupToken.getStart(), lookupToken.getEnd());
                    chunk.setChunkType(CHUNK_TYPE_ZIP);
                    chunkTree.addChunk(new AddressChunk(chunk, new DefaultTextSpan(i, i+1)));
                }
            }

        }
        try {
            for (List<AddressChunk> chunksChain : chunkTree.getBranches()) {
                    Set<List<Chunk>> candidates = (Set<List<Chunk>>)fsm.execute(chunksChain);
                    for (List<Chunk> chunkBundle : candidates) {
                        int begin = Integer.MAX_VALUE;
                        int end = Integer.MIN_VALUE;
                        AddressMention addr = new AddressMention(aJCas);
                        for (Chunk c : chunkBundle) {
                            switch (c.getChunkType()) {
                                case AddressesAnnotator.CHUNK_TYPE_STREET:
                                    addr.setStreet(c.getCoveredText());
                                    break;
                                case AddressesAnnotator.CHUNK_TYPE_CITY:
                                    addr.setCity(c.getCoveredText());
                                    break;
                                case AddressesAnnotator.CHUNK_TYPE_STATE:
                                    addr.setState(c.getCoveredText());
                                    break;
                                case AddressesAnnotator.CHUNK_TYPE_ZIP:
                                    addr.setZip(c.getCoveredText());
                                    break;
                            }
                            begin = Math.min(begin, c.getBegin());
                            end = Math.max(end, c.getEnd());
                        }

                        // check if city is absent
                        if (addr.getZip() != null && zipCodeData.containsKey(addr.getZip())) {
                            // try to add the city by zip code
                            ZipCodeData zipData = zipCodeData.get(addr.getZip());

                            if (addr.getCity() == null) {
                                addr.setCity(zipData.getCity());
                            }

                            if (addr.getState() == null) {
                                addr.setState(zipData.getState());
                            }

                        }

                        addr.setBegin(begin);
                        addr.setEnd(end);
                        addr.addToIndexes();
                    }

            }
        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }
    
    private boolean isLookup(BaseToken token){
        return
                !(token instanceof PunctuationToken) &&
                !(token instanceof ContractionToken);
    }

    private AddressChunk checkChunks(JCas aJCas, RareWordDictionary dict, List<AddressFastLookupToken> lookupTokens, int index, String chunkType) {
        AddressFastLookupToken token = lookupTokens.get(index);
        List<RareWordTerm> terms = new ArrayList<>();
        if (token.getVariant() != null) {
            terms.addAll(dict.getRareWordHits(token.getVariant()));
        }

        if (token.getText() != null && !token.getText().isEmpty()) {
            terms.addAll(dict.getRareWordHits(token.getText()));
        }

        terms.sort(cmp);
        if (terms.size() > 0) {
            for (int i=0; i < terms.size(); i++) {
                RareWordTerm term = terms.get(i);
                if ( term.getText().length() < 3 ) {
                    continue;
                }

                int termStartIndex = index - term.getRareWordIndex();
                if ( termStartIndex < 0 || termStartIndex + term.getTokenCount() > lookupTokens.size() ) {
                    continue;
                }
                final int termEndIndex = termStartIndex + term.getTokenCount() - 1;
                if ( isTerm( term, lookupTokens, termStartIndex, termEndIndex ) ) {

                    // additional checking of house number before the street name
                    if (chunkType.equals(CHUNK_TYPE_STREET) && termStartIndex > 0 && !lookupTokens.get( termStartIndex ).isNumber()) {
                        if (lookupTokens.get( termStartIndex - 1 ).isNumber()) {
                            termStartIndex-=1;
                        }
                    }
                    final int spanStart = lookupTokens.get( termStartIndex ).getStart();
                    final int spanEnd = lookupTokens.get( termEndIndex ).getEnd();
                    Chunk chunk = new Chunk(aJCas, spanStart, spanEnd);
                    chunk.setChunkType(chunkType);
                    return new AddressChunk(chunk, new DefaultTextSpan(termStartIndex, termEndIndex + 1));
                }
            }
        }
        return null;

    }

    public boolean isTerm( final RareWordTerm rareWordTerm, final List<AddressFastLookupToken> lookupTokens,
                                final int termStartIndex, final int termEndIndex ) {
        final String[] hitTokens = rareWordTerm.getTokens();
        int hit = 0;
        for ( int i = termStartIndex; i < termEndIndex + 1; i++ ) {
            if ( hitTokens[ hit ].equals( lookupTokens.get( i ).getText() )
                    || hitTokens[ hit ].equals( lookupTokens.get( i ).getVariant() ) ) {
                hit++;
                continue;
            }
            return false;
        }
        return true;
    }
}
