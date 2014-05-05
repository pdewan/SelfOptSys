package selfoptsys;

import java.rmi.*;
import java.util.*;

import selfoptsys.comm.*;
import selfoptsys.overlay.*;
import selfoptsys.perf.*;

import commonutils.config.*;

public interface Logger 
	extends Remote {
	
	void setMappings(
			int masterUserIndex,
			Vector<AnOverlayMapping> inputDestMappings,
			Vector<AnOverlayMapping> inputSourceMappings,
			Vector<AnOverlayMapping> outputDestMappings,
			Vector<AnOverlayMapping> outputSourceMappings,
			int sysConfigVersion
			) throws RemoteException;
	
	void unsetMappings(
			Vector<AnOverlayMapping> inputDestMappings,
			Vector<AnOverlayMapping> inputSourceMappings,
			Vector<AnOverlayMapping> outputDestMappings,
			Vector<AnOverlayMapping> outputSourceMappings,
			int sysConfigVersion
			) throws RemoteException;
	
	void leaving(
			int userIndex
			) throws RemoteException;
	
    public MessageDest getInputMsgDest(
    		int sysConfigVersion ) throws RemoteException;
    public MessageDest getOutputMsgDest(
    		int sysConfigVersion ) throws RemoteException;
    
    public void registerTimeServer( 
    		TimeServer timeServer ) throws RemoteException;
    
    void registerPerformanceOptimizationServer(
            PerformanceOptimizationServer performanceOptimizationServer
            ) throws RemoteException;
    
    public void beginExperiment() throws RemoteException;
    
    public void prepareToQuit() throws RemoteException;
    public void quit() throws RemoteException;
    
    void mapAsInputDest(
    		int rootUserIndex,
    		int userIndex,
    		int latencyIndex,
    		MessageDest msgDest,
			int sysConfigVersion ) throws RemoteException;
    
    void mapAsOutputDest(
    		int rootUserIndex,
    		int userIndex,
            int latencyIndex,
    		MessageDest msgDest,
			int sysConfigVersion ) throws RemoteException;
    
    void unmapAsInputDest( 
    		int rootUserIndex,
    		int userIndex,
			int sysConfigVersion ) throws RemoteException;
    
    void unmapAsOutputDest(
    		int rootUserIndex,
    		int userIndex,
			int sysConfigVersion ) throws RemoteException;
    
    void mapAsInputSource( 
    		int rootUserIndex,
    		int userIndex,
            int latencyIndex,
    		Logger remoteLogger,
			int sysConfigVersion ) throws RemoteException;
    
    void mapAsOutputSource(
    		int rootUserIndex,
    		int userIndex,
            int latencyIndex,
    		Logger remoteLogger,
			int sysConfigVersion ) throws RemoteException;
    
    void unmapAsInputSource( 
    		int rootUserIndex,
    		Logger remoteLogger,
			int sysConfigVersion ) throws RemoteException;
    
    void unmapAsOutputSource(
    		int rootUserIndex,
    		Logger remoteLogger,
			int sysConfigVersion ) throws RemoteException;
    
    int getMasterUserIndex() throws RemoteException;
    
    void requestingAllInputCommandsTransmittedSoFar(
    		int userIndex,
    		MessageDest msgDest,
    		int sysConfigVersionTag
    		) throws RemoteException;
    
    void requestingAllOutputCommandsTransmittedSoFar(
    		int userIndex,
    		MessageDest msgDest,
            int sysConfigVersionTag
    		) throws RemoteException;
    
    void setSpecialDestMappings(
    		Vector<AnOverlayMapping> specialInputDestMappings,
    		Vector<AnOverlayMapping> specialOutputDestMappings,
			int sysConfigVersion
    		) throws RemoteException;
    
    void unsetSpecialDestMappings(
    		Vector<AnOverlayMapping> specialInputDestMappings,
    		Vector<AnOverlayMapping> specialOutputDestMappings,
			int sysConfigVersion
    		) throws RemoteException;
    
    void setSchedulingPolicy( 
			SchedulingPolicy schedulingPolicy,
            int sysConfigVersion
			) throws RemoteException;
    
    void setCommunicationArchitectureType(
    		CommunicationArchitectureType commArchType
    		) throws RemoteException;

    void setReplayCommandsToLatecomers(
    		boolean replayCommandsToLatecomers
    		) throws RemoteException;
    
    void setOutputCorrespondsToInput(
    		boolean outputCorrespondsToInput
    		) throws RemoteException;
    
    void setAutoReportEachCommandToTimeServer(
            boolean reportEachCommandToTimeServer
            ) throws RemoteException;
    
    void setAutoReportProcCosts(
    		boolean reportProcCosts
    		) throws RemoteException;

    void setAutoReportTransCosts(
            boolean reportTransCosts
            ) throws RemoteException;

    void setSimulatedLatencies(
            double[][] simulatedLatencies
            ) throws RemoteException;
    void setSimulatedLatencyIndex(
            int simulatedLatencyIndex
            ) throws RemoteException;
    
    void setUserIndexToLatencyIndexMap(
            Map<Integer, Integer> userIndexToLatencyIndexMap
            ) throws RemoteException;
    
    void joinByCommandFromTimeServer() throws RemoteException;
    
    void prepareForNewSysConfigVersion(
            int sysConfigVersion,
            int myMasterUserIndex,
            Logger userLoggerFromWhoToQueryCommands
            ) throws RemoteException;
    void switchToSysConfigVersion(
            int sysConfigVersion
            ) throws RemoteException;
    
    void setObservedTransCost(
            HashMap<PerformanceParameterType, Double> observedTransCosts,
            int sysConfigVersion
            ) throws RemoteException;
    
    void takeEnterInputToken() throws RemoteException;
    void releaseEnterInputToken() throws RemoteException;
    
}
