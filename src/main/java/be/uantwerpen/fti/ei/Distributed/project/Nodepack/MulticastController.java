/*
package be.uantwerpen.fti.ei.Distributed.project.Nodepack;

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
    private final Nodetemp nodetemp;
    private InetAddress multicastGroup;
    private MulticastSocket multicastSocket;

    @Autowired
    public MulticastController(Nodetemp nodetemp) {
        this.nodetemp = nodetemp;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing multicast");
        try {
            multicastGroup = InetAddress.getByName(nodetemp.getMulticastGroup());
            multicastSocket = new MulticastSocket(nodetemp.getPort());
            multicastSocket.joinGroup(multicastGroup);
            multicastSocket.setLoopbackMode(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread t = new Thread(this::listenMulticast);
        t.start();
        t = new Thread(this::bootstrap);
        t.start();
    }

    private void bootstrap() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String msg = "@" + nodetemp.getLocalIP();
            logger.info("Sending bootstrap message: " + msg);
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), multicastGroup, nodetemp.getPort());
            multicastSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenMulticast() {
        logger.info("Listening to multicast group " + multicastGroup + " on port " + nodetemp.getPort());
        boolean listening = true;
        while (listening) {
            try {
                byte[] buffer = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(recv);
                String input = new String(recv.getData());
                nodetemp.processMulti(input);
            } catch (IOException e) {
                e.printStackTrace();
                listening = false;
            }
        }
    }

}
*/
