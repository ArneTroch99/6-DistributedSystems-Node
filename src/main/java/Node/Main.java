package Node;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("------Running node------");

        String address = "228.5.6.7";
        int port = 6789;
        Node node = new Node(address, port);

        UnicastListener unicastListener = new UnicastListener(7890, node);
        new Thread(unicastListener).start();
        MulticastListener listener = new MulticastListener(address, port, node);
        new Thread(listener).start();

        node.bootstrap();

        while (true) {
            String scan = new Scanner(System.in).nextLine();

            if (scan.equals("stop"))
                break;
            if (scan.equals("shut")) {
                System.out.println("shutting");
                node.shut();
            }
        }

        unicastListener.halt();
        listener.stop();

    }
}
