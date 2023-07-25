package com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions;

import net.openai.util.fsm.Condition;
import org.apache.ctakes.typesystem.type.syntax.NumToken;

public class NumRangeCondition extends Condition {

    private int minVal;
    private int maxVal;

    public NumRangeCondition(int minValue, int maxValue) {
        minVal = minValue;
        maxVal = maxValue;
    }

    @Override
    public boolean satisfiedBy(Object o) {
        if (o instanceof NumToken) {
            NumToken numToken = (NumToken)o;
            try {
                int num = Integer.parseInt(numToken.getCoveredText());
                if (num >= minVal && num <= maxVal)
                    return true;

            } catch (NumberFormatException e) {
                return false;
            }

        }
        return false;
    }
}
