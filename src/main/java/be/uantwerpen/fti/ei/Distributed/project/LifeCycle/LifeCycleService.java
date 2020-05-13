package be.uantwerpen.fti.ei.Distributed.project.LifeCycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LifeCycleService {

    private final LifeCycleRepository repository;

    @Autowired
    public LifeCycleService(LifeCycleRepository repository) {
        this.repository = repository;
    }

    String getLocalIP() {
        return this.repository.getLocalIP();
    }

    void processMulti(String input) {
        this.repository.processMulti(input);
    }

    void processPostIP(String input) {
        this.repository.processPostIP(input);
    }

    void setNamingServerIP(String ip) {
        this.repository.setNamingServerIP(ip);
    }

    void setNextID(String ID) {
        this.repository.setNextID(Integer.parseInt(ID));
    }

    void setPreviousID(String ID) {
        this.repository.setPreviousID(Integer.parseInt(ID));
    }

    void setOnlyNode() {
        this.repository.setOnlyNode();
    }

    public void shutdown() throws JsonProcessingException {
        this.repository.shutdown();
    }

}
