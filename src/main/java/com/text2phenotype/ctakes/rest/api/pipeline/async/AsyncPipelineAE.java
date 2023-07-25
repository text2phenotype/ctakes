package com.text2phenotype.ctakes.rest.api.pipeline.async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineManagement;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.AnalysisProcessData;
import org.apache.uima.analysis_engine.CasIterator;
import org.apache.uima.analysis_engine.JCasIterator;
import org.apache.uima.analysis_engine.ResultNotSupportedException;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.util.Logger;
import org.apache.uima.util.ProcessTrace;
import org.apache.uima.util.impl.ProcessTrace_impl;

/**
 * Analysis engine for asynchronous pipeline
 */
public class AsyncPipelineAE implements AnalysisEngine {

    private boolean isDestroyedFlag = false;
    private String currentStepKey = null;

    private Properties mPerformanceTuningSettings = UIMAFramework
            .getDefaultPerformanceTuningProperties();

    private ResourceMetaData metaData;

    private List<String> annotatorsSequence = new ArrayList<>();

    private AsyncPipelineAERepository repository;

    public AsyncPipelineAERepository getRepository() {
        return repository;
    }

    public void setRepository(AsyncPipelineAERepository repository) {
        this.repository = repository;
    }

    public AsyncPipelineAE(List<String> annotators) {
        this.annotatorsSequence.addAll(annotators);
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
        return false;
    }

    @Override
    public ResourceMetaData getMetaData() {
        return metaData;
    }

    @Override
    public AnalysisEngineMetaData getAnalysisEngineMetaData() {
        return null;
    }

    @Override
    public CAS newCAS() throws ResourceInitializationException {
        return null;
    }

    @Override
    public JCas newJCas() throws ResourceInitializationException {
        return null;
    }

    @Override
    public ProcessTrace process(CAS aCAS, ResultSpecification aResultSpec) throws ResultNotSupportedException, AnalysisEngineProcessException {
        throw new AnalysisEngineProcessException();
    }

    @Override
    public ProcessTrace process(CAS aCAS) throws AnalysisEngineProcessException {
        throw new AnalysisEngineProcessException();
    }

    @Override
    public void process(CAS aCAS, ResultSpecification aResultSpec, ProcessTrace aTrace) throws ResultNotSupportedException, AnalysisEngineProcessException {
        throw new AnalysisEngineProcessException();
    }

    @Override
    public void process(AnalysisProcessData aProcessData, ResultSpecification aResultSpec) throws ResultNotSupportedException, AnalysisEngineProcessException {
        throw new AnalysisEngineProcessException();
    }

    final private org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger( "AsyncPipelineAE" );
    private final boolean DEBUG = LOGGER.isDebugEnabled();

    @Override
    public ProcessTrace process(JCas aJCas) throws AnalysisEngineProcessException {
        this.isDestroyedFlag = false;
        currentStepKey = null;
        ProcessTrace trace = new ProcessTrace_impl();
        try {
            Iterator<String> keysItr = annotatorsSequence.iterator();
            while (keysItr.hasNext()) {
            	String key = keysItr.next();

                if (this.isDestroyed()) {
                	if (DEBUG)  LOGGER.debug("Timeout error: " + trace.toString());
                    break;
                }

                currentStepKey = key;
                AnalysisEngine engine = null;
                AsyncPipelineLayer layer = null;
                layer = repository.GetLayer(key);
                engine = layer.Lock();
                try {
                    trace.addAll(engine.process(aJCas).getEvents());
                } finally {
                    currentStepKey = null;
                    if (!isDestroyed()) {
                        layer.Free(engine);
                    }

                }
            }
        } catch (InterruptedException e) {
        	if (DEBUG)  LOGGER.debug(e.toString());

            throw new AnalysisEngineProcessException(e);
        }
        return trace;
    }

    @Override
    public ProcessTrace process(JCas aJCas, ResultSpecification aResultSpec) throws ResultNotSupportedException, AnalysisEngineProcessException {
        throw new AnalysisEngineProcessException();
    }

    @Override
    public void process(JCas aJCas, ResultSpecification aResultSpec, ProcessTrace aTrace) throws ResultNotSupportedException, AnalysisEngineProcessException {
        throw new AnalysisEngineProcessException();
    }

    @Override
    public CasIterator processAndOutputNewCASes(CAS aCAS) throws AnalysisEngineProcessException {
        return null;
    }

    @Override
    public JCasIterator processAndOutputNewCASes(JCas aJCAS) throws AnalysisEngineProcessException {
        return null;
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {

    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {

    }

    @Override
    public ResultSpecification createResultSpecification() {
        return null;
    }

    @Override
    public ResultSpecification createResultSpecification(TypeSystem aTypeSystem) {
        return null;
    }

    @Override
    public ResourceManager getResourceManager() {
        return null;
    }

    @Override
    public Object getConfigParameterValue(String aParamName) {
        return null;
    }

    @Override
    public Object getConfigParameterValue(String aGroupName, String aParamName) {
        return null;
    }

    @Override
    public void setConfigParameterValue(String aParamName, Object aValue) {

    }

    @Override
    public void setConfigParameterValue(String aGroupName, String aParamName, Object aValue) {

    }

    @Override
    public void reconfigure() throws ResourceConfigurationException {

    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void setLogger(Logger aLogger) {

    }

    @Override
    public void destroy() {
        this.isDestroyedFlag = true;
        if (currentStepKey != null) {
            try {
                repository.GetLayer(currentStepKey).CreateNew();
            } catch (ResourceInitializationException e) {
                LOGGER.error(e);
            }
        }
    }

    public boolean isDestroyed() {
        return this.isDestroyedFlag;
    }

    @Override
    public UimaContext getUimaContext() {
        return null;
    }

    @Override
    public UimaContextAdmin getUimaContextAdmin() {
        return null;
    }

    @Override
    public String[] getFeatureNamesForType(String aTypeName) {
        return new String[0];
    }

    @Override
    public Properties getPerformanceTuningSettings() {
        return mPerformanceTuningSettings;
    }

    @Override
    public void setResultSpecification(ResultSpecification aResultSpec) {

    }

    @Override
    public AnalysisEngineManagement getManagementInterface() {
        return null;
    }

    @Override
    public void processCas(CAS aCAS) throws ResourceProcessException {

    }

    @Override
    public void processCas(CAS[] aCASes) throws ResourceProcessException {

    }

    @Override
    public void typeSystemInit(TypeSystem aTypeSystem) throws ResourceInitializationException {

    }

    @Override
    public boolean isStateless() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public ProcessingResourceMetaData getProcessingResourceMetaData() {
        return null;
    }

    @Override
    public void batchProcessComplete(ProcessTrace aTrace) throws ResourceProcessException, IOException {

    }

    @Override
    public void collectionProcessComplete(ProcessTrace aTrace) throws ResourceProcessException, IOException {

    }
}
