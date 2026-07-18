package com.demo.health;

import com.demo.model.Server;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthChecker {

    private final List<Server> servers;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final int intervalSec;

    public HealthChecker(List<Server> servers, int intervalSec) {
        this.servers = servers;
        this.intervalSec = intervalSec;
    }

    public void start(){
        scheduledExecutorService.scheduleAtFixedRate(()-> {
            for (Server ser : servers) {
                boolean check = ser.healthCheck();
                ser.setHealthy(check);
            }
        }, 0, intervalSec, TimeUnit.SECONDS);
    }

    public void stop(){
        scheduledExecutorService.shutdown();
    }
}

