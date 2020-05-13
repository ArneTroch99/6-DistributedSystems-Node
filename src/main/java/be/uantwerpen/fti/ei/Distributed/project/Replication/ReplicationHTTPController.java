package be.uantwerpen.fti.ei.Distributed.project.Replication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ReplicationHTTPController {

    private final static Logger logger = LoggerFactory.getLogger(ReplicationHTTPController.class);
    private final ReplicationService replicationService;

    @Autowired
    ReplicationHTTPController(ReplicationService replicationService) {
        this.replicationService = replicationService;
    }

    @RequestMapping(value = "/addReplicatedFile", method = RequestMethod.POST)
    public ResponseEntity addReplicated(@RequestParam("file") MultipartFile file,
                                        @RequestParam("fileLog") String fileLog,
                                        HttpServletRequest request) {
        logger.info("Received replicated file from " + request.getRemoteAddr());
        replicationService.saveFile(file, fileLog);
        logger.info("Replicated file saved successfully!");
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteReplicatedFile", method = RequestMethod.PUT)
    public ResponseEntity deleteReplicated(@RequestParam("fileName") String name) {
        logger.info("Received request to delete file " + name);
        replicationService.deleteFile(name);
        logger.info("File deleted successfully!");
        return new ResponseEntity(HttpStatus.OK);
    }

}
