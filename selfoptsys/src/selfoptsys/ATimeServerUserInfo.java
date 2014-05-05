package selfoptsys;

import java.util.*;

public class ATimeServerUserInfo {

	protected int m_userIndex;
	protected ReplayClient m_replayClient;
	protected ReplayUserInfo m_replayUserInfo;
	
	protected Map<Integer, Long> m_inputEnteredTimes;
	protected Map<Integer, Map<Integer, Long>> m_outputProcessedTimes;
	
	public ATimeServerUserInfo(
			int userIndex,
			ReplayClient replayClient,
			ReplayUserInfo replayUserInfo
			) {
		m_userIndex = userIndex;
		m_replayClient = replayClient;
		m_replayUserInfo = replayUserInfo;
		
		m_inputEnteredTimes = new Hashtable<Integer, Long>();
		m_outputProcessedTimes = new Hashtable<Integer, Map<Integer, Long>>();
	}
	
	public int getUserIndex() {
		return m_userIndex;
	}
	
	public ReplayClient getReplayClient() {
		return m_replayClient;
	}
	
	public boolean getMeasurePerformance() {
		return m_replayUserInfo.getMeasurePerformance();
	}
	
	public void addInputEnteredTime(
			int seqId,
			long procTime
			) {
		m_inputEnteredTimes.put( seqId, procTime );
	}
	public long getInputEnteredTime(
			int seqId
			) {
		return m_inputEnteredTimes.get( seqId );
	}
	
	public void addOutputProcessedTime(
	        int cmdSourceUserIndex,
			int seqId,
			long procTime
			) {
	    Map<Integer, Long> outputProcessedReportsForCmdsByUser = m_outputProcessedTimes.get( cmdSourceUserIndex );
	    if ( outputProcessedReportsForCmdsByUser == null ) {
	        outputProcessedReportsForCmdsByUser = new Hashtable<Integer, Long>();
	        m_outputProcessedTimes.put(
	                cmdSourceUserIndex,
	                outputProcessedReportsForCmdsByUser
	                );
	    }
	    outputProcessedReportsForCmdsByUser.put(
	            seqId,
	            procTime
	            );
	}
	
	public long getOutputProcessedTime(
	        int cmdSourceUserIndex,
			int seqId
			) {
	    Map<Integer, Long> outputProcessedReportsForCmdsByUser = m_outputProcessedTimes.get( cmdSourceUserIndex );
	    if ( outputProcessedReportsForCmdsByUser == null ) {
	        return -1;
	    }
	    
	    Long procTime = outputProcessedReportsForCmdsByUser.get( seqId );
	    if ( procTime == null ) {
	        return -1;
	    }
	    
	    return procTime.longValue();
	}
	
}
