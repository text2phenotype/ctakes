package com.text2phenotype.ctakes.rest.api.pipeline.async;

public class AsyncPipelineLayerConfiguration {
    public static int CORES = Runtime.getRuntime().availableProcessors();

    // by default max layer size is equal to cores count
    private int maxLayerSize = CORES;

    public int getMaxLayerSize() {
        return maxLayerSize;
    }

    public void setMaxLayerSize(int maxLayerSize) {
        this.maxLayerSize = maxLayerSize;
    }
}
