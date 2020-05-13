package be.uantwerpen.fti.ei.Distributed.project.Replication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReplicationService {

    private final ReplicationRepository repository;

    @Autowired
    ReplicationService(ReplicationRepository repository) {
        this.repository = repository;
    }

    void saveFile(MultipartFile file, String fileLog) {
        this.repository.saveFile(file, fileLog);
    }

    void deleteFile(String filename) {
        this.repository.deleteFile(filename);
    }

    public void shutdown() {
        this.repository.shutdown();
    }

}
