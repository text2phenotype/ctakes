package com.text2phenotype.ctakes.rest.api.pipeline.async;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.NameValuePair;
import org.apache.uima.util.InvalidXMLException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Repository of Analysis Engines of async pipelines
 */
@Component
@Scope("singleton")
public class AsyncPipelineAERepository {

    @Autowired
    private AsyncPipelineConfiguration config;
    final private Logger LOGGER = Logger.getLogger( "AsyncPipelineAERepository" );
    private Map<String, AsyncPipelineLayer> layers = new HashMap<>();

    public synchronized void InitLayer(AnalysisEngineDescription description) throws ResourceInitializationException {
        try {
            if (!description.isPrimitive()) {
                for (String className : description.getDelegateAnalysisEngineSpecifiers().keySet()) {
                    AnalysisEngineDescription resource = (AnalysisEngineDescription) description.getDelegateAnalysisEngineSpecifiers().get(className);
                    if (resource.getResourceManagerConfiguration() == null)
                        resource.setResourceManagerConfiguration(description.getResourceManagerConfiguration());
                    InitLayer(resource);
                }
            } else {
                String layerKey = GetKey(description);
                if (!layers.containsKey(layerKey)) {
                    int count = config.GetLayerSizeByimplementationName(description.getAnnotatorImplementationName());
                    AsyncPipelineLayer layer = new AsyncPipelineLayer(description, count);
                    layer.Init();
                    layers.put(layerKey, layer);
                }
            }
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    public static String GetKey(AnalysisEngineDescription description) throws ClassNotFoundException {
        StringBuilder result = new StringBuilder();
        result.append(description.getAnnotatorImplementationName());

        Set<String> configParams = GetConfigurationParameters(description.getAnnotatorImplementationName());
        NameValuePair[] pairs = description.getAnalysisEngineMetaData().getConfigurationParameterSettings().getParameterSettings();
        for (NameValuePair pair: pairs) {
            if (configParams.contains(pair.getName())) {
                result.append(String.format("-%s+%s", pair.getName(), pair.getValue().toString()));
            }
        }

        return result.toString();
    }

    private static Set<String> GetConfigurationParameters(String implementationName) throws ClassNotFoundException {
        Set<String> result = new HashSet<>();
        Iterator FieldsItr = ReflectionUtil.getFields(Class.forName(implementationName)).iterator();

        while(FieldsItr.hasNext()) {
            Field field = (Field)FieldsItr.next();
            if (ConfigurationParameterFactory.isConfigurationParameterField(field)) {
                ConfigurationParameter annotation = (ConfigurationParameter)ReflectionUtil.getAnnotation(field, ConfigurationParameter.class);
                String parameterName = ConfigurationParameterFactory.getConfigurationParameterName(field);
                result.add(parameterName);
            }
        }

        return result;
    }

    public AsyncPipelineLayer GetLayer(String layerKey) {
        return layers.get(layerKey);
    }
}
