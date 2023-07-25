package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.PhoneNumberMention;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.attribute.NPIAttributes;
import com.text2phenotype.ctakes.rest.api.pipeline.dictionary.ExtendedJdbcRareWordDictionary;
import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.apache.ctakes.core.fsm.token.NumberToken;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.core.util.collection.HashSetMap;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.dictionary.JdbcRareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.textspan.DefaultTextSpan;
import org.apache.ctakes.dictionary.lookup2.textspan.MultiTextSpan;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.ctakes.dictionary.lookup2.util.JdbcConnectionFactory;
import org.apache.ctakes.dictionary.lookup2.util.TuiCodeUtil;
import org.apache.ctakes.typesystem.type.syntax.*;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.similarity.Levenshtein;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.ctakes.dictionary.lookup2.dictionary.JdbcRareWordDictionary.RARE_WORD_TABLE;
import static org.apache.ctakes.dictionary.lookup2.util.JdbcConnectionFactory.*;

public class NPIAnnotator extends JCasAnnotator_ImplBase {

    private final String MATCH_TYPE = "MATCH_TYPE";

    private final List<String> paramsCol = Arrays.asList(MATCH_TYPE);

    private List<String> matchTypes;

    @ConfigurationParameter( name = "dict", mandatory = false,
            description = "Path to HSQLDB dictionary", defaultValue = "com/text2phenotype/ctakes/resources/dictionaries/npi/npi" )
    private String dictPath;

    @ConfigurationParameter( name = "consecutiveSkips", mandatory = false,
            description = "Number of total tokens that can be skipped" )
    private int _consecutiveSkipMax = 1;

    @ConfigurationParameter( name = JCasTermAnnotator.PARAM_MIN_SPAN_KEY, mandatory = false,
            description = "Minimum number of characters for a term" )
    protected int _minimumLookupSpan = 3;

    @ConfigurationParameter( name = "minTokensMatch", mandatory = false,
            description = "Minimum number of tokens which should be matched")
    protected int _minTokensMatch = 3;

    @ConfigurationParameter( name = "maxMiddleNameLength", mandatory = false,
            description = "Maximum number of tokens of provider middle name")
    protected int maxMiddleNameLength = 2;

    // recognition by name
    private final String MATCH_TYPE_PROVIDER_NAME = "Provider Name"; // 1
    private final String MATCH_TYPE_FACILITY_NAME = "Facility Name"; // 2

    // recognition by address
    private final String MATCH_TYPE_MAILING_ADDRESS_STREET = "Mailing address street"; // 3
    private final String MATCH_TYPE_PHYSICAL_ADDRESS_STREET = "Physical address street"; // 4

    // recognition by contact
    private final String MATCH_TYPE_MAILING_ADDRESS_PHONE = "Mailing address phone"; // 5
    private final String MATCH_TYPE_MAILING_ADDRESS_FAX = "Mailing address fax"; // 6
    private final String MATCH_TYPE_PHYSICAL_ADDRESS_PHONE = "Physical address phone"; // 7
    private final String MATCH_TYPE_PHYSICAL_ADDRESS_FAX = "Physical address fax"; // 8


    private static final int NPI = 1;
    private static final int PREFTERM = 2;
    private static final int TUI = 3;
    private static final int MAILING_STREET1 = 4;
    private static final int MAILING_STREET2 = 5;
    private static final int MAILING_CITY = 6;
    private static final int MAILING_STATE = 7;
    private static final int MAILING_PHONE = 8;
    private static final int MAILING_FAX = 9;
    private static final int MAILING_ZIP = 10;
    private static final int PHYSICAL_STREET1 = 11;
    private static final int PHYSICAL_STREET2 = 12;
    private static final int PHYSICAL_CITY = 13;
    private static final int PHYSICAL_STATE = 14;
    private static final int PHYSICAL_PHONE = 15;
    private static final int PHYSICAL_FAX = 16;
    private static final int PHYSICAL_ZIP = 17;

