package com.text2phenotype.ctakes.rest.api.pipeline.fsm;

import org.apache.ctakes.core.fsm.machine.FSM;
import org.apache.ctakes.core.fsm.output.BaseTokenImpl;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.log4j.Logger;

import java.util.*;

public class LabUnitsFSM implements FSM {
    private final char END_OF_UNIT = 0;
    private TreeMap unitsTree = new TreeMap<>();

    public LabUnitsFSM(Set<String> unitsSet){

        for (String unit: unitsSet) {
            TreeMap levelMap = unitsTree;
            for (char c : unit.toLowerCase().toCharArray()) {
                if (c == 32)
                    continue;

                if (!levelMap.containsKey(c)) {
                    levelMap.put(c, new TreeMap<>());
                }
                levelMap = (TreeMap)levelMap.get(c);
            }

            levelMap.put(END_OF_UNIT, new TreeMap<>());
        }
    }

    @Override
    public Set execute(List data) {

        Set<BaseTokenImpl> units = new HashSet<>();

        TreeMap levelMap = unitsTree;
        int unitBegin = -1;
        int unitEnd = -1;
        String debug = "";
        List<String> unitsText = new ArrayList<>();
        for (int t=0; t< data.size(); t++) {
            BaseToken token = (BaseToken)data.get(t);

            if (levelMap == unitsTree) {
                unitBegin = token.getBegin();
                unitEnd = token.getEnd();
                debug = "";
            }

            char[] charray = token.getCoveredText().toLowerCase().toCharArray();
            int i;
            for (i = 0; i < charray.length; i++) {
                char c = charray[i];
                if (levelMap.containsKey(c)){
                    levelMap = (TreeMap)levelMap.get(c);
                } else {
                    break;
                }
            }

            if (i == charray.length) {
                unitEnd = token.getEnd();
                debug += token.getCoveredText();
                if (t == data.size() - 1 && levelMap.containsKey(END_OF_UNIT)) {
                    units.add(new BaseTokenImpl(unitBegin, unitEnd));
                    unitsText.add(debug);
                }
            } else {
                // token is not a next part of unit
                if (i == 0 && levelMap != unitsTree) {
                    if (levelMap.containsKey(END_OF_UNIT)) {
                        units.add(new BaseTokenImpl(unitBegin, unitEnd));
                        unitsText.add(debug);
                    }

                    levelMap = unitsTree;
                    if (t < data.size() - 1) {
                        t--;
                    }
                } else {
                    levelMap = unitsTree;
                }
            }

        }
        
        if (DEBUG)  LOGGER.debug("Created " + units.size() + " lab/units values.");

        return units;
    }
    
    static private final Logger LOGGER = Logger.getLogger(LabUnitsFSM.class);
    static private final boolean DEBUG = LOGGER.isDebugEnabled();
}
