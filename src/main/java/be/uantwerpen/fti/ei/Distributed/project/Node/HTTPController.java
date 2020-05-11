package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@RestController
public class HTTPController {

    private static final Logger logger = LoggerFactory.getLogger(HTTPController.class);
    private final ReplicationService replicationService;
    private final Node node;
    private String localPath;
    private String replicatedPath;
    private File localFolder;

    @Autowired
    public HTTPController(Node node, ReplicationService replicationService) {
        this.node = node;
        this.replicationService = replicationService;
    }

    @PostConstruct
    public void setup() {
        logger.info("Controller setup");
        new File("Replication").mkdir();
        File replication = new File("Replication/replicatedData");
        localFolder = new File("Replication/localData");
        replication.mkdir();
        localFolder.mkdir();
        localPath = localFolder.getPath();
        replicatedPath = replication.getPath();
        logger.info("Completed controller setup!");
    }

    @RequestMapping(value = "/postIP", method = RequestMethod.PUT)
    public ResponseEntity test(@RequestParam(name = "ip") String ip) {
        logger.info("Received unicast to update neighbours from " + ip);
        try {
            node.calcIDs(ip);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/bootstrap", method = RequestMethod.PUT)
    public ResponseEntity receiveBootstrap(@RequestParam(name = "namingip") String namingServerIP,
                                           @RequestParam(name = "nodes") int amount) {
        logger.info("Received ip address of the Naming Server " + namingServerIP + " and amount of nodes: " + amount);
        try {
            node.setNamingServerIp(namingServerIP);
            if (amount < 1) {
                node.onlyNode();
            }
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
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
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/addReplicatedFile", method = RequestMethod.POST)
    public ResponseEntity addReplicated(MultipartFile file) throws IOException {
        logger.info("Received replicated file");
        replicationService.saveFile(file, replicatedPath);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/addLocalFile", method = RequestMethod.POST)
    public ResponseEntity addLocal(MultipartFile file) throws IOException {
        logger.info("Received local file");
        replicationService.saveFile(file, localPath);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteReplicatedFile", method = RequestMethod.PUT)
    public ResponseEntity deleteReplicated(@RequestParam("fileName") String name) {
        logger.info("Received request to delete file");
        replicationService.deleteFile(name, replicatedPath);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Scheduled(fixedRate = 500)
    public void checkLocalData() {
        if (node.getNamingServerIp() != null) {
            replicationService.checkForNewFiles(localFolder, node.getNamingServerIp());
        }
    }

}
