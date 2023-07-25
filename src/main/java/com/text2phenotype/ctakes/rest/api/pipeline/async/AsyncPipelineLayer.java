package com.text2phenotype.ctakes.rest.api.pipeline.async;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.ArrayDeque;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncPipelineLayer {
    static final private Logger LOGGER = Logger.getLogger(AsyncPipelineLayer.class);

    private final ArrayDeque<AnalysisEngine> engines;
    private AnalysisEngineDescription description;
    private int maxCount;
    private Lock lock = new ReentrantLock();
    private Condition hasFreeEngine = lock.newCondition();

    public AsyncPipelineLayer(AnalysisEngineDescription description, int instanceCount) throws ResourceInitializationException {
        engines = new ArrayDeque<>(instanceCount);
        this.maxCount = instanceCount;
        this.description = description;
    }

    public void Init() throws ResourceInitializationException {
        for (int lc=0; lc < this.maxCount; lc++) {
            CreateNew();
        }
    }
    /**
     * Add new component if layer is not full
     */
    public void CreateNew() throws ResourceInitializationException {
        if (engines.size() < maxCount) {
            AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(description);
            Properties tuningProps = ae.getPerformanceTuningSettings();
            tuningProps.setProperty(UIMAFramework.JCAS_CACHE_ENABLED, "false");
            tuningProps.setProperty(UIMAFramework.CAS_INITIAL_HEAP_SIZE, "1000000");
            engines.push(ae);
        } else {
        	LOGGER.warn("Could not add new analysis engine: " + description.getAnnotatorImplementationName());
        }
    }

    public AnalysisEngine Lock() throws InterruptedException {
        lock.lock();
        try {
            while (engines.size() == 0) {
                hasFreeEngine.await();
            }

            return engines.poll();
        } finally {
            lock.unlock();
        }
    }

    public void Free(AnalysisEngine engine) {
        lock.lock();
        try {
            engines.push(engine);
            hasFreeEngine.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
