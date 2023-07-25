package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.refsem.*;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.uima.cas.CASException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attributes model for {@link MedicationMention}
 */
public class MedicationAttributesModel extends UnknownAttributesModel {

    public List<Object> medFrequencyNumber = new ArrayList<>(3);
    public List<Object> medFrequencyUnit = new ArrayList<>(3);
    public List<Object> medStrengthNum = new ArrayList<>(3);
    public List<Object> medStrengthUnit = new ArrayList<>(3);
    public String medStatusChange;
    public String medDosage;
    public String medRoute;
    public String medForm;
    public String medDuration;

    private static final List<String> SPECIAL_FREQ_WORDS = Arrays.asList("ONCE","TWICE","ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE", "T.I.D");
    private static final List<String> SPECIAL_STR_WORDS = Arrays.asList("ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE");

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        super.init(mention);

        MedicationMention typedMention = (MedicationMention)mention;

        // dosage
        Modifier tempMod = typedMention.getMedicationDosage();
        if (tempMod != null) {
            MedicationDosage attrData = (MedicationDosage)tempMod.getNormalizedForm();
            medDosage = attrData.getValue();
        }

        // strength
        tempMod = typedMention.getMedicationStrength();
        if (tempMod != null) {
            MedicationStrength attrData = (MedicationStrength)tempMod.getNormalizedForm();

            String txt = tempMod.getCoveredText();

            int[] valuePos = new int[2];
            valuePos[0] = txt.indexOf(attrData.getNumber());
            medStrengthNum.add(0,attrData.getNumber());

            if (valuePos[0] == -1) {
                valuePos = findSpecialWord(txt, SPECIAL_STR_WORDS);
            } else {
                valuePos[1] = valuePos[0] + attrData.getNumber().length();
            }

            // special word was not found and number also is not found
            // check numbers with separator
            if (valuePos[0] == -1) {
                String numPattern = "(" + attrData.getNumber().replaceAll("(\\d)", "$1[.,]?") + ")";
                Pattern p = Pattern.compile(numPattern);
                Matcher m = p.matcher(txt);
                if (m.find()) {

                    valuePos[0] = m.start(0);
                    valuePos[1] = m.end(0);
                }
            }

            if (valuePos[0] != -1) {
                medStrengthNum.add(1, tempMod.getBegin() + valuePos[0]);
                medStrengthNum.add(2, tempMod.getBegin() + valuePos[1]);
            }

            if (attrData.getUnit() != null) {
                int unitPos = txt.indexOf(attrData.getUnit());
                medStrengthUnit.add(0, attrData.getUnit());
                medStrengthUnit.add(1, tempMod.getBegin() + unitPos);
                medStrengthUnit.add(2, tempMod.getBegin() + unitPos + attrData.getUnit().length());
            }
        }

        // route
        tempMod = typedMention.getMedicationRoute();
        if (tempMod != null) {
            MedicationRoute attrData = (MedicationRoute)tempMod.getNormalizedForm();
            medRoute = attrData.getValue();
        }

        // form
        tempMod = typedMention.getMedicationForm();
        if (tempMod != null) {
            MedicationForm attrData = (MedicationForm)tempMod.getNormalizedForm();
            medForm = attrData.getValue();
        }

        // duration
        tempMod = typedMention.getMedicationDuration();
        if (tempMod != null) {
            MedicationDuration attrData = (MedicationDuration)tempMod.getNormalizedForm();
            medDuration = attrData.getValue();
        }

        // frequency
        tempMod = typedMention.getMedicationFrequency();
        if (tempMod != null) {
            if (tempMod.getEnd() - tempMod.getBegin() == 0) {
                return false;
            }
            MedicationFrequency attrData = (MedicationFrequency)tempMod.getNormalizedForm();
            String txt = tempMod.getCoveredText();

            if (attrData.getNumber() != null) {
                int[] valuePos = new int[2];
                valuePos[0] = txt.indexOf(attrData.getNumber());
                medFrequencyNumber.add(0,attrData.getNumber());
                if (valuePos[0] == -1) {
                    valuePos = findSpecialWord(txt, SPECIAL_FREQ_WORDS);
                } else {
                    valuePos[1] = valuePos[0] + attrData.getNumber().length();
                }
                if (valuePos[0] != -1) {
                    medFrequencyNumber.add(1, tempMod.getBegin() + valuePos[0]);
                    medFrequencyNumber.add(2, tempMod.getBegin() + valuePos[1]);
                } else {
//                    return false;
                }
            }

            if (attrData.getUnit() != null) {
                int unitPos = txt.indexOf(attrData.getUnit());
                medFrequencyUnit.add(0, attrData.getUnit());
                if (unitPos != -1) {
                    medFrequencyUnit.add(1, tempMod.getBegin() + unitPos);
                    medFrequencyUnit.add(2, tempMod.getBegin() + unitPos + attrData.getUnit().length());
                } else {
//                    return false;
                }
            }
        }

        // status change
        tempMod = typedMention.getMedicationStatusChange();
        if (tempMod != null) {
            MedicationStatusChange attrData = (MedicationStatusChange)tempMod.getNormalizedForm();
            medStatusChange = attrData.getValue();
        }

        return true;
    }

    private int[] findSpecialWord(String text, List<String> words) {
        String txt = text.toUpperCase().replace(" ", "");
        for (String word: words) {
            int pos = txt.indexOf(word);
            if (pos != -1)
                return new int[]{pos, pos + word.length()};
        }
        return new int[] {-1, -1};
    }
}
