package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import be.uantwerpen.fti.ei.Distributed.project.fileProperties;
import jade.core.Agent;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class SyncAgent extends Agent implements Serializable, Runnable {

    private final File localFolder = new File("Replication/LocalData");
    private File replicatedFolder = new File("Replication/ReplicatedData");
    private final int nodeID;
    private final Map<String, fileProperties> agentList;

    public SyncAgent(Node node){
        this.nodeID = node.getCurrentID();
        this.agentList = node.getFileList();
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