package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.core.ae.TokenizerAnnotator;
import org.apache.ctakes.core.nlp.tokenizer.TokenizerPTB;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.*;

/**
 * Tokenizer class to handle erroneous cTAKES core processing.
 * @author mike.banos
 *
 */
public class TokenizerAnnotatorPTB extends JCasAnnotator_ImplBase {

	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * Value is "SegmentsToSkip".  This parameter specifies which segments to skip.  The parameter should be
	 * of type String, should be multi-valued and optional.
	 */
	public static final String PARAM_SEGMENTS_TO_SKIP = "SegmentsToSkip";
	@ConfigurationParameter(
			name = PARAM_SEGMENTS_TO_SKIP,
			mandatory = false,
			description = "Set of segments that can be skipped"
	)
	private String[] skipSegmentsArray;
	private Set<String> skipSegmentsSet;

	private TokenizerPTB tokenizer;

	static char CR = '\r';
	static char LF = '\n';

	private int tokenCount = 0;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		logger.info("Initializing " + this.getClass().getName());
		tokenizer = new TokenizerPTB();
		skipSegmentsSet = new HashSet<>();
		if(skipSegmentsArray != null){
			Collections.addAll(skipSegmentsSet, skipSegmentsArray);
		}
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.info("process(JCas) in " + this.getClass().getName());

		tokenCount = 0;

		Collection<Segment> segments = JCasUtil.select(jcas, Segment.class);
		for(Segment sa : segments){
			String segmentID = sa.getId();
			if (!skipSegmentsSet.contains(segmentID)) {
				annotateRange(jcas, sa.getBegin(), sa.getEnd());
			}
		}

		Thread thread = Thread.currentThread();
		for(BaseToken token : JCasUtil.select(jcas, BaseToken.class)) {

			if (thread.isInterrupted()) {
				throw new AnalysisEngineProcessException(new InterruptedException());
			}

			if (token.getClass() == NumToken.class) {
				final String text = token.getCoveredText();
				try {
					Double.parseDouble(text.replaceAll(",", ""));
				} catch(NumberFormatException ex) {
					WordToken word = new WordToken(jcas, token.getBegin(), token.getEnd());
					setAttributes(word, text);
					word.addToIndexes();
					
					token.removeFromIndexes();
				}
			}
		}
	}

	protected void annotateRange(JCas jcas, int rangeBegin, int rangeEnd) throws AnalysisEngineProcessException {

		// int tokenCount = 0; // can't start with tokenCount=0 here because this method can be called multiple times

		// First look for all newlines and carriage returns (which are not contained within sentences)
		String docText = jcas.getDocumentText();
		Thread thread = Thread.currentThread();
		for (int i = rangeBegin; i<rangeEnd; i++) {

			if (thread.isInterrupted()) {
				throw new AnalysisEngineProcessException(new InterruptedException());
			}

			if (docText.charAt(i)==CR) {

				NewlineToken nta;
				if (i+1<rangeEnd && docText.charAt(i+1)==LF) {
					// single NewlineToken for the 2 characters
					nta = new NewlineToken(jcas, i, i+2);
					i++; // skip past the LF
				} else {
					nta = new NewlineToken(jcas, i, i+1);
				}
				nta.addToIndexes();

			} else if (docText.charAt(i)==LF) {

				NewlineToken nta = new NewlineToken(jcas, i, i+1);
				nta.addToIndexes();

			}

		}

		// Now process each sentence
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);

		// Tokenize each sentence, adding the tokens to the cas index
		for(Sentence sentence : sentences){
			if (sentence.getBegin() < rangeBegin || sentence.getEnd() > rangeEnd) {
				continue;
			}
			List<?> tokens = tokenizer.tokenizeTextSegment(jcas, sentence.getCoveredText(), sentence.getBegin(), true);
			for (Object bta: tokens) {
				if (thread.isInterrupted()) {
					throw new AnalysisEngineProcessException(new InterruptedException());
				}

				if (bta==null) {
					Exception e = new RuntimeException("bta==null tokenCount=" + tokenCount + " tokens.size()==" + tokens.size());
					e.printStackTrace();
				} else{
					//logger.info("Token #" + tokenCount + " len = " + bta.getCoveredText().length() + " " + bta.getCoveredText());
					// add the BaseToken to CAS index
					if(BaseToken.class.isAssignableFrom(bta.getClass())){
						BaseToken.class.cast(bta).addToIndexes();
					}else{
						throw new AnalysisEngineProcessException("Token returned cannot be cast as BaseToken", new Object[]{bta});
					}
					//tokenCount++;
				}
			}

		}

		// Now add the tokenNumber in the order of offsets
		Collection<BaseToken> tokens = JCasUtil.select(jcas, BaseToken.class);
		for(BaseToken bta : tokens){
			if (bta.getBegin()>=rangeBegin && bta.getBegin()<rangeEnd) {
				bta.setTokenNumber(tokenCount);
				tokenCount++;
			}
		}

	}

	/**
	 * Set word token specific attributes.
	 * Adopted from org.apache.ctakes.core.nlp.tokenizer.TokenizerPTB.
	 * @param token The token to process.
	 * @param text The token text.
	 */
	public static void setAttributes(final WordToken token, final String text) {
		if (text.isEmpty()) {
			token.setNumPosition(TokenizerAnnotator.TOKEN_NUM_POS_NONE);
			token.setCapitalization(TokenizerAnnotator.TOKEN_CAP_NONE);
			
			return;
		}
		
		boolean containsDigit = false, containsNonUpperCase = false;
		int upperCount = 0;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			
			if (Character.isUpperCase(c)) {
				upperCount++;
			} else {
				containsNonUpperCase = true;
				
				if (Character.isDigit(c)) containsDigit = true;
			}
		}
		
		if (Character.isDigit(text.charAt(0))) {
			token.setNumPosition(TokenizerAnnotator.TOKEN_NUM_POS_FIRST);
		} else if (Character.isDigit(text.charAt(text.length() - 1))) {
			token.setNumPosition(TokenizerAnnotator.TOKEN_NUM_POS_LAST);
		} else if (containsDigit) { 
			token.setNumPosition(TokenizerAnnotator.TOKEN_NUM_POS_MIDDLE);
		} else { 
			token.setNumPosition(TokenizerAnnotator.TOKEN_NUM_POS_NONE);
		}
		
		if (upperCount == 0) { 
			token.setCapitalization(TokenizerAnnotator.TOKEN_CAP_NONE);
		} else if (!containsNonUpperCase) {
			token.setCapitalization(TokenizerAnnotator.TOKEN_CAP_ALL);
	    } else if (upperCount == 1 && Character.isUpperCase(text.charAt(0))) {
	    	token.setCapitalization(TokenizerAnnotator.TOKEN_CAP_FIRST_ONLY);
	    } else {
	    	token.setCapitalization(TokenizerAnnotator.TOKEN_CAP_MIXED);
	    }
	}
}
