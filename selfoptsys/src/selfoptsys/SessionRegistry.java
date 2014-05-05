package selfoptsys;

import java.rmi.*;
import java.util.*;

import selfoptsys.network.*;
import selfoptsys.perf.*;
import commonutils.config.*;

public interface SessionRegistry 
	extends Remote {
	
	final int COMM_ARCH_REQUEST_STATUS_SUCCESS = 0;
	final int COMM_ARCH_REQUEST_STATUS_FAILURE = 1;
	
	void registerPerformanceOptimizationServer(
			PerformanceOptimizationServer performanceOptimizationServer
			) throws RemoteException;
	void startPerformanceOptimizationServer() throws RemoteException;
	
	void join(
			int userIndex,
			Logger remoteLogger,
			boolean firstTimeJoin,
			boolean inputsCommands,
			boolean runningUIAsMaster,
			boolean joiningByCommandFromTimeServer,
			boolean fakeUser
			) throws RemoteException;
	
	void joinAsMaster(
			int userIndex,
			Logger remoteLogger,
			boolean firstTimeJoin,
			boolean inputsCommands,
			boolean runningUIAsMaster,
            boolean fakeUser
			) throws RemoteException;
	
	void joinAsSlave(
			int userIndex,
			int masterUserIndex,
			Logger remoteLogger,
			boolean firstTimeJoin,
			boolean inputsCommands,
            boolean fakeUser
			) throws RemoteException;
	
	void leaving(
			int userIndx
			) throws RemoteException;
	
	void setSchedulingPolicy( 
			SchedulingPolicy schedulingPolicy ) throws RemoteException;
	SchedulingPolicy getSchedulingPolicy() throws RemoteException;
	
	ProcessingArchitectureType getProcessingArchitecture() throws RemoteException;
	
	Overlay getOverlay(
			int userIndex
			) throws RemoteException;
	
	void updateUserInputsCommands(
			int userIndex,
			boolean inputsCommands
			) throws RemoteException;
	
    boolean setupNewSysConfig(
            int prevSysConfigVersion,
            List<Integer> masterUserIndices,
            HashMap<Integer, Overlay> overlays,
            HashMap<Integer, SchedulingPolicy> schedulingPolicies,
            HashMap<Integer, HashMap<PerformanceParameterType, Double>> observedTransCosts
            ) throws RemoteException;
    
    void signalStartOfExperiment() throws RemoteException;
    
}
