package be.uantwerpen.fti.ei.Distributed.project;

import java.io.Serializable;

public class fileProperties implements Serializable {
    public int fileNodeID;
    public Boolean fileLock;

    public fileProperties(int nodeID, Boolean lock){
        this.fileNodeID = nodeID;
        this.fileLock = lock;
    }
}
