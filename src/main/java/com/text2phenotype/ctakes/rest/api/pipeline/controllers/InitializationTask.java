package com.text2phenotype.ctakes.rest.api.pipeline.controllers;

import com.text2phenotype.ctakes.rest.api.pipeline.async.AsyncPipelineFramework;
import com.text2phenotype.ctakes.rest.healthcheck.HealthCheckState;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Initialization of Analysis Engines
 */
public class InitializationTask implements Callable<Map<Pipeline,List<String>>> {

    private final AsyncPipelineFramework framework;
    private final Map<Pipeline, AnalysisEngineDescription> descriptionMap;
    private final HealthCheckState state;

    public InitializationTask(AsyncPipelineFramework asyncFramework, Map<Pipeline, AnalysisEngineDescription> pipelinesDescriptionMap, HealthCheckState state) {
        this.descriptionMap = pipelinesDescriptionMap;
        this.framework = asyncFramework;
        this.state = state;
    }

    @Override
    public Map<Pipeline, List<String>> call() throws Exception {
        Thread.sleep(3000);
        Map<Pipeline, List<String>> result = new HashMap<>();
        for (Pipeline pipeline: descriptionMap.keySet()) {
            AnalysisEngineDescription description = descriptionMap.get(pipeline);
            synchronized (this.framework) {
                List<String> steps = this.framework.produceAnalysisEngine(description);
                state.getPipelines().put(pipeline.toString(), "ok");
                result.put(pipeline, steps);
            }
        }
        return result;
    }
}
