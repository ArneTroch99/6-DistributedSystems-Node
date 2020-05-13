package be.uantwerpen.fti.ei.Distributed.project;

import be.uantwerpen.fti.ei.Distributed.project.LifeCycle.LifeCycleService;
import be.uantwerpen.fti.ei.Distributed.project.Replication.ReplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Scanner;

@Component
public class getInput {

    private static final Logger logger = LoggerFactory.getLogger(getInput.class);
    private final LifeCycleService lifeCycleService;
    private final ReplicationService replicationService;

    @Autowired
    public getInput(LifeCycleService lifeCycleService, ReplicationService replicationService) {
        this.lifeCycleService = lifeCycleService;
        this.replicationService = replicationService;
    }

    @PostConstruct
    private void input() {
        Thread t = new Thread(() -> {
            logger.info("Listening for shutdown command");
            boolean running = true;
            while (running) {
                Scanner sc = new Scanner(System.in);
                String input = sc.nextLine();
                if (input.trim().equals("shutdown")) {
                    logger.info("Received shutdown command");
                    try {
                        lifeCycleService.shutdown();
                        replicationService.shutdown();
                    } catch (JsonProcessingException e) {
                        logger.info("!An error occurred while trying to shutdown this node!");
                        e.printStackTrace();
                    }
                    logger.info("The node was shutdown successfully!");
                    running = false;
                    System.exit(0);
                }
            }
        });
        t.start();
    }
}
