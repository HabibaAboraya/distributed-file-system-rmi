package node;

import common.Request;
import common.ACK;
import rmi.NodeInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.util.*;

public class Node extends UnicastRemoteObject implements NodeInterface {

    // Node identity
    private int nodeId;
    private int leaderId;
    private int totalNodes;

    // Lamport logical clock
    private int logicalClock;

    // Directory for this node's files
    private File nodeDirectory;

    // Request queue ordered by timestamp then requestId
    private PriorityQueue<Request> requestQueue;

    // requestId -> set of nodeIds that acknowledged
    private Map<Integer, Set<Integer>> ackMap;

    // References to all nodes (for multicast)
    private Map<Integer, NodeInterface> nodes;

    // Constructor
    public Node(int nodeId, int totalNodes, int leaderId) throws RemoteException {
        super();
        this.nodeId = nodeId;
        this.totalNodes = totalNodes;
        this.leaderId = leaderId;
        this.logicalClock = 0;

        // Create node directory
        this.nodeDirectory = new File("files/node" + nodeId);
        if (!nodeDirectory.exists()) {
            nodeDirectory.mkdirs();
        }

        // Initialize priority queue with total ordering
        this.requestQueue = new PriorityQueue<>(
                (r1, r2) -> {
                    if (r1.getTimestamp() != r2.getTimestamp()) {
                        return Integer.compare(r1.getTimestamp(), r2.getTimestamp());
                    }
                    return Integer.compare(r1.getRequestId(), r2.getRequestId());
                }
        );

        this.ackMap = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    // Register other nodes (called during startup)
    public void addNode(int id, NodeInterface node) {
        nodes.put(id, node);
    }

    //  REQUEST SENDING (MULTICAST)

    public synchronized void multicastRequest(Request.Type type, String fileName)
            throws RemoteException {

        // Increment logical clock before sending
        logicalClock++;

        // Generate unique request ID
        int requestId = logicalClock * 100 + nodeId;

        // Create request
        Request request = new Request(
                type,
                fileName,
                logicalClock,
                nodeId,
                requestId
        );

        // Multicast request to all nodes (including self)
        for (NodeInterface targetNode : nodes.values()) {
            targetNode.receiveRequest(request);
        }
    }

    //  RMI METHODS


    @Override
    public synchronized void receiveRequest(Request request) throws RemoteException {

        // Update logical clock on receive
        logicalClock = Math.max(logicalClock, request.getTimestamp()) + 1;

        // Add request to the priority queue
        requestQueue.add(request);

        // Initialize ACK tracking
        ackMap.putIfAbsent(request.getRequestId(), new HashSet<>());

        // Multicast ACK to all nodes
        for (NodeInterface targetNode : nodes.values()) {
            ACK ack = new ACK(request.getRequestId(), nodeId);
            targetNode.receiveACK(ack);
        }
    }


    @Override
    public synchronized void receiveACK(ACK ack) throws RemoteException {

        int requestId = ack.getRequestId();
        int senderId = ack.getSenderId();

        // Record ACK
        ackMap.putIfAbsent(requestId, new HashSet<>());
        ackMap.get(requestId).add(senderId);

        // Check if requests can now be executed
        tryExecuteRequests();
    }

    //  EXECUTION LOGIC

    private void tryExecuteRequests() {

        while (!requestQueue.isEmpty()) {

            Request request = requestQueue.peek();
            int requestId = request.getRequestId();

            // Check if all nodes acknowledged this request
            Set<Integer> ackedNodes = ackMap.get(requestId);
            if (ackedNodes == null || ackedNodes.size() < totalNodes) {
                return;
            }

            // Safe to execute
            requestQueue.poll();
            ackMap.remove(requestId);

            executeRequest(request);
        }
    }

    private void executeRequest(Request request) {

        File targetFile = new File(nodeDirectory, request.getFileName());

        switch (request.getType()) {

            case UPLOAD:
                try {
                    if (!targetFile.exists()) {
                        targetFile.createNewFile();
                        System.out.println("Node " + nodeId +
                                " UPLOADED " + request.getFileName());
                    } else {
                        System.out.println("Node " + nodeId +
                                " already has " + request.getFileName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case DELETE:
                if (targetFile.exists()) {
                    targetFile.delete();
                    System.out.println("Node " + nodeId +
                            " DELETED " + request.getFileName());
                } else {
                    System.out.println("Node " + nodeId +
                            " file not found for DELETE: " + request.getFileName());
                }
                break;

            case SEARCH:
                if (nodeId == leaderId) {
                    if (targetFile.exists()) {
                        System.out.println("Leader FOUND " + request.getFileName());
                    } else {
                        System.out.println("Leader DID NOT FIND " + request.getFileName());
                    }
                }
                break;

            case DOWNLOAD:
                if (nodeId == leaderId) {
                    if (targetFile.exists()) {
                        System.out.println("Leader DOWNLOADING " + request.getFileName());
                    } else {
                        System.out.println("Leader file not found for DOWNLOAD: "
                                + request.getFileName());
                    }
                }
                break;
        }
    }
}
