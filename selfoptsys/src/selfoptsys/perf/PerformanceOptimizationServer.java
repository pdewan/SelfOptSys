package selfoptsys.perf;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import commonutils.config.*;

import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.network.*;

public interface PerformanceOptimizationServer 
	extends Remote {

    MessageDest getMessageDest() throws RemoteException;
    void init() throws RemoteException;
    
    ClockSyncServer getClockSyncServer() throws RemoteException;
    
    void begin() throws RemoteException;
    void quit() throws RemoteException;
    
    void registerUser(
            PerformanceOptimizationUserInfo performanceOptimizationUserInfo
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
	
    void setSimulatedLatencies(
            double[][] simulatedLatencies
            ) throws RemoteException;
}
