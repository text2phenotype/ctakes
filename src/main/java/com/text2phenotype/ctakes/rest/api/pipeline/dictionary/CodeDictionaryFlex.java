package com.text2phenotype.ctakes.rest.api.pipeline.dictionary;

import java.util.Properties;

import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordTermMapCreator;
import org.apache.uima.UimaContext;

/**
 * Dictionary for looking up concepts by code.
 * Bypasses term "tokenization" to avoid splitting codes by "." (see ICD formats for examples).
 * @author mike.banos
 *
 */
public class CodeDictionaryFlex extends BsvRareWordDictionaryFlex {
	public CodeDictionaryFlex(String name, UimaContext uimaContext, Properties properties) {
		super(name, uimaContext, properties);
	}
	
	/**
     * Create an instance of a term.
     * @param cui The term CUI.
     * @param term The term text.
     * @return The term instance.
     */
	@Override
    protected RareWordTermMapCreator.CuiTerm createTerm(final String cui, final String term) {
        try {
            // check if it is number like '0.000'
            Double.parseDouble(term);
            
            return new CodeCuiTerm(cui, term);
        } catch (NumberFormatException nfe) {
            return super.createTerm(cui, term);
        }
    }

	/**
	 * Term class to keep original terms in tact.
	 * @author mike.banos
	 *
	 */
	static public class CodeCuiTerm extends RareWordTermMapCreator.CuiTerm {
        final private String term;

        public CodeCuiTerm(final String cui, final String term) {
        	super(cui, "");
        	this.term = new String(term);
        }

        @Override
        public String getTerm() {
           return term;
        }

        @Override
        public boolean equals( final Object value ) {
           return value instanceof RareWordTermMapCreator.CuiTerm
                  && term.equals( ((RareWordTermMapCreator.CuiTerm)value).getTerm() )
                  && getCui().equals( ((RareWordTermMapCreator.CuiTerm)value).getCui() );
        }
     }
}
