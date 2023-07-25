package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ExtendedSemanticUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BIOMED_710_tests {

    private Map<Integer, List<String>> createExpectation() {
        Map<Integer, List<String>> result = new HashMap<>();
        result.put(CONST.NE_TYPE_ID_DRUG, Arrays.asList("T053", "T109", "T110", "T114", "T115", "T116", "T118", "T119",
                "T121", "T122", "T123", "T124", "T125", "T126", "T127",
                "T129", "T130", "T131", "T195", "T196", "T197", "T200", "T203"));
        result.put(CONST.NE_TYPE_ID_LAB, Arrays.asList("T034", "T059", "T201"));
        result.put(CONST.NE_TYPE_ID_ANATOMICAL_SITE, Arrays.asList("T021", "T022", "T023", "T024", "T025", "T026", "T029", "T030"));
        result.put(CONST.NE_TYPE_ID_DISORDER, Arrays.asList("T019", "T020", "T037", "T047", "T048", "T049", "T050", "T190", "T191"));
        result.put(CONST.NE_TYPE_ID_FINDING, Arrays.asList("T033", "T040", "T041", "T042", "T043", "T044", "T045", "T046", "T056", "T057", "T184"));
        result.put(CONST.NE_TYPE_ID_PROCEDURE, Arrays.asList("T060", "T061"));
        result.put(com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST.NE_TYPE_ID_ACTIVITY, Arrays.asList("T058"));
        return result;
    }

    @Test
    public void semantics_test() {
        Map<Integer, List<String>> expectations = createExpectation();
        for (int i=0; i<1000; i++) {
            String tui = String.format("T%03d", i);
            Integer semantic = CONST.NE_TYPE_ID_UNKNOWN;
            for (Integer sem : expectations.keySet()) {
                List<String> val = expectations.get(sem);
                if (val.contains(tui)) {
                    semantic = sem;
                    break;
                }
            }

            Assert.assertEquals("Semantics are not equal", semantic, ExtendedSemanticUtil.getTuiSemanticGroupId(tui));

        }
    }
}
