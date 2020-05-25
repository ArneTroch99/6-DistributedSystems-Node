package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import be.uantwerpen.fti.ei.Distributed.project.fileProperties;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

public class SyncAgent extends Agent {

    private final File localFolder = new File("Replication/LocalData");
    private File replicatedFolder = new File("Replication/ReplicatedData");
    private final Node node;
    private final int nodeID;
    private String nextIP;
    private final HashMap<String, fileProperties> agentList;
    private final RestTemplate restTemplate;

    public SyncAgent(RestTemplateBuilder restTemplateBuilder, Node node) {
        this.node = node;
        this.nodeID = node.getCurrentID();
        this.restTemplate = restTemplateBuilder.build();
        this.agentList = node.getFileList();
    }


    @Override
    protected void setup(){
        while(node.getNamingServerIp() == "" || node.getNextID() == 0) {
            System.out.println(node.getNamingServerIp());
            System.out.println(node.getNextID());
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

                if(!changed) {
                    final String namingServerURL = "http://" + node.getNamingServerIp() + ":8081/nodeip?id=" + node.getNextID();
                    ResponseEntity<String> nextNodeIP = restTemplate.getForEntity(namingServerURL, String.class);
                    nextIP = nextNodeIP.getBody();
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    //msg.setContentObject(agentList);
                    msg.setContent("Test");
                    AID nextAgent = new AID(Integer.toString((node.getNextID())), AID.ISGUID);
                    nextAgent.addAddresses("http://" + nextIP + ":7778/acc");
                    msg.addReceiver(nextAgent);
                    msg.setConversationId("ABC");
                    send(msg);
                    System.out.println("\nMessage to " + nextAgent);
                }
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("ABC");
                ACLMessage msg = receive(mt);
                if(msg != null){
                    System.out.println("message received" + msg);
                    if(msg.getPerformative()== ACLMessage.REQUEST)
                    {
                        String content = msg.getContent();
                        if ((content != null))
                        {
                            System.out.println("Received Request from " + msg.getSender().getLocalName());
                            System.out.println("Received Message : " + content);
                        }
                        else
                        {
                            block();
                        }
                    }
                }
                else
                {
                    block();
                }
            }
        });
    }
}