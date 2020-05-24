package be.uantwerpen.fti.ei.Distributed.project.LifeCycle;

import be.uantwerpen.fti.ei.Distributed.project.Replication.ReplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class LifeCycleHTTPController {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleHTTPController.class);
    private final LifeCycleService service;
    private final ReplicationService replicationService;

    @Autowired
    public LifeCycleHTTPController(LifeCycleService service, ReplicationService replicationService){
        this.service = service;
        this.replicationService = replicationService;
    }

    @PostConstruct
    public void initialize(){
        logger.info("Initializing Life-Cycle HTTP Controller");
        logger.info("Life-Cycle HTTP Controller initialized successfully!");
    }

    @RequestMapping(value = "/postIP", method = RequestMethod.PUT)
    public ResponseEntity postIP(@RequestParam(name = "ip") String ip) {
        logger.info("Received unicast to update neighbours from " + ip);
        try {
            service.processPostIP(ip);
            logger.info("Unicast to update neighbous from " + ip + " handled successfully!");
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            logger.info("!An error occurred while handling postIP message from " + ip + "!");
            e.printStackTrace();
            failure();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/bootstrap", method = RequestMethod.PUT)
    public ResponseEntity receiveBootstrap(@RequestParam(name = "namingip") String namingServerIP,
                                           @RequestParam(name = "nodes") int amount) {
        logger.info("Received ip address of the Naming Server " + namingServerIP + " and amount of nodes: " + amount);
        try {
            service.setNamingServerIP(namingServerIP);
            if (amount < 1) {
                service.setOnlyNode();
                logger.info("IP address of the naming server and previous+nextID were updated successfully!");
            }
            logger.info("IP address of the naming server was updated successfully!");
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            logger.info("!An error occured while handling bootstrap from NamingServer!");
            e.printStackTrace();
            failure();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/updateNeighbors", method = RequestMethod.PUT)
    public ResponseEntity updateNeighbors(@RequestParam(name = "what") String what,
                                          @RequestParam(name = "id") String newID) {
        logger.info("Received command to change " + what + " neighbor to " + newID);
        try {
            switch (what) {
                case "upper":
                    service.setNextID(newID);
                    return new ResponseEntity(HttpStatus.OK);
                case "lower":
                    service.setPreviousID(newID);
                    return new ResponseEntity(HttpStatus.OK);
                default:
                    logger.info("!Received a wrong request!");
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.info("!An error occurred while updating neighbours!");
            e.printStackTrace();
            failure();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/shutdown", method = RequestMethod.PUT)
    public ResponseEntity shutdown(){
        logger.info("Received command to shutdown this node!");
        try {
            replicationService.shutdown();
            service.shutdown();
            logger.info("The node was shutdown successfully!");
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            });
            t.start();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            logger.info("!An error occurred while trying to shutdown this node!");
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping()

    private void failure(){
        try {
            logger.info("Attempting graceful shutdown");
            service.shutdown();
            logger.info("Graceful shutdown was successful!");
        } catch (JsonProcessingException e1) {
            logger.info("!An error occurred during graceful shutdown! We're screwed.");
            e1.printStackTrace();
        }
    }
}
