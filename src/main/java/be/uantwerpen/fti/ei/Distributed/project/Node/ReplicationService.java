package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ReplicationService.class);


    private List<String> localList = new ArrayList<>();
    private RestTemplate restTemplate = new RestTemplate();
    private Node node;

    @Autowired
    public ReplicationService(Node node) {
        this.node = node;
    }

    void saveFile(MultipartFile file, String pathname) throws IOException {
        file.transferTo(Paths.get(pathname, file.getOriginalFilename()));
    }

    void deleteFile(String name, String pathname) {
        new File(pathname, name).delete();
    }

    List<String> listFilesForFolder(final File folder) {
        List<String> filenameList = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filenameList.add(fileEntry.getName());
            }
        }
        return filenameList;
    }

    void checkForNewFiles(final File folder, String nameServerIP) {
        List<String> temp = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()){
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                temp.add(fileEntry.getName());
                if (!localList.contains(fileEntry.getName())) {
                    logger.info("toegevoegd " + fileEntry.getName());
                    replicateFile(fileEntry, nameServerIP);
                    localList.add(fileEntry.getName());
                }
            }
        }
        List<String> removed = new ArrayList<>();
        for (String s : localList) {
            if (!temp.contains(s)) {
                logger.info("verwijder " + (s));
                removed.add(s);
                sendDeleteFile(s, node.getNamingServerIp());
            }
        }
        for (String s : removed){
            localList.remove(s);
        }
    }

    private void sendDeleteFile(String fileName, String nameServerIP) {
        final String namingServerURL = "http://" + nameServerIP + ":8081/fileLocation";
        ResponseEntity<String> nodeIP = restTemplate.getForEntity(namingServerURL, String.class);

        final String nodeURL = "http://" + nodeIP + ":8081/deleteReplicatedFile?fileName=" + fileName;

        restTemplate.put(nodeURL, String.class);
    }


    private void replicateFile(final File file, String nameServerIP) {

        final String namingServerURL = "http://" + nameServerIP + ":8081/fileLocation?filename=" + file.getName();
        ResponseEntity<String> nodeIP = restTemplate.getForEntity(namingServerURL, String.class);

        if (!nodeIP.toString().equals(node.getLocalIP())){
            final String nodeURL = "http://" + nodeIP + ":8081/addReplicatedFile";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(nodeURL, requestEntity, String.class);
            if (!(response.getStatusCodeValue() == 200)) {
                System.out.println("Wrong");
            }
        }
    }
}