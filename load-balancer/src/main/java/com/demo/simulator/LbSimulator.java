package com.demo.simulator;

import com.demo.model.Server;
import com.demo.orchestrator.LoadBalancer;
import com.demo.strategy.LeastConnectionStrategy;
import com.demo.strategy.RoundRobinStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class LbSimulator implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        LoadBalancer loadBalancer = new LoadBalancer(new RoundRobinStrategy());
        loadBalancer.addServer(new Server("s1", "10.0.0.1",true, 8080, 1));
        loadBalancer.addServer(new Server("s2", "10.0.0.2",false,8080, 1));
        loadBalancer.addServer(new Server("s3", "10.0.0.3",true, 8080, 1));

        ExecutorService clients = Executors.newFixedThreadPool(10);
        for(int i= 0; i< 20; i++){
            clients.submit(() -> {
                Server server = loadBalancer.getNextServer();
                System.out.println("Selected server: " + server);
                loadBalancer.releaseServer(server);
            });
        }

        clients.shutdown();
        clients.awaitTermination(1, TimeUnit.MINUTES);

        loadBalancer.setStrategy(new LeastConnectionStrategy());
        System.out.println("Switched to least connection strategy");
        loadBalancer.shutdown();

    }
}
