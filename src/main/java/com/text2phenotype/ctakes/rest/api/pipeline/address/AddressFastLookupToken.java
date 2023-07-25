package com.text2phenotype.ctakes.rest.api.pipeline.address;

import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.uima.jcas.tcas.Annotation;

import javax.annotation.concurrent.Immutable;

/**
 * @author Anton Vasin
 * Similar to {@link org.apache.ctakes.dictionary.lookup2.util.FastLookupToken } but all numbers text is changed to '_'. It allows use it when numbers are not critical for FDL.
 */
@Immutable
public class AddressFastLookupToken {

    public static final String NUM_MARKER = "_";
    private boolean isNumber;
    private FastLookupToken flt;

    public AddressFastLookupToken(final Annotation annotation){
        this.flt = new FastLookupToken(annotation);
        isNumber = annotation instanceof NumToken;
    }

    public TextSpan getTextSpan() {
        return this.flt.getTextSpan();
    }

    /**
     * @return True if the token is number
     */
    public boolean isNumber() { return this.isNumber; }
    /**
     * @return the start index used for this lookup token
     */
    public int getStart() {
        return this.flt.getStart();
    }

    /**
     * @return the end index used for this lookup token
     */
    public int getEnd() {
        return this.flt.getEnd();
    }

    /**
     * @return the length of the text span in characters
     */
    public int getLength() {
        return this.flt.getLength();
    }

    /**
     * @return the actual text in the document for the lookup token, in lowercase or
     */
    public String getText() {
        return this.isNumber ? NUM_MARKER : flt.getText();
    }

    /**
     * @return possible canonical variant text for the lookup token, in lowercase, or null if none
     */
    public String getVariant() {
        return this.isNumber && this.flt.getVariant() != null ? NUM_MARKER : this.flt.getVariant();
    }

    /**
     * Two lookup tokens are equal iff the spans are equal.
     *
     * @param value -
     * @return true if {@code value} is a {@code FastLookupToken} and has a span equal to this token's span
     */
    public boolean equals( final Object value ) {
        return value != null && value instanceof FastLookupToken
                && this.flt.getTextSpan().equals( ((FastLookupToken)value).getTextSpan() );
    }

    /**
     * @return hashCode created from the Span
     */
    public int hashCode() {
        return this.flt.getTextSpan().hashCode();
    }
}
