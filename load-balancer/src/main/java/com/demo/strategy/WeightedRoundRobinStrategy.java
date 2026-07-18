package com.demo.strategy;

import com.demo.model.Server;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WeightedRoundRobinStrategy implements LoadBalancerStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Server selectServer(List<Server> activeServer) {
        if(activeServer.isEmpty()){
            throw new IllegalArgumentException("No Active Server is available at the moment. Please try again after sometime.");
        }

        int totalWeight = activeServer.stream().mapToInt(Server::getWeight).sum();
        int targetWeight = counter.getAndIncrement() % totalWeight;
        int cumulative = 0;
        for (Server server : activeServer){
            cumulative += server.getWeight();
            if(cumulative >= targetWeight){
                return server;
            }
        }

        return activeServer.getFirst();


    }
}
