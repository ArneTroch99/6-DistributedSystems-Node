package Node;

public class MulticastHandler extends Thread {
    private String input;
    private Node currentNode;

    public MulticastHandler(String input, Node node) {
        this.input = input;
        this.currentNode = node;
    }

    public void run() {
        String[] data = input.split("%");

        currentNode.process(data[0], data[1]);
    }
}
