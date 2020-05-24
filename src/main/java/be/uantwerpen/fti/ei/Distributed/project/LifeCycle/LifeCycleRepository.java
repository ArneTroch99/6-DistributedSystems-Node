package be.uantwerpen.fti.ei.Distributed.project.LifeCycle;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import be.uantwerpen.fti.ei.Distributed.project.Replication.ReplicationRepository;
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
    private final ReplicationRepository replicationRepository;

    @Autowired
    public LifeCycleRepository(Node node, LifeCycleHTTPSender httpSender, ReplicationRepository replicationRepository) {
        this.node = node;
        this.httpSender = httpSender;
        this.replicationRepository = replicationRepository;
    }

    String getLocalIP() {
        return this.node.getLocalIP();
    }

    void processMulti(String input) {
        if (this.node.calcIDs(input)) {
            this.httpSender.postIP(input, getLocalIP());
            this.replicationRepository.refreshFiles();
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
        if (!neighbours.get(0).equals(node.getLocalIP())) {
            this.httpSender.updateNeighbor(neighbours.get(0), "upper", node.getNextID());
        }
        if(!neighbours.get(1).equals(node.getLocalIP())) {
            this.httpSender.updateNeighbor(neighbours.get(1), "lower", node.getPreviousID());
        }
    }

}
