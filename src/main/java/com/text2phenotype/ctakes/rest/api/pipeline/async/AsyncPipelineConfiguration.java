package com.text2phenotype.ctakes.rest.api.pipeline.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration of async pipeline repository
 */
public class AsyncPipelineConfiguration {

    @Autowired
    public void setTaskexecutor(ThreadPoolTaskExecutor taskexecutor) {
        this.taskexecutor = taskexecutor;
    }

    private ThreadPoolTaskExecutor taskexecutor;

    public Map<String, AsyncPipelineLayerConfiguration> getLayersConfiguration() {
        return layersConfiguration;
    }

    public void setLayersConfiguration(Map<String, AsyncPipelineLayerConfiguration> layersConfiguration) {
        this.layersConfiguration = layersConfiguration;
    }

    private Map<String, AsyncPipelineLayerConfiguration> layersConfiguration;

    public int GetLayerSizeByimplementationName(String implName) {
        if (layersConfiguration!= null && layersConfiguration.containsKey(implName)) {
            return layersConfiguration.get(implName).getMaxLayerSize();
        } else {
            if (taskexecutor != null) {
                return taskexecutor.getCorePoolSize();
            } else {
                return 1;
            }
        }
    }
}
