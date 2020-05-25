package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AgentController {

    private int nodeID;

    @Autowired
    public AgentController(Node node){
        this.nodeID = node.getCurrentID();
    }

    @PostConstruct
    private void syncThread(){
        Thread agentSync = new Thread(new SyncAgent(this.nodeID));
        agentSync.start();
    }

}
