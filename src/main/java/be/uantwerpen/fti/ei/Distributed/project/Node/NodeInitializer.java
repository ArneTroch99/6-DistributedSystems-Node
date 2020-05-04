package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Scanner;
import org.slf4j.Logger;

@Component
public class NodeInitializer {

    private static final Logger logger = LoggerFactory.getLogger(NodeInitializer.class);
    private static final String address = "228.5.6.7";
    private static final int port = 6789;

    @PostConstruct
    public void init() {
        logger.info("Initializing node");

       /* Node node = new Node(address, port);

        UnicastListener unicastListener = new UnicastListener(7890, node);
        new Thread(unicastListener).start();
        MulticastListenerThread listener = new MulticastListenerThread(address, port, node);
        new Thread(listener).start();

        node.bootstrap();

        while (true) {
            String scan = new Scanner(System.in).nextLine();

            if (scan.equals("stop"))
                break;
            if (scan.equals("shut")) {
                System.out.println("shutting");
                node.shut();
            }
        }

        unicastListener.halt();
        listener.stop();*/

    }
}
