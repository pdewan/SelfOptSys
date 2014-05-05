package selfoptsys;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import selfoptsys.comm.*;

public interface ReplayClient 
    extends Remote {

    void registerWithReplayServer(
            TimeServer replayServer
            ) throws RemoteException;
    
    void reportReadySecondTimeToReplayServer() throws RemoteException;
    void reportDoneToReplayServer() throws RemoteException;
    void recordReplayServerReport(
            MessageType messageType,
            int userIndex,
            int sourceUserIndex,
            int seqId,
            long time
            ) throws RemoteException;
    
    void beginExperiment() throws RemoteException;
    void joinByCommandFromReplayServer() throws RemoteException;
    List<TimeServerReportMessage> getReplayServerReports() throws RemoteException;
    void syncClockWithReplayServer() throws RemoteException;
    void prepareToQuit() throws RemoteException;
    void quit() throws RemoteException;
    
}
