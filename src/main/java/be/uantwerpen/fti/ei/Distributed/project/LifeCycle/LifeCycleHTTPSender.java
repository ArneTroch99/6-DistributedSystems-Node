package be.uantwerpen.fti.ei.Distributed.project.LifeCycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LifeCycleHTTPSender {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LifeCycleHTTPSender.class);

    public LifeCycleHTTPSender(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    void postIP(String destinationIP, String localIP){
        try {
            logger.info("Sending postIP PUT request to " + destinationIP);
            String url = "http://" + destinationIP + ":8082/postIP?ip=" + localIP;
            restTemplate.put(url, String.class);
            logger.info("postIP PUT request was sent successfully to " + destinationIP + "!");
        } catch (Exception e){
            logger.info("!An error occurred while sending postIP PUT request to " + destinationIP);
            e.printStackTrace();
        }
    }

    String leaveNamingServer(String destinationIP, int nodeID, int lower, int upper) {
        try {
            logger.info("Sending leave message to naming server");
            String url = "http://" + destinationIP + ":8082/leave?id=" + nodeID + "&lower=" + lower + "&upper=" + upper;
            String response =  restTemplate.getForObject(url, String.class);
            logger.info("Successfully left the naming server!");
            return response;
        } catch (Exception e){
            logger.info("!An error occurred while trying to leave the naming server!");
            e.printStackTrace();
            return null;
        }
    }

    void updateNeighbor(String destinationIP, String what, int newID) {
        try {
            logger.info("Sending updateNeighbour message to " + destinationIP);
            String url = "http://" + destinationIP + ":8082/updateNeighbors?what=" + what + "&id=" + newID;
            restTemplate.put(url, String.class);
            logger.info("updateNeighbour message was successfully sent to " + destinationIP + "!");
        } catch (Exception e) {
            logger.info("!An error occurred while sending updateNeighbour message to " + destinationIP + "!");
            e.printStackTrace();
        }
    }
}
