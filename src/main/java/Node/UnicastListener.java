package Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UnicastListener implements Runnable {

    private int port;
    private boolean isRunning = true;
    private Node currentNode;

    UnicastListener(int port, Node currentNode) {
        this.port = port;
        this.currentNode = currentNode;
    }

    @Override
    public void run() {
        System.out.println("Unicast Listener running");
        try {
            DatagramSocket s = new DatagramSocket(port);

            while (isRunning) {
                byte[] buf = new byte[32768];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                s.receive(recv);

                String input = new String(recv.getData());
                System.out.println("Unicast: " + input);
                currentNode.processUni(input);
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("UDP thread stoppped");
    }

    public void halt() {
        System.out.println("UDP thread stoppping");
        isRunning = false;
    }
}
