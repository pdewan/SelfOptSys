package selfoptsys;

import java.util.*;

import selfoptsys.comm.*;

public interface Loggable {

	final static int INIT_SEQ_NUMBER = 0;
	
	final static int DEFAULT_CLUSTER = 0;
	
	final static int UNSET_DELAY_VALUE = -1;
	final static int UNSET_CLUSTER_VALUE = -1;
	
	final static int INFINITY = Integer.MAX_VALUE;
	
	abstract void sessionStarted();
	
    abstract void replayToView( Object msg );
    abstract void replayToModel( Object msg );
	
    void startLogger();
    
    void deliverInputMsg(
    		CommandMessage msg
            );
    void deliverOutputMsg(
    		CommandMessage msg
            );
    
    void sendInputMsg(
            Object msg
            );
    void sendInputMsg(
            Object msg,
            Vector<Integer> destUserIndices
            );
    void sendInputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost
            );
    void sendInputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost,
            Vector<Integer> destUserIndices
            );
    void sendOutputMsg(
            Object msg
            );
    void sendOutputMsg(
            Object msg,
            Vector<Integer> destUserIndices
            );
    void sendOutputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost
            );
    void sendOutputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost,
            Vector<Integer> destUserIndices
            );
    
    int getUserIndex();
    int getMasterUserIndex();
    void setMasterUserIndex(
    		int masterUserIndex
    		);
    
    void beginExperiment();
    
    void loggerAboutToQuit();
    
    void quit();
    
    public void replayMessageFromReplayLogIfMyTurn();
    
    void resetLoggableForArchChange(
    		int masterUserIndex
    		);
    
    LocalLogger getLogger();

    void setUserInputsCommands(
    		boolean inputsCommands
    		);
    
    void setPrintCosts(
    		boolean printCosts
    		);
    void setCostsOutputFile(
    		String costsOutputFileName
    		);

    void reportReadySecondTimeToTimeServer();
    
    void sendPerfReportCollectorReportMessage(
            Message perfReportCollectorReportMessage
            );
    
    void setAutoReplayCommandsFromReplayLog(
    		boolean autoReplayCommandsFromReplayLog
    		);
    
    void setWaitForUserToReleaseTasks(
            boolean waitForUsersToReleaseTasks
            );
    
    void setWaitForUserToScheduleTasks(
            boolean waitForUsersToScheduleTasks
            );
    
    boolean isMaster();
    
    ALoggableUserInfo getLoggableUserInfo();
    
    boolean shouldProcCostForMsgBeReported(
            Object msg
            );
    boolean shouldTransCostForMsgBeReported(
            Object msg
            );
    
    void recordTimeServerMessage(
    		MessageType messageType,
            int userIndex,
            int sourceUserIndex,
            int seqId,
            long time
    		);
    
}
