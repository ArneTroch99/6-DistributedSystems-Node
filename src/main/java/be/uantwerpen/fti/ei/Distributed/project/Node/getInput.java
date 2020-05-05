package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Scanner;

@Component
public class getInput {

    private static final Logger logger = LoggerFactory.getLogger(getInput.class);

    private final Node node;

    @Autowired
    public getInput(Node node) {
        this.node = node;
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
                    node.shutdown();
                    running = false;
                }
            }
        });
        t.start();
    }
}
