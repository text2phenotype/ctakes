package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * AnalysisComponent wrapper. Allows avoiding multi creation of the components with the same parameters.
 * Instead of this, it gets the early created component from the repository.
 */
public class CachedComponent implements AnalysisComponent {
	final static private Logger LOGGER = Logger.getLogger( CachedComponent.class.getName() );
    final static private boolean DEBUG = LOGGER.isDebugEnabled();
    
    private AnalysisComponent wrappedComponent;

    /**
     * Generates hash code for the component by its context
     * @param aContext UIMA context
     * @return
     */
    private static int createContextHash(UimaContext aContext) {
        Map<String, Object> hashMap = new HashMap<>();
        for (String paramName : aContext.getConfigParameterNames()) {
            hashMap.put(paramName, aContext.getConfigParameterValue(paramName));
        }

        return hashMap.hashCode();
    }

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        int hashKey = createContextHash(aContext);
        CachedComponentsRepository repository = CachedComponentsRepository.getInstance();

        // get component from the repository or create new instance
        if (repository.hasComponent(hashKey)) {
        	if (DEBUG)  LOGGER.debug("Found cached AnalysisComponent for key: " + hashKey);
        	
            wrappedComponent = repository.getComponent(hashKey);
        } else {
        	if (DEBUG)  LOGGER.debug("Creating cached AnalysisComponent for key: " + hashKey);
        	
            try {
                // get component implementation and create instance by name
                String impl = aContext.getConfigParameterValue("for").toString();
                Class<?> c = Class.forName(impl);
                if (!AnalysisComponent.class.isAssignableFrom(c)){
                    throw new Exception("Cached class is not AnalysisComponent");
                }
                Constructor<?> ctor = c.getConstructor();
                wrappedComponent = (AnalysisComponent) ctor.newInstance();
                wrappedComponent.initialize(aContext);
                repository.addComponent(hashKey, wrappedComponent);
            } catch (Exception e) {
                throw new ResourceInitializationException(e);
            }

        }


    }

    @Override
    public void reconfigure() throws ResourceInitializationException, ResourceConfigurationException {
        synchronized (wrappedComponent) {
            wrappedComponent.reconfigure();
        }
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {
        synchronized (wrappedComponent) {
            wrappedComponent.batchProcessComplete();
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        synchronized (wrappedComponent) {
            wrappedComponent.collectionProcessComplete();
        }
    }

    @Override
    public void destroy() {
    	if (DEBUG)  LOGGER.debug("Destroy called but not propogated to wrappedComponent.");
        // wrappedComponent.destroy();
        // TODO: remove from repository?
    }

    @Override
    public void process(AbstractCas aCAS) throws AnalysisEngineProcessException {
        LOGGER.info(wrappedComponent.getClass().getName() + " is starting");
        synchronized (wrappedComponent) {
            wrappedComponent.process(aCAS);
        }
        LOGGER.info(wrappedComponent.getClass().getName() + " is finished");
    }

    @Override
    public boolean hasNext() throws AnalysisEngineProcessException {
        return wrappedComponent.hasNext();
    }

    @Override
    public AbstractCas next() throws AnalysisEngineProcessException {
        return wrappedComponent.next();
    }

    @Override
    public Class<? extends AbstractCas> getRequiredCasInterface() {
        return wrappedComponent.getRequiredCasInterface();
    }

    @Override
    public int getCasInstancesRequired() {
        return wrappedComponent.getCasInstancesRequired();
    }

    @Override
    public void setResultSpecification(ResultSpecification aResultSpec) {
        synchronized (wrappedComponent) {
            wrappedComponent.setResultSpecification(aResultSpec);
        }
    }
}
