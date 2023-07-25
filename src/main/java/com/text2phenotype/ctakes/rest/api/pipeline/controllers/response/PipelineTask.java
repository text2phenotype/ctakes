package com.text2phenotype.ctakes.rest.api.pipeline.controllers.response;

import com.text2phenotype.ctakes.rest.api.pipeline.async.IFreeCasCallback;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb.ResponseModelBuilder;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ErrorResponseModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseModel;
import gov.nih.nlm.nls.lvg.Util.Str;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.concurrent.Callable;

public class PipelineTask implements Callable<ResponseEntity<ResponseData>> {

    final static private Logger LOGGER = Logger.getLogger(PipelineTask.class);
    final static private boolean DEBUG = LOGGER.isDebugEnabled();

    private AnalysisEngine ae;
    private ResponseModelBuilder modelBuilder;
    private String text;
    private TypeSystemDescription typeSystem;
    private JCas jCas;
    private IFreeCasCallback freeCasCallback = null;

    // for async mode
    public PipelineTask(AnalysisEngine ae, String text, TypeSystemDescription typeSystem, ResponseModelBuilder modelBuilder) {
        this.ae = ae;
        this.modelBuilder = modelBuilder;
        this.text = text;
        this.typeSystem = typeSystem;
    }

    public PipelineTask(AnalysisEngine ae, JCas jCas, ResponseModelBuilder modelBuilder) {
        this.ae = ae;
        this.modelBuilder = modelBuilder;
        this.jCas = jCas;
    }

    @Override
    public ResponseEntity<ResponseData> call() {
        try {
        	if (this.jCas == null) {
                if (this.typeSystem == null || this.text == null) {
                    throw new Exception("Wrong task data. TypeSystem and Text are required");
                }
                this.jCas = JCasFactory.createJCas(this.typeSystem);
                this.jCas.setDocumentText(this.text);
            }
            
            ae.process(jCas);
            ResponseData data = modelBuilder.build(jCas);
            
            return new ResponseEntity<>(data, HttpStatus.OK);
        }
        catch(Exception e) {
        	e.printStackTrace();
        	LOGGER.error(e.getMessage());
            return new ResponseEntity<>(new ErrorResponseModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        finally {
            if (this.freeCasCallback != null && this.jCas != null) {
                this.freeCasCallback.onFreeCas(this.jCas);
            }
        }
    }

    public void setFreeCasCallback(IFreeCasCallback cb){
        this.freeCasCallback = cb;
    }
}
