package common;

import java.io.Serializable;

public class ACK implements Serializable {

    private int requestId;
    private int senderId;

    public ACK(int requestId, int senderId) {
        this.requestId = requestId;
        this.senderId = senderId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getSenderId() {
        return senderId;
    }
}
