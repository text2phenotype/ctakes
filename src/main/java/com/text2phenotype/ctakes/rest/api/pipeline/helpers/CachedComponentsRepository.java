package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import org.apache.uima.analysis_component.AnalysisComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for AnalysisComponents
 */
public class CachedComponentsRepository {

    private static CachedComponentsRepository instance;

    public static CachedComponentsRepository getInstance() {
        if (instance == null) {
            instance = new CachedComponentsRepository();
        }

        return instance;
    }

    /**
     * cached components
     */
    private Map<Integer, AnalysisComponent> components;

    private CachedComponentsRepository() {

        components = new HashMap<>();
    }

    /**
     * Checks if the repository contains the component by hash key
     * @param componentKey Hash code of component
     * @return
     */
    public boolean hasComponent(Integer componentKey) {

        return components.containsKey(componentKey);
    }

    /**
     * Gets the component from the repository
     * @param componentKey Hash code of component
     * @return
     */
    public AnalysisComponent getComponent(Integer componentKey) {

        return components.get(componentKey);
    }

    /**
     * Adds to repository
     * @param componentKey Hash code of component
     * @param component Component instance
     * @return
     */
    public boolean addComponent(Integer componentKey, AnalysisComponent component) {
        if (!hasComponent(componentKey)) {
            components.put(componentKey, component);
            return true;
        }

        return false;
    }
}
