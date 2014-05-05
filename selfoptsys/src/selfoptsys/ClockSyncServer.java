package selfoptsys;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import selfoptsys.comm.MessageDest;

public interface ClockSyncServer 
    extends Remote {

    static int DEFAULT_NUM_CLOCK_SYNC_MESSAGES_REQUIRED = 30;
    static int PAUSE_TIME_BETWEEN_CLOCK_SYNC_MESSAGE_SENDS = 10;
    
    ClockSyncServer getRmiStub() throws RemoteException;
    
    int getNumberOfClockSyncMessagesRequired() throws RemoteException;
    long getPauseTimeBetweenClockSyncMessageSends() throws RemoteException;

    void start() throws RemoteException;
    void quit() throws RemoteException;
    
    MessageDest getMessageDest() throws RemoteException;
    
    Map<Integer, Long> getClockSkews() throws RemoteException;
    
}
