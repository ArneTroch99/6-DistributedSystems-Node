package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import be.uantwerpen.fti.ei.Distributed.project.fileProperties;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SyncAgent extends Agent {

    private final File localFolder = new File("Replication/LocalData");
    private File replicatedFolder = new File("Replication/ReplicatedData");
    private final int nodeID;
    private final HashMap<String, fileProperties> agentList;

    public SyncAgent(Node node) {
        this.nodeID = node.getCurrentID();
        this.agentList = node.getFileList();
    }


    @Override
    protected void setup(){
        AMSAgentDescription[] agents = null;
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults((long) -1);
            agents = AMSService.search(this, new AMSAgentDescription(), c);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                List<String> localFileNames = new ArrayList<>();
                File[] localFiles = localFolder.listFiles();
                Boolean changed = false;
                if (localFiles.length > 0) {
                    for (File file : localFiles) {
                        String fileName = file.getName();
                        localFileNames.add(fileName);
                        if (!agentList.containsKey(fileName)) {
                            changed = true;
                            agentList.put(fileName, new fileProperties(nodeID, false));
                            System.out.println("File " + fileName + " was added");
                        }
                    }
                }
                for (Map.Entry<String, fileProperties> entryVals : agentList.entrySet()) {
                    if (entryVals.getValue().fileNodeID == nodeID && !localFileNames.contains(entryVals.getKey())) {
                        changed = true;
                        agentList.remove(entryVals.getKey());
                        System.out.println("File " + entryVals.getKey() + " was removed");
                    }
                }

                if(changed) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    try {
                        msg.setContentObject(agentList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AMSAgentDescription[] agents = null;
                }
            }
        });
    }
}