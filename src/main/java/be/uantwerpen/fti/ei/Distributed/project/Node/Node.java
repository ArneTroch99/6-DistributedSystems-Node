package be.uantwerpen.fti.ei.Distributed.project.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Node {

    // TODO: 11/05/2020 Hele hoop shit aanpassen voor de filemapping: zelf verwijderen enzo, gaat nooit werken in de huidige staat

    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final ReplicationService replicationService;
    private final String multicastGroup = "228.5.6.7";
    private final int port = 6789;
    private final HTTPClient httpClient;
    private String localIP;
    private String namingServerIp;
    private int currentID;
    private int nextID = 0;
    private int previousID = 0;
    private File replicatedFolder;
    private Map<String, List<String>> fileMapping = new HashMap<>();

    @Autowired
    public Node(HTTPClient httpClient, ReplicationService replicationService) {
        this.httpClient = httpClient;
        this.replicationService = replicationService;
    }

    @PostConstruct
    private void initNode() {
        logger.info("Initalizing Node");
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
            if (calcIDs(slpInput)) replicationService.updateAll(replicatedFolder, getNamingServerIp());
            httpClient.putIP(localIP, slpInput);
        }
    }

    synchronized boolean calcIDs(String name) {
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
            String previousIP = httpClient.getIP(previousID, namingServerIp);
            for (final File fileEntry : replicatedFolder.listFiles()) {
                replicationService.sendFile(fileEntry, previousIP);
            }
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

    public String getNamingServerIp() {
        return namingServerIp;
    }

    void setNextID(int nextID) {
        this.nextID = nextID;
    }

    void setPreviousID(int previousID) {
        this.previousID = previousID;
    }

    public File getReplicatedFolder() {
        return replicatedFolder;
    }

    public void setReplicatedFolder(File replicatedFolder) {
        this.replicatedFolder = replicatedFolder;
    }

    public void addMapping(String filename, List<String> list){
        fileMapping.put(filename, list);
    }

    public Map<String, List<String>> getFileMapping() {
        return fileMapping;
    }
}