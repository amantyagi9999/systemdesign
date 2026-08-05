package com.demo.api;

import com.demo.dto.DefaultApiResponse;
import com.demo.dto.NameResolutionJobStatusDto;
import com.demo.service.NameResolutionJobTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caller/")
@RequiredArgsConstructor
public class AdminJobController {

    private final NameResolutionJobTriggerService jobTriggerService;

    /** POST /api/v1/admin/jobs/name-resolution - kick off a run. */
    @GetMapping("name-resolution")
    public ResponseEntity<DefaultApiResponse> trigger() {
        NameResolutionJobStatusDto started = jobTriggerService.trigger();
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Job started")
                .data(started)
                .code(HttpStatus.OK.value()).build());
    }

    /** GET /api/v1/admin/jobs/name-resolution - check current/last run status. */
    @GetMapping
    public ResponseEntity<DefaultApiResponse> status() {
        NameResolutionJobStatusDto statusDto = jobTriggerService.getStatus();

        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Job Status")
                .data(statusDto)
                .code(HttpStatus.OK.value()).build());
    }
}
