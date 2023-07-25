package com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions;

import net.openai.util.fsm.Condition;

import java.util.List;
import java.util.Set;

/**
 * Check if the type in the list
 */
public class InTypeLIstCondition extends Condition {

    private Set typeList;

    public InTypeLIstCondition(Set types) {
        this.typeList = types;
    }
    @Override
    public boolean satisfiedBy(Object o) {

        return typeList.contains(o.getClass());
    }
}
