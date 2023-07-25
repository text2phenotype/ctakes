package com.text2phenotype.ctakes.rest.api.pipeline.controllers;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ErrorResponseModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import com.text2phenotype.ctakes.rest.api.pipeline.model.response.StatusResponseModel;

@Service
public class ResponseStore {

    private final ErrorResponseModel error = new ErrorResponseModel("There is no data for this Id");
    private final StatusResponseModel processing = new StatusResponseModel("The request is processing", HttpStatus.PROCESSING.value());

    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private final boolean DEBUG = LOGGER.isDebugEnabled();

    private static final long Timeout = 60000;

    private Map<String, ResponseStoreData> dataM = new HashMap<>();
    private Queue<ResponseTimeStamp> timeQ = new ArrayDeque<>();

    /**
     * Add new response data to the store
     */
    public synchronized void Add(String id, String dict, String pipeline, ResponseEntity<ResponseData> data){
        ResponseData d = null;
        if (data != null && data.getStatusCode() == HttpStatus.OK) {
            d = data.getBody();
        }
        ResponseStoreData storeData = new ResponseStoreData(dict, pipeline, d);
        this.dataM.put(id, storeData);

        Date now = new Date();
        ResponseTimeStamp time = new ResponseTimeStamp(id, now);
        this.timeQ.offer(time);

    }

    /**
     * Get the response data from the store by Id
     */
    public synchronized ResponseEntity<ResponseData> Get(String Id, String dict, String pipeline) throws ExecutionException, InterruptedException {
        if (!dataM.containsKey(Id)){
        	if (DEBUG)  LOGGER.debug("Response not found for " + Id);
        	
        	// TODO: this is a stop-gap to guard against running on a cluster.
        	// should be remove once a dedicated response store is implemented
        	return new ResponseEntity<>(processing, HttpStatus.OK);
        }

        ResponseStoreData data = dataM.get(Id);
        if (data.data == null) {
            return new ResponseEntity<>(processing, HttpStatus.OK);
        }

        if (!data.dict.equals(dict) || !data.pipeline.equals(pipeline)) {
        	if (DEBUG)  LOGGER.debug("Response " + Id + ": (" + data.dict + " != " + dict + ") or (" + 
        			data.pipeline + " != " + pipeline + ")");
        	
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        
        this.dataM.remove(Id);
        
        return new ResponseEntity<>(data.data, HttpStatus.OK);
    }

    /**
     * Clear the store
     */
    public synchronized void Reset() {
        this.dataM.clear();
        this.timeQ.clear();
    }

    /**
     * Remove old data
     */
    @Scheduled(fixedRate = Timeout)
    public void Refresh() {
    	Date now = new Date();

        while (timeQ.size() > 0 && (now.getTime() - timeQ.peek().date.getTime() > Timeout)) {
            ResponseTimeStamp time = timeQ.poll();
            this.dataM.remove(time.id);
            if (DEBUG)  LOGGER.debug("Response timeout: " + time.id);
        }
    }

    private class ResponseTimeStamp {
        private String id;
        private Date date;

        public ResponseTimeStamp(String id, Date date) {
            this.id = id;
            this.date = date;
        }
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    private class ResponseStoreData {
        private String dict;
        private String pipeline;
        private ResponseData data;

        public ResponseStoreData(String dict, String pipeline, ResponseData data) {
            this.dict = dict;
            this.pipeline = pipeline;
            this.data = data;
        }

        public String getDict() {
            return dict;
        }

        public void setDict(String dict) {
            this.dict = dict;
        }

        public String getPipeline() {
            return pipeline;
        }

        public void setPipeline(String pipeline) {
            this.pipeline = pipeline;
        }

        public ResponseData getData() {
            return data;
        }

        public void setData(ResponseData data) {
            this.data = data;
        }
    }
}
