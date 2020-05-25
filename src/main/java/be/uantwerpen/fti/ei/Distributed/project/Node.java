package be.uantwerpen.fti.ei.Distributed.project;

import be.uantwerpen.fti.ei.Distributed.project.Agents.SyncAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Component
public class Node {

    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private String localIP;
    private int currentID;
    private int nextID = 0;
    private int previousID = 0;
    private String namingServerIp = "";
    private Map<String, fileProperties> fileList = new HashMap<>();


    @PostConstruct
    public void initialize() {
        logger.info("Initializing node");
        try {
            this.localIP = InetAddress.getLocalHost().getHostAddress();
            currentID = hash(localIP);
            logger.info("Node initialized successfully! LocalIP = " + localIP + ", currentID = " + currentID);
        } catch (UnknownHostException e) {
            logger.info("!An error occured while initializing node!");
            e.printStackTrace();
        }
    }

    public synchronized boolean calcIDs(String name) {
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
        } else if ((previousID > currentID && (nodeHash > previousID || nodeHash < currentID))) {
            previousID = nodeHash;
            state = true;
        } else if ((nextID < currentID && (nodeHash < nextID || nodeHash > currentID))) {
            nextID = nodeHash;
            state = true;
        }
        if (state) {
            logger.info("Changed ID's: previousID = " + previousID + ", nextID = " + nextID);
        }
        return state;
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

    public String getLocalIP() {
        return localIP;
    }

    public String getNamingServerIp() {
        return namingServerIp;
    }

    public void setNamingServerIp(String namingServerIp) {
        logger.info("Changing NamingServerIP to " + namingServerIp);
        this.namingServerIp = namingServerIp;
    }

    public int getCurrentID() {
        return currentID;
    }

    public void setCurrentID(int currentID) {
        this.currentID = currentID;
    }

    public int getNextID() {
        return nextID;
    }

    public void setNextID(int nextID) {
        this.nextID = nextID;
    }

    public int getPreviousID() {
        return previousID;
    }

    public void setPreviousID(int previousID) {
        this.previousID = previousID;
    }
}
