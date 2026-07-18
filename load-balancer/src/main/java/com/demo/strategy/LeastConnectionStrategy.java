package com.demo.strategy;

import com.demo.model.Server;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class LeastConnectionStrategy implements  LoadBalancerStrategy {
    @Override
    public Server selectServer(List<Server> activeServer) {
        if(activeServer.isEmpty()){
            throw new IllegalArgumentException("No Active Server is available at the moment. Please try again after sometime.");
        }

        return activeServer.stream()
                .min(Comparator.comparingInt(s -> s.getActiveConnections().get()))
                .orElseThrow(() -> new IllegalArgumentException("No server available"));
    }
}