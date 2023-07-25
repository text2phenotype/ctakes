package com.text2phenotype.ctakes.rest.api.pipeline.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.text2phenotype.ctakes.rest.api.pipeline.async.AsyncPipelineAE;
import com.text2phenotype.ctakes.rest.api.pipeline.async.AsyncPipelineAERepository;
import com.text2phenotype.ctakes.rest.api.pipeline.async.AsyncPipelineFramework;
import com.text2phenotype.ctakes.rest.api.pipeline.async.IFreeCasCallback;
import com.text2phenotype.ctakes.rest.api.pipeline.controllers.response.PipelineTask;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ERRORS;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ExternalResource;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.Text2phenotypeCliOptionals;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb.ResponseModelBuilder;
import com.text2phenotype.ctakes.rest.api.pipeline.model.AnnotationModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ErrorResponseModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.PostOKResponse;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseModel;
import com.text2phenotype.ctakes.rest.healthcheck.HealthCheckState;
import com.text2phenotype.ctakes.rest.healthcheck.HealthCheckStatus;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.resource.FileResourceImpl;
import org.apache.ctakes.core.util.PropertyAeFactory;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.ResourceCreationSpecifierFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.metadata.ExternalResourceBinding;
import org.apache.uima.resource.metadata.ResourceManagerConfiguration;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.ResourceManagerConfiguration_impl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Univarsal controller for all pipeline
 */
@RestController
public class PipelineController implements IFreeCasCallback {

    @Value("${request.timeout:120000}")
    private long requestTimeout;
    @Autowired
    public void setResponseStore(ResponseStore store){
        this.store = store;
    }
    private ResponseStore store;

    @Autowired
    public void setTaskexecutor(ThreadPoolTaskExecutor taskexecutor) {
        this.taskexecutor = taskexecutor;
    }

    private ThreadPoolTaskExecutor taskexecutor;

    @Autowired
    private List<Pipeline> pipelineDescriptions;

    @Autowired
    private AsyncPipelineAERepository repository;

    private Map<Pipeline, List<String>> pipelines = new HashMap<>();

    private TypeSystemDescription typeSystemDescr;

    private HealthCheckState healthCheckState;

    private static final Logger LOGGER = Logger.getLogger(PipelineController.class);
    private static final boolean DEBUG = LOGGER.isDebugEnabled();

    public static final String DATATYPE_PLAIN_TEXT = "plain_text";
    public static final String DATATYPE_CCDA = "ccda";

    private final Map<String, List<ExternalResourceDescription>> external_resource_map = new HashMap<>();

    private ArrayBlockingQueue<JCas> casPool;
    @Autowired
    private void initCasPool(ThreadPoolTaskExecutor threadPool, TypeSystemDescription typeSystemDescr) throws UIMAException, InterruptedException {
        this.casPool = new ArrayBlockingQueue<JCas>(threadPool.getCorePoolSize());
        for (int i=0; i < threadPool.getCorePoolSize(); i++) {
            this.casPool.put(JCasFactory.createJCas(typeSystemDescr));
        }

    }

    private BeanFactory beanFactory;
    @Autowired
    public void setBeanFactory(BeanFactory factory) {
        this.beanFactory = factory;
    }

    @Autowired
    public void setTypeSystemDescr(TypeSystemDescription typeSystemDescr) {
        this.typeSystemDescr = typeSystemDescr;
    }

    @Autowired
    public void inject(HealthCheckState state) {
        this.healthCheckState = state;
    }

    @Autowired
    private AsyncPipelineFramework asyncPipelineFramework;

