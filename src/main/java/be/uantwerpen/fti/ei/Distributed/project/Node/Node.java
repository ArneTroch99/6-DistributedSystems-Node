package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class Node {

    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final String multicastGroup = "228.5.6.7";
    private final int port = 6789;
    private final HTTPClient httpClient;
    private String localIP;
    private int currentID;
    private int nameport = 7895;
    private int uniport = 7890;
    private int nextID = 0;
    private int previousID = 0;


    @Autowired
    public Node(HTTPClient httpClient) {
        this.httpClient = httpClient;
    }

    @PostConstruct
    private void initHost() {
        logger.info("Initalizing Node");
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface n: Collections.list(nets)) {
                if (n.getDisplayName().equals("Eth0")) {
                    for (InetAddress ip : Collections.list(n.getInetAddresses()))
                    localIP = ip.toString();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        logger.info("Initialized node with local ip " + localIP + " and hash " + (currentID = hash(localIP)));
    }


    void processMulti(String input) {
        logger.info("Processing multicast input: " + input);
        input = input.trim();
        String slpInput = input.substring(input.indexOf("@") + 1);
        if (!slpInput.equals(localIP)) {
            if (calcIDs(localIP))
                HTTPClient.postIP(localIP, slpInput);
        }
    }

    public boolean calcIDs(String name) {
        int nodeHash = hash(name);
        boolean state = false;
        if (previousID == nextID && (nextID == currentID || nextID == 0)) {
            nextID = nodeHash;
            previousID = nodeHash;
            state = true;
        } else if ((currentID < nodeHash && nodeHash < nextID)) {
            nextID = nodeHash;
            state = true;
        } else if ((currentID > nodeHash && nodeHash > previousID)) {
            previousID = nodeHash;
            state = true;
        }
        if (state) {
            logger.info("Changed ID's: nextID = " + nextID + ", previousID = " + previousID);
        }
        return state;
    }

   /* void processUni(String msg) {
        char firstChar = msg.charAt(0);

        switch (firstChar) {
            case '$':
                msg = msg.substring(1).trim();
                String[] data = msg.split("%");
                int numberOfNodes = Integer.parseInt(data[0].trim());
                ipName = data[1];
                System.out.println("Namingserver ip: " + ipName);
                if (numberOfNodes < 1) {
                    nextID = currentID;
                    previousID = currentID;
                }
                break;
            case '#':
                msg = msg.substring(1).trim();
                calcIDs(msg);
                break;
            case '~':
                msg = msg.substring(1).trim();
                String[] data2 = msg.split("%");
                System.out.println("1 " + data2[0] + " 2 " + data2[1]);
                sendUni("&" + nextID, data2[0].trim(), uniport);
                sendUni("@" + previousID, data2[1].trim(), uniport);
                System.out.println("sent ips");
                break;
            case '&':
                msg = msg.substring(1).trim();
                nextID = Integer.parseInt(msg);
                break;
            case '@':
                msg = msg.substring(1).trim();
                previousID = Integer.parseInt(msg);
                break;
        }
        System.out.println("after uni: nextID " + nextID + " previousID " + previousID);
    }*/

    /*public void process(String ip, String name) {
        if (!ip.equals(localIP)) {
            if (calcIDs(name.trim()))
                sendUni("#" + this.localName, ip, uniport);

            System.out.println("after multi: nextID " + nextID + " previousID " + previousID);
        }
    }

    public void shut() {
        String msg = "~" + previousID + "%" + nextID + "%" + localName;
        sendUni(msg, ipName, nameport);
        System.out.println("sending shut message");
    }

    private boolean sendUni(String msg, String ip, int sendPort) {
        try {
            DatagramSocket ds = new DatagramSocket();

            InetAddress ipDest = InetAddress.getByName(ip.trim());
            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), ipDest, sendPort);
            ds.send(dp);
            ds.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendMulti(String msg) {
        try {
            // join multicast group
            InetAddress group = InetAddress.getByName(multicastGroup);
            MulticastSocket s = new MulticastSocket(socket);
            s.joinGroup(group);

            // build packet and multicast send it
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, socket);
            s.send(packet);

            // leave group
            s.leaveGroup(group);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }*/

    private int hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);

            int temp = 32768;
            return no.mod(BigInteger.valueOf(temp)).intValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    String getMulticastGroup() {
        return multicastGroup;
    }

    int getPort() {
        return port;
    }

    String getLocalIP() {
        return localIP;
    }
}