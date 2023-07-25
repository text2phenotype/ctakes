package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitAnnotation;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitRelation;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;
import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.Lab;
import org.apache.ctakes.typesystem.type.refsem.LabValue;
import org.apache.ctakes.typesystem.type.refsem.MedicationDosage;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Attributes model for {@link LabMention}
 */
public class LabAttributesModel extends UnknownAttributesModel {

    public List<Object> labValue = new ArrayList<>(3);
    public List<Object> labValueUnit = new ArrayList<>(3);

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        super.init(mention);

        LabMention typedMention = (LabMention)mention;

        Event event = typedMention.getEvent();
        Lab labEvent = null;
        if (event != null && event instanceof Lab)
            labEvent = (Lab)event;

        if (labEvent != null) {
            LabValue lVal = labEvent.getLabValue();
            if (lVal != null) {
                Annotation valueAnnotation = typedMention.getLabValue().getArg2().getArgument();

                labValue.add(0, lVal.getNumber());
                labValue.add(1, valueAnnotation.getBegin());
                labValue.add(2, valueAnnotation.getEnd());

                if (lVal.getUnit() != null) {
                    int beginUnit = 0;
                    int endUnit = 0;

                    try {
                        UnitToken unitAnnotation = JCasUtil.selectSingleRelative(UnitToken.class, valueAnnotation, 1);
                        beginUnit = unitAnnotation.getBegin();
                        endUnit = unitAnnotation.getEnd();
                    } catch (IndexOutOfBoundsException e) {

                    }

                    labValueUnit.add(0, lVal.getUnit());
                    labValueUnit.add(1, beginUnit);
                    labValueUnit.add(2, endUnit);

                }
            }

        }
        return true;
    }
}
