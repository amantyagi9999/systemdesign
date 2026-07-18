package com.demo.orchestrator;

import com.demo.health.HealthChecker;
import com.demo.model.Server;
import com.demo.strategy.LoadBalancerStrategy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoadBalancer {

    private final List<Server> servers = new CopyOnWriteArrayList<>();
    private LoadBalancerStrategy strategy;
    private final HealthChecker healthChecker;

    public LoadBalancer(LoadBalancerStrategy loadBalancerStrategy) {
        this.strategy = loadBalancerStrategy;
        this.healthChecker = new HealthChecker(servers, 10);
        healthChecker.start();
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    public void removeServer(String serverId) {
        servers.removeIf(server -> server.getId().equals(serverId));
    }

    public void setStrategy(LoadBalancerStrategy loadBalancerStrategy) {
        this.strategy = loadBalancerStrategy;
    }

    public Server getNextServer(){
        List<Server> activeServer = servers.stream().filter(Server::isHealthy).toList();
        if(activeServer.isEmpty()){
            throw new IllegalArgumentException("No active servers");
        }

        Server chosen = strategy.selectServer(activeServer);
        chosen.incrementConnections();
        return chosen;
    }

    public void releaseServer(Server server) {
        server.decrementConnections();
    }

    public void shutdown() {
        healthChecker.stop();
    }
}

