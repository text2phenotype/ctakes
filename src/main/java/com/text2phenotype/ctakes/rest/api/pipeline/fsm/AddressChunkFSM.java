package com.text2phenotype.ctakes.rest.api.pipeline.fsm;

import com.text2phenotype.ctakes.rest.api.pipeline.address.AddressChunk;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.AddressesAnnotator;
import com.text2phenotype.ctakes.rest.api.pipeline.fsm.conditions.AddressChunkTypeCondition;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST;
import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;
import org.apache.ctakes.core.fsm.machine.FSM;
import org.apache.ctakes.core.fsm.state.NamedState;
import org.apache.ctakes.typesystem.type.syntax.Chunk;

import java.util.*;

public class AddressChunkFSM implements FSM {

    private int maxDistance = CONST.MAX_TOKENS_DISTANCE_BETWEEN_ADDRESSES;
    private Machine machine;

    public AddressChunkFSM(int maxTokensDistance) {
        maxDistance = maxTokensDistance;
        init();
    }

    public void init() {

        State startState = new NamedState("START");

        State streetFound = new NamedState("STREET_FOUND");
        State cityFound = new NamedState("CITY_FOUND");
        State cityOnlyFound = new NamedState("CITY_ONLY_FOUND");
        State stateFound = new NamedState("STATE_FOUND");
        State zipFound = new NamedState("ZIP_FOUND");
//        zipFound.setEndStateFlag(true);

        State endState = new NamedState("END");
        endState.setEndStateFlag(true);

        Condition isStreet = new AddressChunkTypeCondition(AddressesAnnotator.CHUNK_TYPE_STREET);
        Condition isCity = new AddressChunkTypeCondition(AddressesAnnotator.CHUNK_TYPE_CITY);
        Condition isCity2 = new AddressChunkTypeCondition(AddressesAnnotator.CHUNK_TYPE_CITY);
        Condition isState = new AddressChunkTypeCondition(AddressesAnnotator.CHUNK_TYPE_STATE);
        Condition isZip = new AddressChunkTypeCondition(AddressesAnnotator.CHUNK_TYPE_ZIP);

        startState.addTransition(isStreet, streetFound);
        startState.addTransition(isCity, cityOnlyFound);
        startState.addTransition(new AnyCondition(), startState);

        streetFound.addTransition(isCity2, cityFound);
        streetFound.addTransition(isState, stateFound);
        streetFound.addTransition(isZip, zipFound);
        streetFound.addTransition(new AnyCondition(), startState);

        cityFound.addTransition(isState, stateFound);
        cityFound.addTransition(isZip, zipFound);
        cityFound.addTransition(new AnyCondition(), endState);

        cityOnlyFound.addTransition(isState, stateFound);
        cityOnlyFound.addTransition(isZip, zipFound);
        cityOnlyFound.addTransition(new AnyCondition(), startState);

        stateFound.addTransition(isZip, zipFound);
        stateFound.addTransition(new AnyCondition(), endState);

        zipFound.addTransition(new AnyCondition(), endState);

        machine = new Machine(startState);
    }

    @Override
    public Set execute(List chunksChain) throws Exception {
        machine.reset();
        Set<List<Chunk>> result = new HashSet<>();
        if (chunksChain.size() == 0) {
            return result;
        }

        List<Chunk> chunks = new ArrayList<>(4);
        Iterator<AddressChunk> chunksItr = (Iterator<AddressChunk>)chunksChain.iterator();
        AddressChunk currentAddrChunk = chunksItr.next();
        machine.input(currentAddrChunk.getChunk());


        while (chunksItr.hasNext()) {
            AddressChunk nextAddrChunk = chunksItr.next();
            State currentState = machine.getCurrentState();
            machine.input(nextAddrChunk.getChunk());
            State nextState = machine.getCurrentState();
            int currIdx = currentAddrChunk.getSpan().getEnd();
            int idx = nextAddrChunk.getSpan().getStart();

            if (currentState != null && !currentState.getStartStateFlag()) {
                chunks.add(currentAddrChunk.getChunk());
            }

            if (nextState.getStartStateFlag()) {
                chunks.clear();
                machine.input(nextAddrChunk.getChunk());
            }

            if (nextState != null && nextState.getEndStateFlag()) {
                if (chunks.size() > 1) {
                    result.add(chunks);
                    machine.reset();
                    machine.input(nextAddrChunk.getChunk());
                    chunks = new ArrayList<>(4);
                } else {
                    chunks.clear();
                }
            } else if (idx - currIdx > maxDistance) {
                chunks.add(currentAddrChunk.getChunk());
                if (chunks.size() > 1) {
                    result.add(chunks);
                }
                chunks = new ArrayList<>(4);
                machine.reset();
                machine.input(nextAddrChunk.getChunk());
            }

            currentAddrChunk = nextAddrChunk;
        }

        if (machine.getCurrentState() != null && !machine.getCurrentState().getStartStateFlag()) {
            chunks.add(currentAddrChunk.getChunk());
        }
        if (chunks.size() > 1) {
            result.add(chunks);
        }
        return result;
    }
}
