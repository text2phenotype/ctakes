package com.text2phenotype.ctakes.rest.api.pipeline.model.attributes;

import org.apache.ctakes.typesystem.type.refsem.BodyLaterality;
import org.apache.ctakes.typesystem.type.refsem.BodySide;
import org.apache.ctakes.typesystem.type.refsem.ProcedureDevice;
import org.apache.ctakes.typesystem.type.refsem.ProcedureMethod;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.cas.CASException;

/**
 * Attributes model for {@link ProcedureMention}
 */
public class ProcedureAttributesModel extends UnknownAttributesModel {

    public String bodyLaterality;
    public String procedureMethod;
    public String bodySide;
    public String procedureDevice;

    public String startDate;
    public String startTime;
    public String endDate;
    public String endTime;

    @Override
    public boolean init(IdentifiedAnnotation mention) throws CASException {
        super.init(mention);

        ProcedureMention typedMention = (ProcedureMention)mention;

        Modifier tempMod;

        // BodyLaterality
        tempMod = typedMention.getBodyLaterality();
        if (tempMod != null) {
            BodyLaterality attr = (BodyLaterality)tempMod.getNormalizedForm();
            bodyLaterality = attr.getValue();
        }
        // ProcedureMethod
        tempMod = typedMention.getMethod();
        if (tempMod != null) {
            ProcedureMethod attr = (ProcedureMethod)tempMod.getNormalizedForm();
            procedureMethod = attr.getValue();
        }
        // BodySide
        tempMod = typedMention.getBodySide();
        if (tempMod != null) {
            BodySide attr = (BodySide)tempMod.getNormalizedForm();
            bodySide = attr.getValue();
        }
        // ProcedureDevice
        tempMod = typedMention.getProcedureDevice();
        if (tempMod != null) {
            ProcedureDevice attr = (ProcedureDevice)tempMod.getNormalizedForm();
            procedureDevice = attr.getValue();
        }

//        typedMention.getRelativeTemporalContext();
//        typedMention.getBodyLocation();
//        typedMention.getDuration();

        // StartDate and StartTime
        TimeMention startTimeMention = typedMention.getStartTime();
        if (startTimeMention != null) {
            if (startTimeMention.getDate() != null)
                startDate = startTimeMention.getDate().toString();
            if (startTimeMention.getDate() != null)
                startTime = startTimeMention.getTime().toString();
        }
        // EndDate and EndTime
        TimeMention endTimeMention = typedMention.getEndTime();
        if (endTimeMention != null) {
            if (endTimeMention.getDate() != null)
                endDate = endTimeMention.getDate().toString();

            if (endTimeMention.getTime() != null)
                endTime = endTimeMention.getTime().toString();
        }

        return true;
    }
}
