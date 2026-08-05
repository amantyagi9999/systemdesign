package com.demo.service;

import com.demo.dto.NameResolutionJobStatusDto;
import com.demo.exception.JobAlreadyRunningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Triggers NameResolutionService.run() on a background thread within the
 * SAME JVM as the running Spring Boot app - no spark-submit, no separate
 * jar, no external process. Start the app, hit the API, the job runs.
 *
 * Only one run is allowed at a time (guarded by an in-memory flag). For a
 * multi-instance deployment, back this with a distributed lock (e.g. a
 * Redis SETNX, same pattern as the cache-stampede lock) so two app
 * instances can't trigger overlapping runs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NameResolutionJobTriggerService {

    private final NameResolutionService nameResolutionService;

    private final AtomicReference<NameResolutionJobStatusDto> status =
            new AtomicReference<>(NameResolutionJobStatusDto.builder().state(NameResolutionJobStatusDto.State.IDLE).build());

    public NameResolutionJobStatusDto trigger() {
        NameResolutionJobStatusDto current = status.get();
        if (current.getState() == NameResolutionJobStatusDto.State.RUNNING) {
            throw new JobAlreadyRunningException();
        }

        NameResolutionJobStatusDto started = NameResolutionJobStatusDto.builder()
                .state(NameResolutionJobStatusDto.State.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();
        status.set(started);

        runAsync();
        return started;
    }

    public NameResolutionJobStatusDto getStatus() {
        return status.get();
    }

    @Async
    void runAsync() {
        LocalDateTime startedAt = status.get().getStartedAt();
        try {
            int resolved = nameResolutionService.run();
            status.set(NameResolutionJobStatusDto.builder()
                    .state(NameResolutionJobStatusDto.State.SUCCEEDED)
                    .startedAt(startedAt)
                    .finishedAt(LocalDateTime.now())
                   // .numbersResolved(resolved)
                    .build());
        } catch (Exception ex) {
            log.error("Name resolution job failed", ex);
            status.set(NameResolutionJobStatusDto.builder()
                    .state(NameResolutionJobStatusDto.State.FAILED)
                    .startedAt(startedAt)
                    .finishedAt(LocalDateTime.now())
                   // .errorMessage(ex.getMessage())
                    .build());
        }
    }
}
