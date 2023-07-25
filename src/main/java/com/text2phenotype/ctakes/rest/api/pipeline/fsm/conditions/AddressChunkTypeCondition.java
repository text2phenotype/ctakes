package com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions;

import net.openai.util.fsm.Condition;
import org.apache.ctakes.typesystem.type.syntax.Chunk;

public class AddressChunkTypeCondition extends Condition {

    private String expectedType;

    public AddressChunkTypeCondition(String type) {
        this.expectedType = type;
    }

    @Override
    public boolean satisfiedBy(Object o) {

        if (o instanceof Chunk) {
            Chunk chunk = (Chunk)o;
            return expectedType.equals(chunk.getChunkType());
        }

        return false;
    }
}
