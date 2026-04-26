package common;

import java.io.Serializable;

public class Request implements Serializable {

    // Types of operations supported by the system
    public enum Type {
        UPLOAD,
        DELETE,
        DOWNLOAD,
        SEARCH
    }

    private Type type;          // Operation type
    private String fileName;    // Target file
    private int timestamp;      // Logical clock value
    private int senderId;       // Node that sent the request
    private int requestId;      // Tie-breaker for equal timestamps

    public Request(Type type, String fileName, int timestamp, int senderId, int requestId) {
        this.type = type;
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.requestId = requestId;
    }

    public Type getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getRequestId() {
        return requestId;
    }
}
