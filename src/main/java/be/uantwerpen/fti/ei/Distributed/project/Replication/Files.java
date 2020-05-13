package be.uantwerpen.fti.ei.Distributed.project.Replication;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Files {

    private final static Logger logger = LoggerFactory.getLogger(File.class);
    private final Node node;
    private final ObjectMapper mapper = new ObjectMapper();
    private File localFolder;
    private File replicatedFolder;
    private Map<String, List<Integer>> fileLogs = new HashMap<>();
    private List<String> replicatedFiles = new ArrayList<>();

    @Autowired
    Files(Node node){
        this.node = node;
    }

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing Files");
            new File("Replication").mkdir();
            this.setReplicatedFolder(new File("Replication/ReplicatedData"));
            this.setLocalFolder(new File("Replication/LocalData"));
            this.replicatedFolder.mkdir();
            this.localFolder.mkdir();
            logger.info("Successfully initialized Files!");
        } catch (Exception e) {
            logger.info("!An error occurred while initializing Files!");
            e.printStackTrace();
        }
    }

    public void addReplicatedFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        try {
            logger.info("Saving new replicated file: " + filename);
            file.transferTo(Paths.get(replicatedFolder.getPath(), filename));
            logger.info("file " + filename + " was saved successfully!");
        } catch (IOException e) {
            logger.info("!An error occurred while trying to save file " + filename);
            e.printStackTrace();
        }
    }

    public void addFileLog(String fileName, String fileLog) {
        try {
            logger.info("Saving logs of file " + fileName);
            List<Integer> log = mapper.readValue(fileLog, new TypeReference<List<Integer>>() {
            });
            if(!log.contains(node.getCurrentID())){
                log.add(node.getCurrentID());
            }
            fileLogs.put(fileName, log);
            logger.info("Successfully saved logs of file " + fileName);
        } catch (JsonProcessingException e) {
            logger.info("!An error occurred while trying to save logs of file " + fileName);
            e.printStackTrace();
        }
    }

    public void removeFileLog(String fileName){
        logger.info("Removing log of file " + fileName);
        fileLogs.remove(fileName);
    }

    public File getLocalFolder() {
        return localFolder;
    }

    public void setLocalFolder(File localFolder) {
        this.localFolder = localFolder;
    }

    public File getReplicatedFolder() {
        return replicatedFolder;
    }

    public void setReplicatedFolder(File replicatedFolder) {
        this.replicatedFolder = replicatedFolder;
    }

    public List<String> getReplicatedFiles() {
        return replicatedFiles;
    }

    public void addToReplicatedFiles(String fileName){
        replicatedFiles.add(fileName);
    }

    public void removeFromReplicatedFiles(String fileName){
        replicatedFiles.remove(fileName);
    }

    public Map<String, List<Integer>> getFileLogs() {
        return fileLogs;
    }
}
