package selfoptsys.comm;

import java.util.*;
import commonutils.config.*;

public interface CommandMessage 
	extends Message {
		
    void setSeqId(
            int seqId
            );
    int getSeqId();
    
    void setSourceUserIndex(
            int sourceUserIndex
            );
    int getSourceUserIndex();

    void setSenderUserIndex(
            int senderUserIndex
            );
    int getSenderUserIndex();
    
    void setSysConfigVersion(
            int sysConfigVersion
            );
    int getSysConfigVersion();

	void isMsgForLatecomerOrNewMaster(
			boolean isMsgForLatecomerOrNewMaster
			);
	boolean isMsgForLatecomerOrNewMaster();
	
	Vector<Integer> getDestUserIndices();
	
	Object getData();
	void setData( Object newData );
	
	void setSuggestedSchedulingPolicy(
            SchedulingPolicy newSuggestedSchedulingPolicy
            );
	SchedulingPolicy getSuggestedSchedulingPolicyForMsg();
	
    void setReportTransCostForMessage(
            boolean reportTransTimeForMessage
            );
    boolean getReportTransCostForMessage();

    void setReportProcCostForMessage(
            boolean reportProcTimeForMessage
            );
    boolean getReportProcCostForMessage();

    void setNetworkLatencyDelay(
            int networkLatencyDelay
            );
    int getNetworkLatencyDelay();
    
    void setDelaySoFar(
            double delaySoFar
            );
    double getDelaySoFar();

    void setReportReceiveTime(
            boolean reportReceiveTime
            );
    boolean getReportReceiveTime();

}
