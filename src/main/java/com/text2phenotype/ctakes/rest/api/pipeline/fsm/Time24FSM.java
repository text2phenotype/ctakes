package com.text2phenotype.ctakes.rest.api.pipeline.fsm;

import com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions.NumRangeCondition;
import com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions.PunctuationCondition;
import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;
import org.apache.ctakes.core.fsm.machine.FSM;
import org.apache.ctakes.core.fsm.output.TimeToken;
import org.apache.ctakes.core.fsm.state.NamedState;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Time24FSM implements FSM {

    private final int MIN_MINUTE = 0;
    private final int MAX_MINUTE = 59;
    private final int MIN_HOUR = 0;
    private final int MAX_HOUR = 23;
    private final char SEPARATOR = ':';

    private Machine machine;

    public Time24FSM() {
        initMachine();
    }

    private void initMachine() {

        // states
        State startState = new NamedState("START");

        State hourNumState = new NamedState("HOUR_NUM");
        State separatorState = new NamedState("SEPARATOR");
        State minuteNumState = new NamedState("MINUTE_NUM");
        minuteNumState.setEndStateFlag(true);

        // conditions
        Condition hourNumCondition = new NumRangeCondition(MIN_HOUR,MAX_HOUR);
        Condition minuteNumCondition = new NumRangeCondition(MIN_MINUTE, MAX_MINUTE);
        Condition separatorCondition = new PunctuationCondition(SEPARATOR);

        startState.addTransition(hourNumCondition, hourNumState);
        startState.addTransition(new AnyCondition(), startState);

        hourNumState.addTransition(separatorCondition, separatorState);
        hourNumState.addTransition(new AnyCondition(), startState);

        separatorState.addTransition(minuteNumCondition, minuteNumState);
        separatorState.addTransition(new AnyCondition(), startState);

        minuteNumState.addTransition(new AnyCondition(), startState);

        machine = new Machine(startState);
    }

    @Override
    public Set<TimeToken> execute(List tokens) throws Exception {
        Set<TimeToken> result = new HashSet<>();

        int startTimeTokenIndex = 0;
        for (int i = 0; i < tokens.size(); i++) {
            BaseToken token = (BaseToken) tokens.get(i);

            machine.input(token);

            State currentState = machine.getCurrentState();
            if (currentState.getStartStateFlag())
                startTimeTokenIndex = i;


            if (currentState.getEndStateFlag()) {
                BaseToken startToken = (BaseToken) tokens.get(startTimeTokenIndex);
                result.add(new TimeToken(startToken.getBegin(), token.getEnd()));
                machine.reset();
            }

        }


        machine.reset();
        return result;

    }
}
