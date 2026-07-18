package com.demo.strategy;

import com.demo.model.Server;

import java.util.*;

public interface LoadBalancerStrategy {

    Server selectServer(List<Server> activeServer);

}
