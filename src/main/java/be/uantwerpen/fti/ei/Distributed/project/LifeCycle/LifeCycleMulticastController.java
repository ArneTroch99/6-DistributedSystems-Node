package be.uantwerpen.fti.ei.Distributed.project.LifeCycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Component
public class LifeCycleMulticastController {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleMulticastController.class);
    private final LifeCycleService service;
    private final int port = 6789;
    private InetAddress multicastGroup;
    private MulticastSocket multicastSocket;

    @Autowired
    public LifeCycleMulticastController(LifeCycleService service) {
        this.service = service;
    }

    @PostConstruct
    public void initialize() {
        logger.info("Initializing Life-Cycle Multicast Controller");
        try {
            multicastGroup = InetAddress.getByName("228.5.6.7");
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(multicastGroup);
            multicastSocket.setLoopbackMode(false);
        } catch (Exception e) {
            logger.info("!An error occured while initializing Life-Cycle Multicast Controller!");
            e.printStackTrace();
        }
        logger.info("Life-Cycle Multicast Controller initialized successfully!");
        new Thread(this::listenMulticast).start();
        new Thread(this::bootstrap).start();
    }

    private void bootstrap() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            logger.info("Sending multicast bootstrap");
            String msg = "@" + service.getLocalIP();
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), multicastGroup, port);
            multicastSocket.send(packet);
            logger.info("Multicast bootstrap was sent successfully!");
        } catch (IOException e) {
            logger.info("!An error occurred while sending multicast bootstrap!");
            e.printStackTrace();
        }
    }

    private void listenMulticast() {
        logger.info("Listening to multicast group " + multicastGroup + " on port " + port);
        boolean listening = true;
        while (listening) {
            try {
                byte[] buffer = new byte[1000];
                DatagramPacket receivedData = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(receivedData);
                String input = new String(receivedData.getData());
                if (input.charAt(0) == '@') {
                    String parsedInput = new String(receivedData.getData()).trim().substring(input.indexOf("@") + 1);
                    if (!parsedInput.equals(service.getLocalIP())) {
                        logger.info("Processing multicast bootstrap from " + parsedInput);
                        service.processMulti(parsedInput);
                    }
                } else {
                    logger.info("!Received unknown multicast input: " + input);
                }
            } catch (IOException e) {
                logger.info("!An error occurred while listening to multicast!");
                e.printStackTrace();
                listening = false;
            }
        }
    }
}



