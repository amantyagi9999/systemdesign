package com.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Server {

    private String id;
    private String host;
    private boolean healthy;
    private int port;
    private int weight;
    private AtomicInteger activeConnections = new AtomicInteger(0);

    public Server(String id, String host,boolean healthy, int port, int weight) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.healthy = healthy;
        this.weight = weight;
    }

    public void incrementConnections(){
        activeConnections.incrementAndGet();
    }
    public void decrementConnections(){
        activeConnections.decrementAndGet();
    }

    public boolean healthCheck(){
        return healthy;
    }

    @Override
    public String toString() {
        return String.format("Server[%s, healthy=%s, conns=%d]", id, healthy, activeConnections.get());
    }
}
