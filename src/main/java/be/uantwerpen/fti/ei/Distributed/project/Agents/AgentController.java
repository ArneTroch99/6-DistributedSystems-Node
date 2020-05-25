package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import be.uantwerpen.fti.ei.Distributed.project.fileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class AgentController {

    private Node node;
    private Map<String, fileProperties> agentList;

    @Autowired
    public AgentController(Node node){
        this.node = node;
    }

    @PostConstruct
    private void syncThread(){
        Thread agentSync = new Thread(new SyncAgent(this.node));
        agentSync.start();
    }

}