    private ExtendedJdbcRareWordDictionary dictionary;

    static final private Logger LOGGER = Logger.getLogger( "NPIAnnotator" );

    private PreparedStatement _selectAttrs;
    private PreparedStatement _selectPhone;

    private Map<String, Map<RareWordTerm, Map<String, Object>>> sessionCache = new HashMap<>();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        matchTypes = Arrays.asList(
                "Unknown",
                MATCH_TYPE_PROVIDER_NAME,
                MATCH_TYPE_FACILITY_NAME,
                MATCH_TYPE_MAILING_ADDRESS_STREET,
                MATCH_TYPE_PHYSICAL_ADDRESS_STREET,
                MATCH_TYPE_MAILING_ADDRESS_PHONE,
                MATCH_TYPE_MAILING_ADDRESS_FAX,
                MATCH_TYPE_PHYSICAL_ADDRESS_PHONE,
                MATCH_TYPE_PHYSICAL_ADDRESS_FAX
        );
        Properties props = new Properties();
        props.setProperty(JDBC_DRIVER, "org.hsqldb.jdbcDriver");
        props.setProperty(JDBC_URL, "jdbc:hsqldb:file:" + dictPath);
        props.setProperty(JDBC_USER, "sa");
        props.setProperty(JDBC_PASS, "");
        props.setProperty(RARE_WORD_TABLE, "cui_terms");

