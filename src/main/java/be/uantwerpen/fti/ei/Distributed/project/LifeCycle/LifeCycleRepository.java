package be.uantwerpen.fti.ei.Distributed.project.LifeCycle;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LifeCycleRepository {

    private final Node node;
    private final LifeCycleHTTPSender httpSender;

    @Autowired
    public LifeCycleRepository(Node node, LifeCycleHTTPSender httpSender) {
        this.node = node;
        this.httpSender = httpSender;
    }

    String getLocalIP() {
        return this.node.getLocalIP();
    }

    void processMulti(String input) {
        if (this.node.calcIDs(input)) {
            this.httpSender.postIP(input, getLocalIP());
        }
    }

    void processPostIP(String input) {
        this.node.calcIDs(input);
    }

    void setNamingServerIP(String ip) {
        this.node.setNamingServerIp(ip);
    }

    void setNextID(int ID) {
        this.node.setNextID(ID);
    }

    void setPreviousID(int ID) {
        this.node.setPreviousID(ID);
    }

    void setOnlyNode() {
        this.node.setNextID(this.node.getCurrentID());
        this.node.setPreviousID(this.node.getCurrentID());
    }

    void shutdown() throws JsonProcessingException {
        String response = this.httpSender.leaveNamingServer(this.node.getNamingServerIp(), this.node.getCurrentID(), this.node.getPreviousID(), this.node.getNextID());
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<String> neighbours = objectMapper.readValue(response, typeFactory.constructCollectionType(List.class, String.class));
        this.httpSender.updateNeighbor(neighbours.get(0), "upper", node.getNextID());
        this.httpSender.updateNeighbor(neighbours.get(1), "lower", node.getPreviousID());
    }

}
