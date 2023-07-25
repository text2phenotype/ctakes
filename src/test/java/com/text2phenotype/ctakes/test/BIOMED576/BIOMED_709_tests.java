package com.text2phenotype.ctakes.test.BIOMED576;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb.*;
import com.text2phenotype.ctakes.rest.api.pipeline.model.*;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.BaseTokenAttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.LabAttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.attributes.MedicationAttributesModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.*;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

public class BIOMED_709_tests {
    
    @Test
    public void default_mb_test() throws Exception {
        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());

        JCasSerializer.Load(jcas, FileLocator.getFullPath("BIOMED_709/default.xmi"));
        DefaultModelBuilder mb = new DefaultModelBuilder();
        mb.setResultModel(new DefaultResponseModel());
        DefaultResponseModel data = (DefaultResponseModel)mb.build(jcas);

        Assert.assertEquals(2, data.getContent().size());

        ContentModel content1 = data.getContent().get(0);
        Assert.assertEquals("prob", content1.getAspect());
        Assert.assertEquals(9, content1.getText().get(1));
        Assert.assertEquals(27, content1.getText().get(2));

        ContentModel content2 = data.getContent().get(1);
        Assert.assertEquals("device", content2.getAspect());
        Assert.assertEquals(35, content2.getText().get(1));
        Assert.assertEquals(42, content2.getText().get(2));
    }

    @Test
    public void drug_mb_test() throws Exception  {
        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());

        JCasSerializer.Load(jcas, FileLocator.getFullPath("BIOMED_709/drug.xmi"));
        DrugNERModelBuilder mb = new DrugNERModelBuilder();
        mb.setResultModel(new DrugResponseModel());
        DrugResponseModel data = (DrugResponseModel)mb.build(jcas);

        Assert.assertEquals(1, data.getDrugEntities().size());

        ContentModel drug = data.getDrugEntities().get(0);
        Assert.assertEquals(0, drug.getText().get(1));
        Assert.assertEquals(7, drug.getText().get(2));

        MedicationAttributesModel attrs = (MedicationAttributesModel)drug.getAttributes();
        Assert.assertEquals(3, attrs.medFrequencyNumber.size());
        Assert.assertEquals(3, attrs.medFrequencyUnit.size());

        Assert.assertEquals(0, attrs.medStrengthNum.size());
        Assert.assertEquals(0, attrs.medStrengthUnit.size());
        Assert.assertEquals("2", attrs.medDosage);
        Assert.assertEquals("tablet", attrs.medForm);
    }

    @Test
    public void lab_mb_test() throws Exception  {
        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        JCasSerializer.Load(jcas, FileLocator.getFullPath("BIOMED_709/lab.xmi"));

        LabValuesModelBuilder mb = new LabValuesModelBuilder();
        mb.setResultModel(new LabResponseModel());
        LabResponseModel data = (LabResponseModel)mb.build(jcas);

        Assert.assertEquals(1, data.getLabValues().size());

        LabValueModel lab = data.getLabValues().get(0);
        Assert.assertEquals(0, lab.getText().get(1));
        Assert.assertEquals(10, lab.getText().get(2));

        LabAttributesModel attrs = (LabAttributesModel)lab.getAttributes();
        Assert.assertEquals(3, attrs.labValue.size());
        Assert.assertEquals(11, attrs.labValue.get(1));
        Assert.assertEquals(15, attrs.labValue.get(2));
        Assert.assertEquals(0, attrs.labValueUnit.size());
    }

    @Test
    public void POS_tagger_mb_test() throws Exception  {
        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        JCasSerializer.Load(jcas, FileLocator.getFullPath("BIOMED_709/pos.xmi"));

        POSTaggerModelBuilder mb = new POSTaggerModelBuilder();
        mb.setResultModel(new POSTaggerResponseModel());
        POSTaggerResponseModel data = (POSTaggerResponseModel)mb.build(jcas);

        Assert.assertEquals(15, data.getTokens().size());

        for (BaseTokenModel model: data.getTokens()) {
            Assert.assertEquals(2, model.getSentence().size());
            Assert.assertEquals(2, model.getSectionOffset().size());
            Assert.assertEquals(3, model.getText().size());
            Assert.assertNotNull(((BaseTokenAttributesModel)model.getAttributes()).getPartOfSpeech());
        }
    }

    @Test
    public void smoking_status_mb_test() throws Exception  {
        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());

        JCasSerializer.Load(jcas, FileLocator.getFullPath("BIOMED_709/smoking.xmi"));

        SmokingStatusModelBuilder mb = new SmokingStatusModelBuilder();
        mb.setResultModel(new SmokingResponseModel());
        SmokingResponseModel data = (SmokingResponseModel)mb.build(jcas);
        Assert.assertEquals("PAST_SMOKER", data.getSmokingStatus());
    }

    @Test
    public void temporal_mb_test() throws Exception  {
        JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());

        JCasSerializer.Load(jcas, FileLocator.getFullPath("BIOMED_709/temporal.xmi"));

        TemporalModelBuilder mb = new TemporalModelBuilder();
        mb.setResultModel(new TemporalResponseModel());
        TemporalResponseModel data = (TemporalResponseModel)mb.build(jcas);

        Assert.assertEquals(1, data.getEvents().size());
        Assert.assertEquals(0, data.getTlinks().size());
        Assert.assertEquals(1, data.getTimex().size());

        EventModel event = data.getEvents().get(0);
        Assert.assertEquals(2, event.getSentence().size());
        Assert.assertEquals(2, event.getSectionOffset().size());
        Assert.assertEquals(17, event.getText().get(1));
        Assert.assertEquals(27, event.getText().get(2));

        TimeExModel timex = data.getTimex().get(0);
        Assert.assertEquals(2, timex.getSentence().size());
        Assert.assertEquals(2, timex.getSectionOffset().size());
        Assert.assertEquals(0, timex.getText().get(1));
        Assert.assertEquals(10, timex.getText().get(2));
        Assert.assertEquals("DATE", timex.getType());

    }
}
