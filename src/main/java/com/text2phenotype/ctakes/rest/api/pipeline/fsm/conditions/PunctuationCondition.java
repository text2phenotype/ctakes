package com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions;

import net.openai.util.fsm.Condition;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;

public class PunctuationCondition extends Condition {

    private String punctuationVal;

    public PunctuationCondition(char punctuationValue) {
        punctuationVal = String.valueOf(punctuationValue);
    }

    @Override
    public boolean satisfiedBy(Object o) {
        if (o instanceof PunctuationToken) {
            PunctuationToken punctuationToken = (PunctuationToken)o;

            if (punctuationToken.getCoveredText().equals(punctuationVal))
                return true;

        }
        return false;
    }
}
