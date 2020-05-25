package be.uantwerpen.fti.ei.Distributed.project.Agents;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import be.uantwerpen.fti.ei.Distributed.project.fileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class AgentController {

    private Node node;
    private Map<String, fileProperties> agentList;
    private RestTemplateBuilder restTemplateBuilder;


    @Autowired
    public AgentController(RestTemplateBuilder restTemplateBuilder, Node node){
        this.node = node;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @PostConstruct
    private void syncThread(){
        while(node.getNamingServerIp() == "") {
        }
        Thread agentSync = new Thread(new SyncAgent(this.restTemplateBuilder, this.node));
        agentSync.start();
    }

}
