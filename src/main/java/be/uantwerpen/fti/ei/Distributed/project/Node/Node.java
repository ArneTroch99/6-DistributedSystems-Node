package be.uantwerpen.fti.ei.Distributed.project.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
import java.util.List;

@Component
public class Node {

    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final String multicastGroup = "228.5.6.7";
    private final int port = 6789;
    private final HTTPClient httpClient;
    private String localIP;
    private String namingServerIp;
    private int currentID;
    private int nextID = 0;
    private int previousID = 0;

    @Autowired
    public Node(HTTPClient httpClient) {
        this.httpClient = httpClient;
    }

    @PostConstruct
    private void initNode() {
        logger.info("Initalizing Node");
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface n : Collections.list(nets)) {
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
        input = input.trim();
        String slpInput = input.substring(input.indexOf("@") + 1);
        if (!slpInput.equals(localIP)) {
            logger.info("Processing multicast input: " + input);
            if (calcIDs(slpInput))
                httpClient.putIP(localIP, slpInput);
        }
    }

    boolean calcIDs(String name) {
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
        } else if ((previousID > currentID) && (nodeHash > previousID)){
            previousID = nodeHash;
        } else if ((nextID < currentID) && (nodeHash < nextID)){
            nextID = nodeHash;
        }
        if (state) {
            logger.info("Changed ID's: nextID = " + nextID + ", previousID = " + previousID);
        }
        return state;
    }

    void shutdown() {
        logger.info("Received shutdown command");
        String ips = httpClient.leave(Integer.toString(currentID), namingServerIp, Integer.toString(nextID), Integer.toString(previousID));
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        try {
            List<String> someClassList = objectMapper.readValue(ips, typeFactory.constructCollectionType(List.class, String.class));
            logger.info("Left the naming server");
            httpClient.updateNeighbor(someClassList.get(0), Integer.toString(nextID), "upper");
            httpClient.updateNeighbor(someClassList.get(1), Integer.toString(previousID), "lower");
            logger.info("Updated neighbors");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    void onlyNode() {
        nextID = currentID;
        previousID = currentID;
    }

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

    void setNamingServerIp(String namingServerIp) {
        this.namingServerIp = namingServerIp;
    }

    void setNextID(int nextID) {
        this.nextID = nextID;
    }

    void setPreviousID(int previousID) {
        this.previousID = previousID;
    }
}