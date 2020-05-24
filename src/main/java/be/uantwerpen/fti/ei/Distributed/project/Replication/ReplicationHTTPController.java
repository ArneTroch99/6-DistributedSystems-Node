package be.uantwerpen.fti.ei.Distributed.project.Replication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
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

    @ResponseBody
    @RequestMapping(value = "/getfiles", method = RequestMethod.GET)
    public List<List<String>> getFiles() {
        logger.info("Received request for all files");
        return this.replicationService.getFiles();
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(name = "filename") String filename) {
        logger.info("Received request to download file " + filename);
        try {
            File file = this.replicationService.getFile(filename);
            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource;

            resource = new ByteArrayResource(Files.readAllBytes(path));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException | NullPointerException e) {
            logger.info("!An error occurred while trying to download file " + filename + "!");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}
