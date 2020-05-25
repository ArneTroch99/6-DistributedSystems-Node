package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.fileProperties;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class SyncAgent implements Serializable, Runnable {

    private File localFolder = new File("Replication/LocalData");
    private File replicatedFolder = new File("Replication/ReplicatedData");
    private int nodeID;
    private Map<String, fileProperties> agentList;

    public SyncAgent(int nodeID, Map<String, fileProperties> nodeList){
        this.nodeID = nodeID;
        this.agentList = nodeList;
    }

    @Override
    public void run() {
        while (true) {
            List<String> localFileNames = new ArrayList<String>();
            File[] localFiles = localFolder.listFiles();
            if (localFiles.length > 0)
                for (File file : localFiles) {
                    String fileName = file.getName();
                    localFileNames.add(fileName);
                    if (!agentList.containsKey(fileName)) {
                        agentList.put(fileName, new fileProperties(this.nodeID, false));
                        System.out.println("File " + fileName + " was added");
                    }

                }
            for (Map.Entry<String, fileProperties> entryVals : agentList.entrySet()) {
                if (entryVals.getValue().fileNodeID == this.nodeID && !localFileNames.contains(entryVals.getKey())) {
                    agentList.remove(entryVals.getKey());
                    System.out.println("File " + entryVals.getKey() + " was removed");
                }
            }
        }
    }
}