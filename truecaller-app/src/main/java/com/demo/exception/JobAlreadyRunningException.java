package com.demo.exception;

import org.springframework.http.HttpStatus;

public class JobAlreadyRunningException extends ApiException {

    public JobAlreadyRunningException() {
        super("JOB_ALREADY_RUNNING",
                "The name-resolution batch job is already running - wait for it to finish before triggering again",
                HttpStatus.CONFLICT);
    }
}
