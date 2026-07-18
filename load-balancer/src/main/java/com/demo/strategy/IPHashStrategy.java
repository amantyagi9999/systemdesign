package com.demo.strategy;

import com.demo.model.Server;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IPHashStrategy implements LoadBalancerStrategy {
    String clientIp;

    public Server selectServer(List<Server> activeServer, String clientIp) {
        if(activeServer.isEmpty()){
            throw new IllegalArgumentException("No Active Server is available at the moment. Please try again after sometime.");
        }

        int hash = Math.abs(clientIp.hashCode());
        int index = hash % activeServer.size();
        return activeServer.get(index);
    }

    @Override
    public Server selectServer(List<Server> activeServer) {
        throw new UnsupportedOperationException("IPHashStrategy requires client IP — use overload");
    }
}
