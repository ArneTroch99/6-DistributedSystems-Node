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

@Controller
public class UnicastController {

private static final Logger logger = LoggerFactory.getLogger(UnicastController.class);
    private final Node node;

    @Autowired
    public UnicastController(Node node) {
        this.node = node;
    }

    @RequestMapping(value = "postIP", method = RequestMethod.PUT)
    public ResponseEntity test(@RequestParam(name = "ip") String ip){
        logger.info("Received unicast to update neighbours from " + ip);
        node.calcIDs(ip);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/bootstrap", method = RequestMethod.PUT)
    public ResponseEntity receiveBootstrap(@RequestParam(name = "namingip") String namingServerIP){
        System.out.println(namingServerIP);
        return new ResponseEntity(HttpStatus.OK);

    }

}
