/*
package be.uantwerpen.fti.ei.Distributed.project.Nodepack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class HTTPController {

    private static final Logger logger = LoggerFactory.getLogger(HTTPController.class);
    private final ReplicationService replicationService;
    private final Nodetemp nodetemp;
    private String localPath;
    private String replicatedPath;
    private File localFolder;

    @Autowired
    public HTTPController(Nodetemp nodetemp, ReplicationService replicationService) {
        this.nodetemp = nodetemp;
        this.replicationService = replicationService;
    }

    @PostConstruct
    public void setup() {
        logger.info("Controller setup");
        new File("Replication").mkdir();
        nodetemp.setReplicatedFolder(new File("Replication/replicatedData"));
        localFolder = new File("Replication/localData");
        nodetemp.getReplicatedFolder().mkdir();
        localFolder.mkdir();
        localPath = localFolder.getPath();
        replicatedPath = nodetemp.getReplicatedFolder().getPath();
        logger.info("Completed controller setup!");
    }

    @RequestMapping(value = "/postIP", method = RequestMethod.PUT)
    public ResponseEntity test(@RequestParam(name = "ip") String ip) {
        logger.info("Received unicast to update neighbours from " + ip);
        try {
            nodetemp.calcIDs(ip);
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
            nodetemp.setNamingServerIp(namingServerIP);
            if (amount < 1) {
                nodetemp.onlyNode();
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
                    nodetemp.setNextID(Integer.parseInt(newID));
                    return new ResponseEntity(HttpStatus.OK);
                case "lower":
                    nodetemp.setPreviousID(Integer.parseInt(newID));
                    return new ResponseEntity(HttpStatus.OK);
                default:
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/addReplicatedMapping", method = RequestMethod.PUT)
    public ResponseEntity addReplicatedMapping(@RequestParam("fileName") String filename,
                                               @RequestParam("list") List<String> list) {
        logger.info("Received replicated mapping for file " + filename);
        if(!list.contains(nodetemp.getLocalIP())){
            list.add(nodetemp.getLocalIP());
        }
        nodetemp.addMapping(filename, list);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/addReplicatedFile", method = RequestMethod.POST)
    public ResponseEntity addReplicated(@RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Received replicated file");
        replicationService.saveFile(file, replicatedPath);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/addLocalFile", method = RequestMethod.POST)
    public ResponseEntity addLocal(@RequestParam("file") MultipartFile file) throws IOException {
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

    @RequestMapping(value = "/shutdown", method = RequestMethod.PUT)
    public ResponseEntity shutdown() {
        logger.info("Received request to shutdown node");
        nodetemp.shutdown();
        return new ResponseEntity(HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public FileSystemResource downloadFile(@RequestParam("file") String filename){
        return new FileSystemResource(replicationService.getFile(filename));
    }


    @Scheduled(fixedRate = 500)
    public void checkLocalData() {
        if (nodetemp.getNamingServerIp() != null) {
            replicationService.checkForNewFiles(localFolder, nodetemp.getNamingServerIp());
        }
    }

}
*/
