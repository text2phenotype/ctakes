package com.text2phenotype.ctakes.rest.api.pipeline.async;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("singleton")
public class AsyncPipelineFramework {
    @Autowired
    private AsyncPipelineAERepository repository;

    public List<String> produceAnalysisEngine(ResourceSpecifier aSpecifier)
            throws ResourceInitializationException {
        try {
            AnalysisEngineDescription descr = (AnalysisEngineDescription)aSpecifier;
            repository.InitLayer(descr);

            List<String> aSeq = new ArrayList<>();
            if (descr.isPrimitive()) {
                aSeq.add(AsyncPipelineAERepository.GetKey(descr));
            } else {
                for (String key : descr.getDelegateAnalysisEngineSpecifiers().keySet()) {
                    AnalysisEngineDescription d = (AnalysisEngineDescription)descr.getDelegateAnalysisEngineSpecifiers().get(key);
                    if (d.getResourceManagerConfiguration() == null)
                        d.setResourceManagerConfiguration(descr.getResourceManagerConfiguration());
                    aSeq.addAll(produceAnalysisEngine(d));
                }
            }
            return aSeq;
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }
}
