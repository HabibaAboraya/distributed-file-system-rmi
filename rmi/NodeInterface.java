package rmi;

import common.Request;
import common.ACK;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeInterface extends Remote {

    // Receive a request from another node
    void receiveRequest(Request request) throws RemoteException;

    // Receive an ACK from another node
    void receiveACK(ACK ack) throws RemoteException;
}
