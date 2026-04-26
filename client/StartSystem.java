package client;

import node.Node;
import rmi.NodeInterface;
import common.Request;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class StartSystem {

    public static void main(String[] args) {

        try {
            int totalNodes = 4;

            // 1. Start RMI Registry
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("RMI Registry started");

            // 2. Select leader randomly
            Random random = new Random();
            int leaderId = random.nextInt(totalNodes) + 1;
            System.out.println("Leader selected: Node " + leaderId);

            // 3. Create nodes
            Node[] nodes = new Node[totalNodes + 1];

            for (int i = 1; i <= totalNodes; i++) {
                nodes[i] = new Node(i, totalNodes, leaderId);
                registry.rebind("Node" + i, nodes[i]);
                System.out.println("Node " + i + " started");
            }

            // 4. Connect nodes together
            for (int i = 1; i <= totalNodes; i++) {
                for (int j = 1; j <= totalNodes; j++) {
                    NodeInterface stub =
                            (NodeInterface) registry.lookup("Node" + j);
                    nodes[i].addNode(j, stub);
                }
            }

            System.out.println("All nodes connected");

            // 5. TEST SCENARIO (important for grading)

            System.out.println("\n--- TEST SCENARIO START ---");

            // Upload from Node 1
            nodes[1].multicastRequest(Request.Type.UPLOAD, "file.txt");

            // Concurrent delete from Node 2
            nodes[2].multicastRequest(Request.Type.DELETE, "file.txt");

            // Search request
            nodes[3].multicastRequest(Request.Type.SEARCH, "file.txt");

            // Download request
            nodes[4].multicastRequest(Request.Type.DOWNLOAD, "file.txt");

            System.out.println("--- TEST SCENARIO END ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
