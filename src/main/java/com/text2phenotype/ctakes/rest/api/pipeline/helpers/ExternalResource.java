package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

/**
 * External resource
 */
public class ExternalResource {

    // Fields
    private String name;
    private String path;
    private String bindTo;
    private String resourceClass;

    // Getters
    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public String getBindTo() {
        return bindTo;
    }
    public String getResourceClass() {
        return resourceClass;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public void setBindTo(String bindTo) {
        this.bindTo = bindTo;
    }
    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }
}
