package be.uantwerpen.fti.ei.Distributed.project.Replication;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReplicationHTTPSender {

    private final static Logger logger = LoggerFactory.getLogger(ReplicationHTTPSender.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final Node node;
    private final Files files;

    public ReplicationHTTPSender(RestTemplateBuilder restTemplateBuilder, Node node, Files files) {
        this.restTemplate = restTemplateBuilder.build();
        this.node = node;
        this.files = files;
    }

    void replicateFile(final File file, String nameServerIP) {
        logger.info("Asking for location to put file " + file.getName());
        final String namingServerURL = "http://" + nameServerIP + ":8082/fileLocation?filename=" + file.getName();
        ResponseEntity<String> nodeIP = restTemplate.getForEntity(namingServerURL, String.class);
        logger.info("Location for file " + file.getName() + " received, sending file");
        files.addToLocalReplicated((file.getName()));
        sendFile(file, nodeIP.getBody());
    }

    String getNodeIP(int ID, String nameServerIP) {
        logger.info("Asking for the ip address of node " + ID);
        final String namingServerURL = "http://" + nameServerIP + ":8081/nodeip?id=" + ID;
        ResponseEntity<String> nodeIP = restTemplate.getForEntity(namingServerURL, String.class);
        logger.info("Received ip address of node " + ID);
        return nodeIP.getBody();
    }


    void deleteFile(String filename, String nameServerIP) {
        logger.info("Asking for location to delete file " + filename);
        final String namingServerURL = "http://" + nameServerIP + ":8081/fileLocation?filename=" + filename;
        ResponseEntity<String> nodeIP = restTemplate.getForEntity(namingServerURL, String.class);
        logger.info("Location for file " + filename + " received, deleting file");
        if (nodeIP.getBody().equals(node.getLocalIP())) {
            logger.info("Location is this node!");
        } else {
            final String nodeURL = "http://" + nodeIP.getBody() + ":8082/deleteReplicatedFile?fileName=" + filename;
            restTemplate.put(nodeURL, String.class);
            logger.info("Request to delete file was sent successfully!");
        }
    }

    void deleteFileByID(String filename, int ID, String nameServerIP) {
        logger.info("Asking for ip address of node " + ID);
        final String namingServerURL = "http://" + nameServerIP + ":8081/nodeip?id=" + ID;
        ResponseEntity<String> nodeIP = restTemplate.getForEntity(namingServerURL, String.class);
        logger.info("Location for file " + filename + " received, deleting file");
        final String nodeURL = "http://" + nodeIP.getBody() + ":8082/deleteReplicatedFile?fileName=" + filename;
        restTemplate.put(nodeURL, String.class);
        logger.info("Request to delete file was sent successfully!");
    }

    void sendFile(File file, String nodeIP) {
        if (!nodeIP.equals(node.getLocalIP())) {
            String nodeURL = "http://" + nodeIP + ":8082/addReplicatedFile";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("form-data")
                    .name("file")
                    .filename(file.getName())
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<File> fileEntity = new HttpEntity<>(file, fileMap);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);
            try {
                if (files.getFileLogs().containsKey(file.getName())) {
                    body.add("fileLog", mapper.writeValueAsString(files.getFileLogs().get(file.getName())));
                } else {
                    List<Integer> temp = new ArrayList<>();
                    body.add("fileLog", mapper.writeValueAsString(temp));
                }
            } catch (JsonProcessingException e) {
                logger.info("!An error occurred while tryin to write logs as json!");
                e.printStackTrace();
            }
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(nodeURL, HttpMethod.POST, requestEntity, String.class);

            if (!(response.getStatusCodeValue() == 200)) {
                logger.info("!Received wrong status code!");
            } else {
                logger.info("File and logs were sent successfully!");
            }
        }
    }

}
