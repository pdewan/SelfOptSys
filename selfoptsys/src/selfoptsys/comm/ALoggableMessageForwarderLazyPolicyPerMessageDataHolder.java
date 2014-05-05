package selfoptsys.comm;

import commonutils.basic.MathUtils;

import selfoptsys.LocalLogger;
import selfoptsys.perf.PerformanceParameterType;

public class ALoggableMessageForwarderLazyPolicyPerMessageDataHolder implements
		LoggableMessageForwarderLazyPolicyPerMessageDataHolder {

	protected LocalLogger m_logger;
	protected CommandMessage m_cmdMsg;
	
	protected double m_delayPerDest = 0;
	protected double m_delayPerDestToFirstDest = 0;
	protected boolean m_msgFromLocal;
	
    protected long m_originalForwardStartTime = 0;
    protected int m_nextIndexToTransmitTo = 0;
    protected boolean m_firstLazyTransmitStepDone = false;
    
    protected boolean m_nwCardCaughtUp;

    public ALoggableMessageForwarderLazyPolicyPerMessageDataHolder(
    		LocalLogger logger,
			CommandMessage cmdMsg
			) {
    	m_logger = logger;
    	m_cmdMsg = cmdMsg;
	}
    
    public CommandMessage getCommandMessage() {
    	return m_cmdMsg;
    }
    
    public void init() {

    	PerformanceParameterType costType = null;
        PerformanceParameterType costTypeToFirstDest = null;
        if ( m_cmdMsg.getMessageType() == MessageType.INPUT ) {
            if ( m_cmdMsg.getSenderUserIndex() == m_logger.getUserIndex() ) {
                costType = PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                costTypeToFirstDest = PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
            }
            else {
                costType = PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                costTypeToFirstDest = PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
            }
        }
        else {
            if ( m_cmdMsg.getSourceUserIndex() == m_logger.getUserIndex() ) {
                costType = PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME;
                costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
            }
            else {
                costType = PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME;
                costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
            }
        }
        m_delayPerDest = m_logger.getObservedTransCost(
                costType,
                m_cmdMsg.getSysConfigVersion()
                );
        m_delayPerDestToFirstDest = m_logger.getObservedTransCost(
                costTypeToFirstDest,
                m_cmdMsg.getSysConfigVersion()
                );
        
        m_msgFromLocal = m_cmdMsg.getSenderUserIndex() == m_cmdMsg.getSourceUserIndex();

    }
    
    public boolean canFirstTransmissionThreadAtAllDelayProcessing() {
    	
        double delaySoFar = m_cmdMsg.getDelaySoFar();
        if ( m_firstLazyTransmitStepDone == false ) {
            if ( ( m_msgFromLocal && delaySoFar >= m_logger.getLazyLocalDelay() ) ||
                    ( !m_msgFromLocal && delaySoFar >= m_logger.getLazyRemoteDelay() ) ) {
                return false;
            }
        }
        
        return true;
    	
    }
    
    public void prepareForTransmission() {
    	
    	if ( m_nextIndexToTransmitTo > 0 ) {
	        long forwardStartTime = m_originalForwardStartTime;
	        double totalNwCardTransTimeCommittedSoFar =
	            ( m_nextIndexToTransmitTo * m_delayPerDest ) + ( m_delayPerDestToFirstDest - m_delayPerDest );
	        double timeSinceStart = (double) ( ( System.nanoTime() - forwardStartTime ) / (double) 1000000 );
	        if ( timeSinceStart > totalNwCardTransTimeCommittedSoFar ) {
	            m_nwCardCaughtUp = true;
	        }
    	}
    	
    }
    
    public double getDelayForMsg(
    		int curDestIndex
    		) {
    	
    	double delayForMsg = 0;
    	
        if ( m_firstLazyTransmitStepDone == true ) {
            if ( m_nextIndexToTransmitTo > 0 && m_nwCardCaughtUp ) {
                double timeSinceStart = (double) ( ( System.nanoTime() - m_originalForwardStartTime ) / (double) 1000000 );
                delayForMsg += timeSinceStart +
                    ( ( curDestIndex - m_nextIndexToTransmitTo ) * m_delayPerDest ) + m_delayPerDest;
            }
            else if ( m_nextIndexToTransmitTo > 0 && m_nwCardCaughtUp == false ) {
                delayForMsg += ( curDestIndex * m_delayPerDest ) + ( m_delayPerDestToFirstDest - m_delayPerDest );
            }
        }
        else {
            delayForMsg += ( curDestIndex * m_delayPerDest ) + ( m_delayPerDestToFirstDest - m_delayPerDest ); 
        }

        return delayForMsg;
    	
    }
    
    public boolean canFirstTransmissionThreadContinueToDelayProcessing() {
    	
        double delaySoFar = 
            MathUtils.round( ( ( System.nanoTime() - m_originalForwardStartTime ) / (double) 1000000 ) - m_cmdMsg.getDelaySoFar(), 3 );
        if ( ( m_msgFromLocal && delaySoFar >= m_logger.getLazyLocalDelay() ) ||
                    ( !m_msgFromLocal && delaySoFar >= m_logger.getLazyRemoteDelay() ) ) {
            return false;
        }

        return true;
    }
    
    public void firstTransmitThreadQuitting() {
        m_firstLazyTransmitStepDone = true;
    }
    public boolean getHasFirstTransmitThreadQuit() {
    	return m_firstLazyTransmitStepDone;
    }
    
    public long getOriginalFowardStartTime() {
    	return m_originalForwardStartTime;
    }
    public void setOriginalForwardStartTime(
    		long forwardStartTime
    		) {
    	m_originalForwardStartTime = forwardStartTime;
    }
    
	public int getNextIndexToTransmitTo() {
		return m_nextIndexToTransmitTo;
	}
	public void setNextIndexToTransmitTo(
			int nextIndexToTransmitTo
			) {
		m_nextIndexToTransmitTo = nextIndexToTransmitTo;
	}

	
}
