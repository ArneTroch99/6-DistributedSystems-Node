package be.uantwerpen.fti.ei.Distributed.project;

public class fileProperties {
    public int fileNodeID;
    public Boolean fileLock;

    public fileProperties(int nodeID, Boolean lock){
        this.fileNodeID = nodeID;
        this.fileLock = lock;
    }
}
