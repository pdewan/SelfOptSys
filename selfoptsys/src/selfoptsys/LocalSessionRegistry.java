package selfoptsys;

import java.util.*;
import selfoptsys.comm.*;
import selfoptsys.ui.*;

public interface LocalSessionRegistry {

    void registerTimeServer(
            TimeServer timeServer
    		);
    
    void startRegistry();
    
    void reset();
    
    void setSessionRegistryUI( 
    		SessionRegistryUI sessionRegistryUI
    		);
    
    void startRecordingManager();
    
    void startPerformanceOptimizationServer();
    
    void setupCentralizedArchitecture(
    		int masterUserIndex
    		);

    void setupReplicatedArchitecture();
    
    void quit();
    
	void setSystemQuittingFlag(
			boolean flag
			);
	
    List<TimeServerReportMessage> getTimeServerReports();
    
    void uiUpdateDelayBetweenUsers(
    		int sourceUser,
    		int destUser,
    		int delay
    		);
    
}
