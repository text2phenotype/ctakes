package com.text2phenotype.ctakes.rest.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private HealthCheckState state;

    @Autowired
    private void inject(HealthCheckState stateValue){
        this.state = stateValue;
    }

    @GetMapping(path = "/live", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
    public ResponseEntity<String> checkLive() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping(path = "/ready", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
    public ResponseEntity<HealthCheckState> checkReady() {
        HttpStatus status = HttpStatus.OK;
        if (state.getStatus() == HealthCheckStatus.UNAVAILABLE || state.getStatus() == HealthCheckStatus.ERROR) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return new ResponseEntity<>(state, status);
    }
}