    /**
     * Initialisation of engines
     * @throws UIMAException
     */
    @PostConstruct
    public void init() throws UIMAException{
        healthCheckState.setStatus(HealthCheckStatus.LIVE);
        // init resource configuration
        ResourceManagerConfiguration conf = new ResourceManagerConfiguration_impl();
        conf.setName(this.getClass().getName());
        for (List<ExternalResourceDescription> resources : external_resource_map.values()) {
            for (ExternalResourceDescription res : resources)
                conf.addExternalResource(res);
        }

        try {
            Map<Pipeline, AnalysisEngineDescription> descriptionMap = new HashMap<>();
            for (Pipeline pipelineDescription: pipelineDescriptions) {

                // Add pipeline to health check list and set state "down" as a default value
                healthCheckState.getPipelines().put(pipelineDescription.toString(), "down");

                Text2phenotypeCliOptionals opts = pipelineDescription.getCli_options();
                if (opts != null) {
                    Map<String, Object> params = opts.getParams();
                    for (String paramName: params.keySet()) {
                        Object paramValue = params.get(paramName);
//                        builder.set(paramName, paramValue);
                        PropertyAeFactory.getInstance().addParameters( paramName, paramValue );
                    }
                }

//                PiperFileReader reader = new PiperFileReader(pipelineDescription.getPiper(), pipelineDescription.getCli_options());
                PiperFileReader reader = new PiperFileReader(pipelineDescription.getPiper());
                PipelineBuilder builder = reader.getBuilder();

                AnalysisEngineDescription description = builder.getAnalysisEngineDesc();
                ResourceCreationSpecifierFactory.setConfigurationParameters(description);
                description.setResourceManagerConfiguration(conf);

                // create resource bindings
                for (String specifierName: description.getDelegateAnalysisEngineSpecifiers().keySet()) {
                    for (String bindTo : external_resource_map.keySet()) {
                        List<ExternalResourceDescription> resources = external_resource_map.get(bindTo);
                        if (bindTo.isEmpty()) {
                            for (ExternalResourceDescription res : resources) {
                                ExternalResourceBinding resBinding = ExternalResourceFactory.createExternalResourceBinding(res.getName(), res);
                                conf.addExternalResourceBinding(resBinding);
                            }
                        } else {
                            if (specifierName.toLowerCase().startsWith(bindTo.toLowerCase())) {
                                for (ExternalResourceDescription res : resources) {
                                    ExternalResourceBinding resBinding = ExternalResourceFactory.createExternalResourceBinding(specifierName + "/" + res.getName(), res);
                                    conf.addExternalResourceBinding(resBinding);
                                }

                            } else {
                                for (ExternalResourceDescription res : resources) {
                                    ExternalResourceBinding resBinding = ExternalResourceFactory.createExternalResourceBinding(bindTo + "/" + res.getName(), res);
                                    conf.addExternalResourceBinding(resBinding);
                                }
                            }
                        }
                    }
                }

                // add descripiton to map
                descriptionMap.put(pipelineDescription, description);

            }

            // run async initialization
            this.taskexecutor.submitListenable(new InitializationTask(asyncPipelineFramework, descriptionMap, healthCheckState)).addCallback(
                    new ListenableFutureCallback<Map<Pipeline, List<String>>>() {
                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error(ex.getMessage());
                            healthCheckState.setError(ex.getMessage());
                            healthCheckState.setStatus(HealthCheckStatus.ERROR);
                        }

                        @Override
                        public void onSuccess(Map<Pipeline, List<String>> result) {
                            pipelines.putAll(result);
//                            result.keySet().forEach(pipeline -> healthCheckState.getPipelines().put(pipeline.toString(), "ok"));
                            healthCheckState.setStatus(HealthCheckStatus.READY);
                        }
                    }
            );

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            healthCheckState.setError(e.getMessage());
            healthCheckState.setStatus(HealthCheckStatus.ERROR);
            throw new UIMAException(e);
        }
    }

    @Autowired
    private Map<String, List<ExternalResourceDescription>> getExternalResourcesMap(List<ExternalResource> resources) {

        // add resources to descriptions
        for (ExternalResource resource : resources) {
            Class<? extends SharedResourceObject> resClass = FileResourceImpl.class;

            String resClassName = resource.getResourceClass();
            if (resClassName != null && !resClassName.isEmpty()) {
                try {
                    resClass = Class.forName(resource.getResourceClass()).asSubclass(SharedResourceObject.class);
                } catch (ClassNotFoundException ex) {
                    continue;
                }
            }

            String path;
            try {
                path = "file:" + FileLocator.getFullPath(resource.getPath());
            } catch (FileNotFoundException e) {
                continue;
            }
            ExternalResourceDescription er = ExternalResourceFactory.createExternalResourceDescription(resource.getName(), resClass, path);

            if (!external_resource_map.containsKey(resource.getBindTo()))
                external_resource_map.put(resource.getBindTo(), new ArrayList<ExternalResourceDescription>());

            external_resource_map.get(resource.getBindTo()).add(er);
        }

        return external_resource_map;
    }

    /**
     * POST request
     * @param dict Dictionary name
     * @param pipelineName Pipeline name
     * @param inputText Text to analyze
     * @param async Asynchronous mode
     * @return
     * @throws Exception
     */
    @PostMapping(path = "/{dict}/{pipeline}", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
    public DeferredResult<ResponseEntity<ResponseData>> post(
            @PathVariable("dict") String dict,
            @PathVariable("pipeline") String pipelineName,
            @RequestParam(value = "inputText", defaultValue = "")  String inputText,
            @RequestParam(value = "async", defaultValue = "False")  boolean async,
            @RequestParam(value = "tid", required = false)  String tid,
            @RequestParam Map<String, String> params) {

        final String finalTid;
        if (tid == null) {
            finalTid = UUID.randomUUID().toString();
        } else {
            finalTid = tid;
        }
        DeferredResult<ResponseEntity<ResponseData>> dfResult = new DeferredResult<>(requestTimeout);

        if (this.healthCheckState.getStatus() != HealthCheckStatus.READY) {
            dfResult.setErrorResult(new ResponseEntity<>(new ErrorResponseModel(ERRORS.SERVICE_IS_NOT_READY), HttpStatus.SERVICE_UNAVAILABLE));
        } else {

            final int charCount = inputText.length();
            final int wordCount = inputText.split("\\s+").length;

            List<String> errors = new ArrayList<>();
            try {

                PriorityQueue<PipelinePriority> candidates = new PriorityQueue<>();

                for (Pipeline pipeline : pipelines.keySet()) {
                    if (pipeline.getName().equals(pipelineName.toLowerCase())) {
                        if (pipeline.getDict().equals(dict.toLowerCase())) {
                            int hits = pipeline.CheckParams(params);
                            candidates.add(new PipelinePriority(pipeline, hits));
                        }
                    }
                }

                if (candidates.size() > 0) {
                    Pipeline pipeline = candidates.peek().pipeline;

                    if (pipeline.getModelBuilder() != null) {
                        ResponseModelBuilder mb = beanFactory.getBean(pipeline.getModelBuilder());
                        mb.initRequestModel();

                        AsyncPipelineAE engine = new AsyncPipelineAE(pipelines.get(pipeline));
                        engine.setRepository(repository);

                        if (async) {
                            if (taskexecutor == null) {
                                errors.add(ERRORS.TASK_EXECUTOR_NOT_FOUND);
                                LOGGER.fatal(ERRORS.TASK_EXECUTOR_NOT_FOUND);
                            } else {
                                ListenableFuture<ResponseEntity<ResponseData>> future = taskexecutor.submitListenable(new PipelineTask(
                                        engine,
                                        inputText,
                                        typeSystemDescr,
                                        mb
                                ));

                                future.addCallback(new FutureCallback(finalTid, dict, pipelineName, store));

                                store.Add(finalTid, dict, pipelineName, null);

                                PostOKResponse result = new PostOKResponse();
                                result.setId(finalTid);
                                dfResult.setResult(new ResponseEntity<>(result, HttpStatus.ACCEPTED));
                            }
                        } else {
                            final JCas jCas = casPool.poll(requestTimeout, TimeUnit.MILLISECONDS);

                            if (jCas == null) {
                                throw new TimeoutException(ERRORS.REQUEST_TIMEOUT_IS_OVER);
                            }
                            jCas.reset();
                            jCas.setDocumentText(inputText);

                            Future taskFuture = taskexecutor.submit(() -> {
                                try {
                                	long startTime = System.nanoTime();
                                    engine.process(jCas);
                                  
                                    ResponseData data = mb.build(jCas);
                                    if (data instanceof ResponseModel) {
                                        ((ResponseModel) data).setDocId(finalTid);
                                    }
                                    dfResult.setResult(new ResponseEntity<>(data, HttpStatus.OK));
                                    
//                                    long taskCount = taskexecutor.getThreadPoolExecutor().getTaskCount() - taskexecutor.getThreadPoolExecutor().getCompletedTaskCount();
//                                    LOGGER.info("Task queue size: " + taskCount);

                                    int poolSize = taskexecutor.getThreadPoolExecutor().getPoolSize();
                                    int activeCount = taskexecutor.getThreadPoolExecutor().getActiveCount();
                                    int queue = taskexecutor.getThreadPoolExecutor().getQueue().size();
                                    LOGGER.info(String.format("Pool: %d Threads: %d Queue: %d", poolSize, activeCount, queue));

                                    LOGGER.info("PipelineTask (" + dict + "," + pipelineName + ") finished in " + ((System.nanoTime() - startTime) / 1000000) +
                                            "ms; document length: " + charCount + "/" + wordCount);
                                } catch (AnalysisEngineProcessException aeEx) {
                                    LOGGER.error(aeEx);
                                } finally {
                                    if (!engine.isDestroyed()) {
                                        onFreeCas(jCas);
                                    }
                                }
                            });

                            dfResult.onTimeout(() -> {
                                try {
                                    engine.destroy();
                                    taskFuture.cancel(true);
                                    this.casPool.put(JCasFactory.createJCas(typeSystemDescr));
                                } catch (Exception uimaex) {
                                    LOGGER.error(uimaex);
                                }
//                                this.onFreeCas(jCas);
                                dfResult.setErrorResult(new ResponseEntity<>(new ErrorResponseModel("Timeout error (" + dict + "," + pipelineName + ")"), HttpStatus.INTERNAL_SERVER_ERROR));
                            });
                        }
                    } else {
                        errors.add(ERRORS.MODEL_BUILDER_NOT_DEFINED);
                        LOGGER.error(ERRORS.MODEL_BUILDER_NOT_DEFINED);
                    }
                } else {
                    errors.add(ERRORS.PIPEINE_NOT_FOUND);
                    LOGGER.error(ERRORS.PIPEINE_NOT_FOUND);
                }
            } catch (Exception e) {
                errors.add(e.getMessage());
                LOGGER.error(dict + "," + pipelineName + ": " + e);
            }
            if (errors.size() > 0) {
                dfResult.setErrorResult(new ResponseEntity<>(new ErrorResponseModel(errors), HttpStatus.BAD_REQUEST));
            }
        }
        return dfResult;
    }

    /**
     * GET request
     * @return
     * @throws Exception
     */
    @GetMapping(path = "/{dict}/{pipeline}/{id}", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
    public ResponseEntity<ResponseData> get(
            @PathVariable("dict") String dict,
            @PathVariable("pipeline") String pipelineName,
            @PathVariable("id") String id) throws Exception {
        return this.store.Get(id, dict, pipelineName);
    }
    
    @GetMapping(path = "/healthcheck")
    public ResponseEntity<String> healthCheck() {

        HttpStatus status = HttpStatus.OK;
        String txt = "OK";
        long t = System.currentTimeMillis();
        try {
            String fullPath = FileLocator.getFullPath("com/text2phenotype/ctakes/resources/healthcheck/healthcheck.txt");
            String testText = new String(Files.readAllBytes(Paths.get(fullPath)));

            Map<String, List<AnnotationModel>> result_map = new HashMap<>();
            for (Pipeline pipeline : this.pipelines.keySet()) {
                String pipelinePath = String.format("%s/%s", pipeline.getDict(), pipeline.getName()).toLowerCase();
                if (HealthCheckUtils.HEALTH_CHECK_PIPELINES.contains(pipelinePath)) {
                    if (pipeline.getModelBuilder() != null) {
                    	LOGGER.info("Checking pipeline: " + pipelinePath);
                    	
                        AsyncPipelineAE engine = new AsyncPipelineAE(pipelines.get(pipeline));
                        engine.setRepository(repository);

                        JCas jCas = casPool.take();
                        try {
                            jCas.reset();
                            jCas.setDocumentText(testText);
                            engine.process(jCas);
                            ResponseModelBuilder mb = beanFactory.getBean(pipeline.getModelBuilder());
                            mb.initRequestModel();

                            result_map.put(pipelinePath, HealthCheckUtils.getHealthCheckData(mb.build(jCas)));
                        } finally {
                            casPool.put(jCas);
                        }
                    }

                    if (System.currentTimeMillis() - t > requestTimeout) {
                        throw new TimeoutException(String.format("Pipeline too slow: %s/%s", pipeline.getDict(), pipeline.getName()));
                    }
                }

            }

            ObjectMapper mapper = new ObjectMapper();
            txt = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result_map);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            txt = e.getMessage();
        }
        return new ResponseEntity<>(txt, status);
    }

    @Override
    public void onFreeCas(JCas jCas) {
        try {
        	jCas.reset();
            casPool.put(jCas);

        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private class PipelinePriority implements Comparable<PipelinePriority> {
        public Pipeline pipeline;
        private int hits;
        public PipelinePriority(Pipeline p, int hitsCount) {
            this.pipeline = p;
            this.hits = hitsCount;
        }

        @Override
        public int compareTo(PipelinePriority o) {
            return Integer.compare(o.hits, this.hits);
        }
    }

    private class FutureCallback implements ListenableFutureCallback<ResponseEntity<ResponseData>> {

        private ResponseStore store;
        private String id;
        private String dict;
        private String pipeline;

        public FutureCallback(String id, String dict, String pipeline, ResponseStore store) {
            this.id = id;
            this.store = store;
            this.dict = dict;
            this.pipeline = pipeline;
        }

        @Override
        public void onFailure(Throwable throwable) {
        	LOGGER.error("Callback error for (" + this.id + ", " + this.dict + ", " + this.pipeline + "):");
        	
        	throwable.printStackTrace();
        	
        	store.Add(this.id, this.dict, this.pipeline, new ResponseEntity<ResponseData>(HttpStatus.BAD_REQUEST));
        }

        @Override
        public void onSuccess(ResponseEntity<ResponseData> responseData) {
            ResponseData body = responseData.getBody();
            if (body instanceof ResponseModel) {
                // set tid as doc id
                ((ResponseModel)body).setDocId(this.id);
            }
            store.Add(this.id, this.dict, this.pipeline, responseData);
        }
    }

}
