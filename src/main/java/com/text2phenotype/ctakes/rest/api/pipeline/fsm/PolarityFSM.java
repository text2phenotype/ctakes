package com.text2phenotype.ctakes.rest.api.pipeline.fsm;

import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;
import org.apache.ctakes.core.fsm.condition.TextSetCondition;
import org.apache.ctakes.core.fsm.machine.FSM;
import org.apache.ctakes.core.fsm.output.NegationIndicator;
import org.apache.ctakes.core.fsm.state.NamedState;
import org.apache.ctakes.core.fsm.token.BaseToken;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PolarityFSM implements FSM {

    private Set<String> polaritySet = new HashSet<String>();

    private Machine polarityMachine;

    public PolarityFSM() {

        polaritySet.add("unremarkable");
        polaritySet.add("unlikely");
        polaritySet.add("negative");
        polaritySet.add("no");
        polaritySet.add("unclear");
//        polaritySet.add("never");

        initMachine();
    }

    private void initMachine() {
        State startState = new NamedState("START");
        State endState = new NamedState("END");
        endState.setEndStateFlag(true);

        polarityMachine = new Machine(startState);

        Condition polarityCondition = new TextSetCondition(polaritySet, false);

        startState.addTransition(polarityCondition, endState);
        startState.addTransition(new AnyCondition(), startState);

        endState.addTransition(new AnyCondition(), endState);
    }

    @Override
    public Set<NegationIndicator> execute(List tokens) throws Exception {
        Set<NegationIndicator> outSet = new HashSet<>();

        int startTokenIndex = 0;
        for (int i = 0; i < tokens.size(); ++i) {
            BaseToken token = (BaseToken) tokens.get(i);

            polarityMachine.input(token);

            State currentState = polarityMachine.getCurrentState();
            if (currentState.getStartStateFlag()) {
                startTokenIndex = i;
            }
            if (currentState.getEndStateFlag()) {

                BaseToken startToken = (BaseToken) tokens.get(startTokenIndex);
                outSet.add(new NegationIndicator(startToken.getStartOffset(), token.getEndOffset()));
                break;
            }

        }

        // reset machine
        polarityMachine.reset();

        return outSet;
    }
}
