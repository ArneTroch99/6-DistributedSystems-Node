package be.uantwerpen.fti.ei.Distributed.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);
    }
}
