package be.uantwerpen.fti.ei.Distributed.project.Agents;

import java.io.Serializable;

public class FailureAgent implements Serializable, Runnable{

    private int failingID, currentID;
    private int startID;

    FailureAgent(int failingID, int currentID){
        this.currentID = currentID;
        this.failingID = failingID;
    }

    @Override
    public void run() {

    }
}
