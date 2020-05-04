package Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastListener implements Runnable {

    private String groupAddress;
    private int port;
    private boolean isRunning = true;
    private Node currentNode;

    public MulticastListener(String groupAddress, int port, Node node) {
        this.groupAddress = groupAddress;
        this.port = port;
        currentNode = node;
    }

    @Override
    public void run() {
        System.out.println("Multicast Listener running");
        while (isRunning) {
            try {
                InetAddress group = InetAddress.getByName(groupAddress);
                MulticastSocket s = new MulticastSocket(port);
                s.joinGroup(group);

                while (isRunning) {
                    byte[] buf = new byte[1000];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);

                    s.receive(recv);

                    String input = new String(recv.getData());
                    MulticastHandler handler = new MulticastHandler(input, currentNode);
                    handler.start();
                }
                s.leaveGroup(group);
                System.out.println("Multicast thread stopped");
            } catch (IOException e) {
                e.printStackTrace();
                isRunning = false;
            }
        }
    }

    public void stop() {
        System.out.println("Multicast thread stopping");
        isRunning = false;
    }
}
