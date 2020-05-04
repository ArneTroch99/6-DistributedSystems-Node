package Node;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node {

    private String multicastGroup;
    private int socket;
    private String localIP;
    private String localName;
    private String ipName;
    private int nameport = 7895;
    private int uniport = 7890;

    private int currentID;
    private int nextID = 0;
    private int previousID = 0;

    Node(String multicastGroup, int socket) {
        this.multicastGroup = multicastGroup;
        this.socket = socket;
        this.setHost();
        this.currentID = hash(localName);
    }

    private void setHost() {
        try {
            InetAddress host = InetAddress.getLocalHost();
            localIP = host.getHostAddress();
            localName = host.getHostName();

            System.out.println("Node information:");
            System.out.println("Local IP: " + localIP + "\nName: " + localName + "\nHash: " + hash(localName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getHost() {
        return localIP + "%" + localName;
    }

    void bootstrap() {
        sendMulti(getHost());
    }

    void processUni(String msg) {
        char firstChar = msg.charAt(0);

        switch (firstChar) {
            case '$':
                msg = msg.substring(1).trim();
                String[] data = msg.split("%");
                int numberOfNodes = Integer.parseInt(data[0].trim());
                ipName = data[1];
                System.out.println("Namingserver ip: " + ipName);
                if (numberOfNodes < 1) {
                    nextID = currentID;
                    previousID = currentID;
                }
                break;
            case '#':
                msg = msg.substring(1).trim();
                calcIDs(msg);
                break;
            case '~':
                msg = msg.substring(1).trim();
                String[] data2 = msg.split("%");
                System.out.println("1 "+ data2[0] + " 2 " + data2[1]);
                sendUni("&" + nextID , data2[0].trim(), uniport);
                sendUni("@" + previousID, data2[1].trim(), uniport);
                System.out.println("sent ips");
                break;
            case '&':
                msg = msg.substring(1).trim();
                nextID = Integer.parseInt(msg);
                break;
            case '@':
                msg = msg.substring(1).trim();
                previousID = Integer.parseInt(msg);
                break;
        }
        System.out.println("after uni: nextID " + nextID + " previousID " + previousID);
    }

    public void process(String ip, String name) {
        if (!ip.equals(localIP)) {
            if (calcIDs(name.trim()))
                sendUni("#" + this.localName, ip, uniport);

            System.out.println("after multi: nextID " + nextID + " previousID " + previousID);
        }
    }

    private boolean calcIDs(String name) {
        int nodeHash = hash(name);
        boolean state = false;

        if (previousID == nextID && (nextID == currentID || nextID == 0)) {
            nextID = nodeHash;
            previousID = nodeHash;
            state = true;
        } else if ((currentID < nodeHash && nodeHash < nextID)) {
            nextID = nodeHash;
            state = true;
        } else if ((currentID > nodeHash && nodeHash > previousID)) {
            previousID = nodeHash;
            state = true;
        }
        return state;
    }

    public void shut() {
        String msg = "~" + previousID + "%" + nextID + "%" + localName;
        sendUni(msg, ipName, nameport);
        System.out.println("sending shut message");
    }

    private boolean sendUni(String msg, String ip, int sendPort) {
        try {
            DatagramSocket ds = new DatagramSocket();

            InetAddress ipDest = InetAddress.getByName(ip.trim());
            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), ipDest, sendPort);
            ds.send(dp);
            ds.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean sendMulti(String msg) {
        try {
            // join multicast group
            InetAddress group = InetAddress.getByName(multicastGroup);
            MulticastSocket s = new MulticastSocket(socket);
            s.joinGroup(group);

            // build packet and multicast send it
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, socket);
            s.send(packet);

            // leave group
            s.leaveGroup(group);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);

            int temp = 32768;
            return no.mod(BigInteger.valueOf(temp)).intValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}