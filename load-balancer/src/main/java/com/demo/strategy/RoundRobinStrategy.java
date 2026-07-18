package com.demo.strategy;

import com.demo.model.Server;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoundRobinStrategy implements LoadBalancerStrategy {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Server selectServer(List<Server> activeServer) {
        if(activeServer.isEmpty()){
            throw new IllegalArgumentException("No Active Server is available at the moment. Please try again after sometime.");
        }

        int i = Math.abs(index.getAndIncrement() % activeServer.size());
        return activeServer.get(i);
    }
}
