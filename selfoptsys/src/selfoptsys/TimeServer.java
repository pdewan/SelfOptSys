package selfoptsys;

import java.rmi.*;

public interface TimeServer 
	extends Remote {
    
    void reportReadyFirstTime( 
            int userIndex,
            ReplayClient replayClient,
            ReplayUserInfo replayUserInfo
            ) throws RemoteException;
    
    void reportReadySecondTime( 
            int userIndex
            ) throws RemoteException;
    
    void reportDone(
            int userIndex
            ) throws RemoteException;
    
    ClockSyncServer getClockSyncServer() throws RemoteException;
}