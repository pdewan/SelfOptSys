package selfoptsys.comm;



public interface LoggableMessageForwarderLazyPolicyPerMessageDataHolder {

	void init();
	
	CommandMessage getCommandMessage();
	
	boolean canFirstTransmissionThreadAtAllDelayProcessing();
	void prepareForTransmission();
	double getDelayForMsg(
			int curDestIndex
			);
	boolean canFirstTransmissionThreadContinueToDelayProcessing();
	
	void firstTransmitThreadQuitting();
	boolean getHasFirstTransmitThreadQuit();
	
	long getOriginalFowardStartTime();
    void setOriginalForwardStartTime(
    		long forwardStartTime
    		);
	
	int getNextIndexToTransmitTo();
	void setNextIndexToTransmitTo(
			int nextIndexToTransmitTo 
			);
}
