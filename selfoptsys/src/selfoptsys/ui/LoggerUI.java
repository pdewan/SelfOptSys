package selfoptsys.ui;

public interface LoggerUI {

	int getUserIndex();
	
	void setMasterUserIndex(
		int masterUserIndex
		);
	int getMasterUserIndex();
	
	void setIsMaster(
			boolean isMaster
			);
	boolean getIsMaster();
	
	void joinAsMaster();
    
	void joinAsSlaveOf(
			int masterUserIndex
			);
    
	void quit();
	
    void setIsJoined(
    		boolean joined
    		);
    boolean getIsJoined();

    void setIsInputtingCommands(
    		boolean inputsCommands
    		);
    boolean getIsInputtingCommands();
    
//    void setUserControlledLoggerUI(
//            UserControlledLoggerUI userControlledLoggerUI
//            );
//    UserControlledLoggerUI getUserControlledLoggerUI();
//	
//    void setUserControlledSchedulerUI(
//            UserControlledMetaSchedulerUI userControlledSchedulerUI
//            );
//    UserControlledMetaSchedulerUI getUserControlledSchedulerUI();
    
}