        try {
            dictionary = new ExtendedJdbcRareWordDictionary("NPIDict", context, props);

            final Connection connection = JdbcConnectionFactory.getInstance()
                    .getConnection( props.getProperty(JDBC_DRIVER), props.getProperty(JDBC_URL), props.getProperty(JDBC_USER), props.getProperty(JDBC_PASS) );

            _selectAttrs = createSelectCall( connection, "ATTRS" );
            _selectPhone = createSelectPhonesCall(connection, "ATTRS");

        } catch (SQLException ex) {
            throw new ResourceInitializationException(ex);
        }
    }

    static private PreparedStatement createSelectPhonesCall(final Connection connection, final String tableName )
            throws SQLException {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT {table}.*, 5 as MATCH_TYPE FROM {table} WHERE (MAILING_PHONE = ?)");
        sb.append(" UNION");
        sb.append(" SELECT {table}.*, 6 as MATCH_TYPE FROM {table} WHERE (MAILING_FAX = ?)");
        sb.append(" UNION");
        sb.append(" SELECT {table}.*, 7 as MATCH_TYPE FROM {table} WHERE (PHYSICAL_PHONE = ?)");
        sb.append(" UNION");
        sb.append(" SELECT {table}.*, 8 as MATCH_TYPE FROM {table} WHERE (PHYSICAL_FAX = ?)");
        final String lookupSql = sb.toString().replace("{table}", tableName);
        return connection.prepareStatement( lookupSql );
    }

    static private PreparedStatement createSelectCall(final Connection connection, final String tableName )
            throws SQLException {
        final String lookupSql = String.format("SELECT * FROM %s WHERE NPI = ?", tableName) ;
        return connection.prepareStatement( lookupSql );
    }

    private NationalProviderMention createNPIMention(JCas aJCas, ResultSet results, String matchType, int begin, int end) throws SQLException {
        NationalProviderMention mention = new NationalProviderMention(aJCas, begin, end);
        mention.setMatchType(matchType);
        mention.setSAB("NPI");
        mention.setCode(results.getString(NPI));
        mention.setPrefText(results.getString(PREFTERM));
        mention.setTUI(TuiCodeUtil.getAsTui(results.getInt(TUI)));
        mention.addToIndexes();

        NPIAttributes mailingAttrs = new NPIAttributes(aJCas);

        String mailingStreet1 = results.getString(MAILING_STREET1);
        String mailingStreet2 = results.getString(MAILING_STREET2);
        if (mailingStreet2!= null && !mailingStreet2.isEmpty()){
            mailingStreet1 = String.format("%s, %s", mailingStreet1, mailingStreet2);
        }

        mailingAttrs.setStreet(mailingStreet1);

        mailingAttrs.setCity(results.getString(MAILING_CITY));
        mailingAttrs.setState(results.getString(MAILING_STATE));
        mailingAttrs.setPhone(results.getLong(MAILING_PHONE));
        mailingAttrs.setFax(results.getLong(MAILING_FAX));
        mailingAttrs.setZip(results.getLong(MAILING_ZIP));
//                                attrs.addToIndexes();

        mention.setMailingAddress(mailingAttrs);

        String physicalStreet1 = results.getString(PHYSICAL_STREET1);
        String physicalStreet2 = results.getString(PHYSICAL_STREET2);
        if (physicalStreet2!= null && !physicalStreet2.isEmpty()){
            physicalStreet1 = String.format("%s, %s", physicalStreet1, physicalStreet2);
        }

        NPIAttributes physicalAttrs = new NPIAttributes(aJCas);
        physicalAttrs.setStreet(physicalStreet1);
        physicalAttrs.setCity(results.getString(PHYSICAL_CITY));
        physicalAttrs.setState(results.getString(PHYSICAL_STATE));
        physicalAttrs.setPhone(results.getLong(PHYSICAL_PHONE));
        physicalAttrs.setFax(results.getLong(PHYSICAL_FAX));
        physicalAttrs.setZip(results.getLong(PHYSICAL_ZIP));

        mention.setPhysicalAddress(physicalAttrs);
        return mention;
    }
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        final Collection<Sentence> sents = JCasUtil.select(aJCas, Sentence.class);
        final Map<Sentence, Collection<PhoneNumberMention>> phones_idx = JCasUtil.indexCovered(aJCas, Sentence.class, PhoneNumberMention.class);
        sessionCache.clear();
        try {
            for ( Sentence sent : sents ) {
                final List<FastLookupToken> allTokens = new ArrayList<>();
                final List<Integer> lookupTokenIndices = new ArrayList<>();
                getAnnotationsInWindow( aJCas, sent, allTokens, lookupTokenIndices );
                final CollectionMap<TextSpan, NpiParams, ? extends Collection<NpiParams>> textSpanCuis = new HashSetMap<>();
                findTerms( dictionary, allTokens, lookupTokenIndices, textSpanCuis );
                for (TextSpan span : textSpanCuis.keySet()) {
                    for (NpiParams npiParams: textSpanCuis.get(span)){
                        _selectAttrs.clearParameters();
                        _selectAttrs.setLong( NPI, npiParams.getNpi() );
                        ResultSet results = _selectAttrs.executeQuery();
                        try {
                            while (results.next()) {
                                int matchtype = ((int)npiParams.getParams().getOrDefault(MATCH_TYPE, 0));
                                createNPIMention(aJCas, results, matchTypes.get(matchtype), span.getStart(), span.getEnd());
                            }
                        }
                        catch (Exception ex) {
                            throw new AnalysisEngineProcessException(ex);
                        }
                        finally {
                            results.close();
                        }
                    }
                }


                // try to find NPI by phones
                if (phones_idx.containsKey(sent)) {
                    for (PhoneNumberMention phoneMention : phones_idx.get(sent)) {
                        Long number = phoneMention.getNumber();
                        if (number == 0) {
                            continue;
                        }
                        _selectPhone.setLong(1, number);
                        _selectPhone.setLong(2, number);
                        _selectPhone.setLong(3, number);
                        _selectPhone.setLong(4, number);
                        ResultSet results = _selectPhone.executeQuery();
                        try {
                            while (results.next()) {
                                int matchtype = results.getInt(MATCH_TYPE);
                                createNPIMention(aJCas, results, matchTypes.get(matchtype), phoneMention.getBegin(), phoneMention.getEnd());
                                phoneMention.removeFromIndexes();
                            }
                        }
                        catch (Exception ex) {
                            throw new AnalysisEngineProcessException(ex);
                        }
                        finally {
                            results.close();
                        }
                    }
                }

            }
        } catch ( Exception iobE ) {
            throw new AnalysisEngineProcessException(iobE);
        }

    }

    private boolean checkToken(final String hitToken, final FastLookupToken lookupToken) {
        return hitToken.equals(lookupToken.getText()) || hitToken.equals(lookupToken.getVariant());
    }

    private TextSpan getTermSpanForAddress( final RareWordTerm rareWordHit, final List<FastLookupToken> allTokens,
                                final int termStartIndex, final int termEndIndex ) {

        int minMatch = Math.min(rareWordHit.getTokenCount(), _minTokensMatch);
        final String[] hitTokens = rareWordHit.getTokens();
        int tokensMatched = 0;

        int globalRareWordIndex = termStartIndex + rareWordHit.getRareWordIndex();

        boolean leftLock = rareWordHit.getRareWordIndex() == 0;
        boolean rightLock = rareWordHit.getRareWordIndex() == rareWordHit.getTokenCount() - 1;

        int range = Math.max(rareWordHit.getRareWordIndex(), rareWordHit.getTokenCount() - rareWordHit.getRareWordIndex() - 1);
        int endIndex = globalRareWordIndex;
        int beginIndex = globalRareWordIndex;
        int n = 1;
        try {

            if (checkToken(hitTokens[rareWordHit.getRareWordIndex()], allTokens.get(globalRareWordIndex))) {
                tokensMatched++;
            }

            for ( n = 1; n <= range; n++ ) {
                if (!leftLock) {
                    int leftLocalIdx = rareWordHit.getRareWordIndex() - n;
                    int leftGlobalIdx = globalRareWordIndex - n;
                    if (leftLocalIdx < 0 || leftGlobalIdx < termStartIndex || leftGlobalIdx < 0) {
                        leftLock = true;
                    } else {
                        if (checkToken(hitTokens[leftLocalIdx], allTokens.get(leftGlobalIdx))) {
                            beginIndex = leftGlobalIdx;
                            tokensMatched++;
                        }
                    }
                }

                if (!rightLock) {
                    int rightLocalIdx = rareWordHit.getRareWordIndex() + n;
                    int rightGlobalIdx = globalRareWordIndex + n;
                    if (rightLocalIdx >= rareWordHit.getTokenCount() || rightGlobalIdx > termEndIndex || rightGlobalIdx >= allTokens.size()) {
                        rightLock = true;
                    } else {
                        if (checkToken(hitTokens[rightLocalIdx], allTokens.get(rightGlobalIdx))) {
                            endIndex = rightGlobalIdx;
                            tokensMatched++;
                        }
                    }
                }

                if (leftLock && rightLock) {
                    break;
                }

            }

            if (tokensMatched >= minMatch) {
                return new DefaultTextSpan(allTokens.get(beginIndex).getStart(), allTokens.get(endIndex).getEnd());
            }

            return null;

        } catch (Exception ex) {
            LOGGER.error(String.format("Index: %d, Rareword: %s, CUI: %d", n, rareWordHit.getRareWord(), rareWordHit.getCuiCode()));
            throw ex;
        }
    }

    private TextSpan getTermSpanForProviderName( final RareWordTerm rareWordHit, final List<FastLookupToken> allTokens,
                                                 final int termStartIndex, final int termEndIndex ) {

        final String[] hitTokens = rareWordHit.getTokens();
        int fskipped = 0;
        int bskipped = 0;
        int fcommasFound = 0;
        int bcommasFound = 0;

        int nfHits = 1;
        int nbHits = 1;
        int rfHits = 1;
        int rbHits = 1;
        int rareIdx = rareWordHit.getRareWordIndex();
        int lookupIdx = termStartIndex + rareWordHit.getRareWordIndex();
        int backRange = rareWordHit.getRareWordIndex();
        int forwardRange = rareWordHit.getTokenCount() - rareWordHit.getRareWordIndex() - 1;
        // find forward hits
        for (int i = 1; i - fskipped <= forwardRange || i - fcommasFound <= forwardRange; i++) {

            int idx = lookupIdx + i;
            int hitIdx = rareIdx + nfHits;
            if (idx < allTokens.size() && hitIdx < hitTokens.length) {
                if (checkToken(hitTokens[hitIdx], allTokens.get(idx))) {
                    nfHits++;
                    if (nfHits > forwardRange)
                        break;
                } else {
                    fskipped++;
                }
            }

            idx = lookupIdx - i;
            hitIdx = rareIdx + rfHits;
            if (fcommasFound != -1 && idx >= 0 && hitIdx < hitTokens.length) {
                if (checkToken(hitTokens[hitIdx], allTokens.get(idx))) {
                    rfHits++;
                    if (rfHits > forwardRange)
                        break;
                } else {
                    if (allTokens.get(idx).getText().equals(",") && fcommasFound == 0) {
                        fcommasFound = 1;
                    } else {
                        fcommasFound = -1;
                    }
                }
            }

            if (fskipped > maxMiddleNameLength) {
                break;
            }
        }

        // find backward hits
        for (int i = 1; i - bskipped <= backRange || i - bcommasFound <= backRange; i++) {

            int idx = lookupIdx - i;
            int hitIdx = rareIdx - nbHits;
            if (idx >= 0 && hitIdx >= 0) {
                if (checkToken(hitTokens[hitIdx], allTokens.get(idx))) {
                    nbHits++;
                    if (nbHits > backRange)
                        break;
                } else {
                    bskipped++;
                }
            }

            idx = lookupIdx + i;
            hitIdx = rareIdx - rbHits;
            if (bcommasFound != -1 && idx < allTokens.size() && hitIdx >= 0) {
                if (checkToken(hitTokens[hitIdx], allTokens.get(idx))) {
                    rbHits++;
                    if (rbHits > backRange)
                        break;
                } else {
                    if (allTokens.get(idx).getText().equals(",") && bcommasFound == 0 && fcommasFound == 0) {
                        bcommasFound = 1;
                    } else {
                        bcommasFound = -1;
                    }
                }
            }

            if (bskipped > maxMiddleNameLength) {
                break;
            }
        }


        if (rfHits + rbHits > rareWordHit.getTokenCount()) {
            return new DefaultTextSpan(allTokens.get(lookupIdx - (rfHits - 1) - fcommasFound).getStart(), allTokens.get(lookupIdx + (rbHits - 1) + bcommasFound).getEnd());
        } else if (nfHits + nbHits > rareWordHit.getTokenCount()) {
            return new DefaultTextSpan(allTokens.get(lookupIdx - (nbHits - 1) - bskipped).getStart(), allTokens.get(lookupIdx + (nfHits - 1) + fskipped).getEnd());
        }

        return null;
    }

    private TextSpan getTermSpan( final RareWordTerm rareWordHit, final List<FastLookupToken> allTokens,
                                  final int termStartIndex, final int termEndIndex ) {
        final String[] hitTokens = rareWordHit.getTokens();
        int hit = 0;
        for ( int i = termStartIndex; i < termEndIndex + 1; i++ ) {
            if ( hitTokens[ hit ].equals( allTokens.get( i ).getText() )
                    || hitTokens[ hit ].equals( allTokens.get( i ).getVariant() ) ) {
                hit++;
                continue;
            }
            return null;
        }
        return new DefaultTextSpan(allTokens.get(termStartIndex).getStart(), allTokens.get(termStartIndex + hit - 1).getEnd());
    }

    public void findTerms( final ExtendedJdbcRareWordDictionary dictionary,
                           final List<FastLookupToken> allTokens,
                           final List<Integer> lookupTokenIndices,
                           final CollectionMap<TextSpan, NpiParams, ? extends Collection<NpiParams>> termsFromDictionary ) {
        Map<RareWordTerm, Map<String, Object>> rareWordHits;
        for ( Integer lookupTokenIndex : lookupTokenIndices ) {
            final FastLookupToken lookupToken = allTokens.get( lookupTokenIndex );
            if (sessionCache.containsKey(lookupToken.getText())) {
                rareWordHits = sessionCache.get(lookupToken.getText());
            } else {
                if (lookupToken.getVariant() != null && sessionCache.containsKey(lookupToken.getVariant())) {
                    rareWordHits = sessionCache.get(lookupToken.getVariant());
                } else {
                    rareWordHits = dictionary.getRareWordHits(lookupToken, paramsCol);
                    sessionCache.put(lookupToken.getText(), rareWordHits);
                }
            }
            if ( rareWordHits == null || rareWordHits.isEmpty() ) {
                continue;
            }
            for ( RareWordTerm rareWordHit : rareWordHits.keySet() ) {

                if ( rareWordHit.getText().length() < _minimumLookupSpan ) {
                    continue;
                }

                Map<String, Object> params = rareWordHits.get(rareWordHit);
                if ( rareWordHit.getTokenCount() == 1 ) {
                    // Single word term, add and move on
                    termsFromDictionary.placeValue( lookupToken.getTextSpan(), new NpiParams(rareWordHit.getCuiCode(), params) );
                    continue;
                }

                TextSpan sp = null;
                int matchType = (int)params.getOrDefault(MATCH_TYPE, 0);

                final int termStartIndex = lookupTokenIndex - rareWordHit.getRareWordIndex();
                if ( matchType != 1 && termStartIndex < 0 ) {
                    // term will extend beyond window
                    continue;
                }
                final int termEndIndex = termStartIndex + rareWordHit.getTokenCount() - 1;

                switch (matchType) {
                    case 1: { // provider name
                        if (termStartIndex + rareWordHit.getTokenCount() <= allTokens.size() ||
                            lookupTokenIndex - rareWordHit.getTokenCount() + 1 + rareWordHit.getRareWordIndex() >= 0) {
                            sp = getTermSpanForProviderName(rareWordHit, allTokens, termStartIndex, termEndIndex);
                        }
                        break;
                    }
                    case 3: // addresses
                    case 4:{
                        if (termStartIndex + Math.min(rareWordHit.getTokenCount(), _minTokensMatch) <= allTokens.size()) {
                            sp = getTermSpanForAddress(rareWordHit, allTokens, termStartIndex, termEndIndex);
                        }
                        break;
                    }
                    default: { // facility name
                        if (termStartIndex + rareWordHit.getTokenCount() <= allTokens.size()) {
                            sp = getTermSpan(rareWordHit, allTokens, termStartIndex, termEndIndex);
                        }
                    }
                }
                if ( sp != null) {
                    termsFromDictionary.placeValue( sp, new NpiParams(rareWordHit.getCuiCode(), params) );
                }
            }
        }
    }

    protected void getAnnotationsInWindow( final JCas jcas, final AnnotationFS window,
                                           final List<FastLookupToken> allTokens,
                                           final Collection<Integer> lookupTokenIndices ) {

        final List<BaseToken> allBaseTokens = JCasUtil.selectCovered( jcas, BaseToken.class, window );
        for ( BaseToken baseToken : allBaseTokens ) {
            if ( baseToken instanceof NewlineToken) {
                continue;
            }
            final boolean isNonLookup = baseToken instanceof PunctuationToken
                    || baseToken instanceof NumberToken
                    || baseToken instanceof ContractionToken;

            if ( !isNonLookup ) {
                lookupTokenIndices.add( allTokens.size() );
            }
            final FastLookupToken lookupToken = new FastLookupToken( baseToken );
            allTokens.add( lookupToken );
        }
    }

    private class NpiParams {
        private Long npi;
        private Map<String, Object> params;

        public Long getNpi() {
            return npi;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        NpiParams(Long npiCode, Map<String, Object> params) {
            this.npi = npiCode;
            this.params = params;
        }

        NpiParams(Long npiCode) {
            this(npiCode, new HashMap<>());
        }
    }
}
