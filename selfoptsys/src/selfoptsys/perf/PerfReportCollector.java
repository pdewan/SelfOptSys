package selfoptsys.perf;

import java.net.*;
import java.rmi.*;
import java.util.*;
import commonutils.config.*;
import selfoptsys.comm.*;
import selfoptsys.network.*;


public interface PerfReportCollector 
    extends Remote {

    void setSimulatedLatencies(
            double[][] simlatedLatencies
            ) throws RemoteException;
    
    void init() throws RemoteException;
    
    void begin() throws RemoteException;
    
    MessageDest getMessageDest() throws RemoteException;
    
    void registerUser(
            int userIndex,
            NetworkLatencyCollectorAgent userAgent,
            InetAddress userHost,
            String processorName,
            String processorIdentifier,
            long processorSpeed,
            int latencyIndex,
            boolean collectPerfStatsForUser,
            int coreUsedByProcessingThread,
            int coreUsedByTransmissionThread,
            int quantumSize
            ) throws RemoteException;
    
    void unregisterUser(
            int userIndex
            ) throws RemoteException;
    
    void setCurrentSystemConfiguration(
            int systemConfigurationVersion,
            List<Integer> userIndices,
            List<Integer> masterUserIndices,
            Vector<Integer> inputtingUserIndices,
            Vector<Integer> mastersRunningUI,
            HashMap<Integer, Overlay> networkOverlays,
            HashMap<Integer, SchedulingPolicy> schedulingPolicies
            ) throws RemoteException;
    
    void quit() throws RemoteException;
    
    void reset() throws RemoteException;
    
}
