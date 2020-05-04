package be.uantwerpen.fti.ei.Distributed.project.Node;

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
public class MulticastController {

    private static final Logger logger = LoggerFactory.getLogger(MulticastController.class);
    private final Node node;
    private InetAddress multicastGroup;
    private MulticastSocket multicastSocket;

    @Autowired
    public MulticastController(Node node) {
        this.node = node;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing multicast");
        try {
            multicastGroup = InetAddress.getByName(node.getMulticastGroup());
            multicastSocket = new MulticastSocket(node.getPort());
            multicastSocket.joinGroup(multicastGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }
        discovery();
        listenMulticast();
    }

    private void discovery() {
        try {
            String msg = "@" + node.getLocalIP();
            logger.info("Sending discovery message: " + msg);
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), multicastGroup, node.getPort());
            multicastSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenMulticast() {
        logger.info("Listening to multicast group " + multicastGroup + " on port " + node.getPort());
        boolean listening = true;
        while (listening) {
            try {
                byte[] buffer = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buffer, buffer.length);

                multicastSocket.receive(recv);

                String input = new String(recv.getData());
                node.processMulti(input);
            } catch (IOException e) {
                e.printStackTrace();
                listening = false;
            }
        }
    }

}
