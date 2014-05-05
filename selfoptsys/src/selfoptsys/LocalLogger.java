package selfoptsys;

import commonutils.config.*;
import selfoptsys.comm.*;
import selfoptsys.perf.*;

public interface LocalLogger {

	int getUserIndex();
	
	int getMasterUserIndex();
	
    void sendInputMsg( CommandMessage msg );
    void sendOutputMsg( CommandMessage msg );
    
    void joinAsMaster();
    
    void joinAsSlave(
    		int masterUserIndex
    		);
    
    void prepareToQuit();
    void quit();
    
    boolean hasAFakeLoggable();
    
    boolean getUserInputsCommands();
    void setUserInputsCommands(
    		boolean inputsCommands
    		);

    void reportReadySecondTimeToReplayServer();
    
    void sendPerfReportCollectorReportMessage(
            Message perfReportCollectorReportMessage
            );
    
    void setAutoReplayCommandsFromReplayLog(
    		boolean autoReplayCommandsFromReplayLog
    		);
    
    boolean getOutputCorrespondsToInput();
    
    ALoggableUserInfo getLoggerUserInfo();
    
    SchedulingPolicy getSchedulingPolicyForSysConfigVersion(
            int sysConfigVersion
            );
    
    int getLazyLocalDelay();
    int getLazyRemoteDelay();
    
    double getObservedTransCost(
            PerformanceParameterType costType,
            int sysConfigVersion
            );
    
    void reportDoneToReplayServer();
    
    void recordReplayServerReport(
    		MessageType messageType,
            int userIndex,
            int sourceUserIndex,
            int seqId,
            long time
    		);
    
}
