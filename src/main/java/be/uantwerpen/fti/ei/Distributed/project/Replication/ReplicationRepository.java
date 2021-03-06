package be.uantwerpen.fti.ei.Distributed.project.Replication;

import be.uantwerpen.fti.ei.Distributed.project.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReplicationRepository {
    private final Files files;
    private final ReplicationHTTPSender sender;
    private final Node node;
    private final Logger logger = LoggerFactory.getLogger(ReplicationRepository.class);

    @Autowired
    ReplicationRepository(Files files, ReplicationHTTPSender sender, Node node) {
        this.files = files;
        this.sender = sender;
        this.node = node;
    }

    void saveFile(MultipartFile file, String fileLog) {
        if (!files.getLocalReplicated().contains(file.getOriginalFilename())) {
            this.files.addReplicatedFile(file);
            this.files.addFileLog(file.getOriginalFilename(), fileLog);
        }
    }

    public void refreshFiles() {
        for (final File fileEntry : files.getReplicatedFolder().listFiles()) {
            this.sender.replicateFile(fileEntry, node.getNamingServerIp(), false);
        }
    }

    void deleteFile(String filename) {
        if (this.files.getFileLogs().get(filename).size() > 1) {
            int id = this.files.getFileLogs().get(filename).get(this.files.getFileLogs().get(filename).size() - 2);
            if (!(id == (node.getCurrentID()))) {
                sender.deleteFileByID(filename, id, node.getNamingServerIp());
            }
        }
        this.files.removeFileLog(filename);
        new File(files.getReplicatedFolder(), filename).delete();
    }

    File getFile(String filename) {
        for (final File fileEntry : files.getReplicatedFolder().listFiles()) {
            if (fileEntry.getName().equals(filename)){
                return fileEntry;
            }
        }
        for (final File fileEntry : files.getLocalFolder().listFiles()) {
            if (fileEntry.getName().equals(filename)){
                return fileEntry;
            }
        }
        return null;
    }

    void shutdown() {
        for (String filename : files.getLocalReplicated()) {
            sender.deleteFile(filename, node.getNamingServerIp());
        }
        for (final File fileEntry : files.getReplicatedFolder().listFiles()) {
            List<Integer> logs = files.getFileLogs().get(fileEntry.getName());
            logs.remove(logs.indexOf(node.getCurrentID()));
            sender.sendFile(fileEntry, sender.getNodeIP(node.getPreviousID(), node.getNamingServerIp()));
        }
    }

    List<List<String>> getFiles(){
        List<String> replicated = new ArrayList<>(listFilesForFolder(files.getReplicatedFolder()));
        List<String> local = new ArrayList<>(listFilesForFolder(files.getLocalFolder()));
        List<List<String>> temp = new ArrayList<>();
        temp.add(replicated);
        temp.add(local);
        return temp;
    }


    @Scheduled(fixedRate = 500, initialDelay = 2000)
    void checkFolders() {
        try {
            List<String> temp = new ArrayList<>();
            for (final File fileEntry : files.getLocalFolder().listFiles()) {
                if (!fileEntry.isDirectory()) {
                    temp.add(fileEntry.getName());
                    if (!files.getLocalReplicated().contains(fileEntry.getName())) {
                        logger.info("Found new file, replicating...");
                        sender.replicateFile(fileEntry, node.getNamingServerIp(), true);
                        logger.info("Replication of new file was succesfull!");
                    }
                }
            }
            List<String> removed = new ArrayList<>();
            for (String filename : files.getLocalReplicated()) {
                if (!temp.contains(filename)) {
                    logger.info("Found deleted file, deleting...");
                    removed.add(filename);
                    sender.deleteFile(filename, node.getNamingServerIp());
                    logger.info("Deletion of new file was successfull!");
                }
            }
            for (String s : removed) {
                files.removeFromLocalReplicated(s);
            }
        } catch (Exception e) {
            logger.info("!An error occurred while trying to check for file changes!");
            e.printStackTrace();
        }

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

}
