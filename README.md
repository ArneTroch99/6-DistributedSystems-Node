# 6-DistributedSystems-Node
Node of the group project for 6-Distributes Systems for group Real Life Save Icons

Run the Naming Server in IDE or package with Maven and run the JAR.

The Nodes consist of two main parts: life-cylce and replication. 
Life-cycle handles discovery, bootstrap, shutdown and failure. Allowing nodes to join an exisiting network automatically on startup and leave the network automatically on shutdown.
Replication handles the file system. The node has 2 folders, one to store the local files and one to store the replicated files. Files added to the local files folder are replicated automatically troughout the network.
