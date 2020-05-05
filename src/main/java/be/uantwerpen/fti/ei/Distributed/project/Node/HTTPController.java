package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HTTPController {

    private static final Logger logger = LoggerFactory.getLogger(HTTPController.class);
    private final Node node;

    @Autowired
    public HTTPController(Node node) {
        this.node = node;
    }

    @RequestMapping(value = "/postIP", method = RequestMethod.PUT)
    public ResponseEntity test(@RequestParam(name = "ip") String ip) {
        logger.info("Received unicast to update neighbours from " + ip);
        try {
            node.calcIDs(ip);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/bootstrap", method = RequestMethod.PUT)
    public ResponseEntity receiveBootstrap(@RequestParam(name = "namingip") String namingServerIP,
                                           @RequestParam(name = "nodes") int amount) {
        logger.info("Received ip address of the Naming Server " + namingServerIP);
        try {
            node.setNamingServerIp(namingServerIP);
            if (amount < 1) {
                node.onlyNode();
            }
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e){
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
                    node.setNextID(Integer.parseInt(newID));
                    return new ResponseEntity(HttpStatus.OK);
                case "lower":
                    node.setPreviousID(Integer.parseInt(newID));
                    return new ResponseEntity(HttpStatus.OK);
                default:
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e){
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
