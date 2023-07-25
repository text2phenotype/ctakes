package com.text2phenotype.ctakes.rest.api.pipeline.address;

import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;
import org.apache.ctakes.typesystem.type.syntax.Chunk;

public class AddressChunk {

    private Chunk chunk;
    private TextSpan span;

    public AddressChunk(Chunk chunk, TextSpan span) {
        this.chunk = chunk;
        this.span = span;
    }

    /**
     * Chunk
     * @return
     */
    public Chunk getChunk() {
        return chunk;
    }

    /**
     * Words index span
     * @return
     */
    public TextSpan getSpan() {
        return span;
    }


    @Override
    public String toString() {
        return String.format("%s [%d:%d]", this.chunk.getCoveredText(), this.span.getStart(), this.span.getEnd());
    }
}
