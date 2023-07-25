package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

public final class ERRORS {

    private ERRORS(){
        throw new AssertionError();
    }

    public static final String MODEL_BUILDER_NOT_DEFINED = "Model builder is not defined";
    public static final String MODEL_BUILDER_INTERNAL_ERROR = "Can't create an output JSON. For more details see server logs";
    public static final String UNKNOWN = "Unknown error";
    public static final String UNKNOWN_DATATYPE = "Unknown request data type";

    public static final String PIPEINE_NOT_FOUND = "The pipeline with such parameters was not found";
    public static final String TASK_EXECUTOR_NOT_FOUND = "The task executor was not found";

    public static final String SERVICE_IS_NOT_READY = "The service is not ready. Try to repeat your request later";
    public static final String REQUEST_TIMEOUT_IS_OVER = "Request timeout is over";
}
