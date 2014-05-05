package selfoptsys.comm;

import java.io.*;
import java.util.*;
import selfoptsys.*;
import selfoptsys.perf.*;

import commonutils.config.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public class ASerializedObjectTCPLoggableMessageForwarder
    extends ASerializedObjectTCPMessageForwarder 
    implements LoggableMessageForwarder {

    protected LoggableMessageForwarderDataHolder m_dataHolder;

    protected boolean m_temporaryForwarderForNewMasterOrLatecomer;
    protected boolean m_autoReportTransCosts;

    protected LocalLogger m_logger;

    public ASerializedObjectTCPLoggableMessageForwarder(
            int userIndex,
            boolean autoReportTransCosts,
            LocalLogger logger
            ) {
        this(
                userIndex,
                false,
                autoReportTransCosts,
                logger
                );
    }
    public ASerializedObjectTCPLoggableMessageForwarder(
            int userIndex,
            boolean temporaryForwarderForNewMasterOrLatecomer,
            boolean autoReportTransCosts,
            LocalLogger logger
            ) {
        super( 
                userIndex
                );
        
        m_dataHolder = new ALoggableMessageForwarderDataHolder(
                userIndex
                );
        
        m_temporaryForwarderForNewMasterOrLatecomer = temporaryForwarderForNewMasterOrLatecomer;
        m_autoReportTransCosts = autoReportTransCosts;
        
        m_logger = logger;
    }
    
    public void addDest(
            int rootUserIndex,
            int userIndex,
            MessageDest msgDest
            ) {
        
        if ( m_dataHolder.isUserNewRegisteredWithOverlayForRootUserIndex(
                rootUserIndex,
                userIndex ) ) {
            return;
        }
        
        m_dataHolder.addDest(
                rootUserIndex,
                userIndex,
                msgDest
                );
        
        super.addDest(
                userIndex,
                msgDest
                );
    }
    
    public void removeDest(
            int rootUserIndex,
            int userIndex
            ) {
        m_dataHolder.removeDest(
                rootUserIndex,
                userIndex
                );
        
        super.removeDest(
                userIndex
                );
    }
    
    public void setCommunicationArchitectureType( 
            CommunicationArchitectureType commArchType 
            ) {
        m_dataHolder.setCommunicationArchitectureType( commArchType );
    }
    
    public void setPerformanceOptimizationClient(
            PerformanceOptimizationClient performanceOptimizationClient
            ) {
        m_dataHolder.setPerformanceOptimizationClient( performanceOptimizationClient );
    }
    
    public void sendMsg( 
            Message msg 
            ) {
        try {
            CommandMessage cmdMsg = (CommandMessage) msg;
            if ( cmdMsg.isMsgForLatecomerOrNewMaster() && !m_temporaryForwarderForNewMasterOrLatecomer ) {
                return;
            }
            cmdMsg.setSenderUserIndex( m_userIndex );
            
            int numDestsTransmittedTo = 0;
            int lastDestTransmittedTo = 0;
            
            LoggableMessageForwarderLazyPolicyPerMessageDataHolder curMessageLazyPolicyPerMessageDataHolder = null;
            boolean lazyPfSchedPol = 
                m_logger.getSchedulingPolicyForSysConfigVersion( cmdMsg.getSysConfigVersion() ) == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST;
            
            if ( lazyPfSchedPol ) {
            	curMessageLazyPolicyPerMessageDataHolder = m_dataHolder.getLazyPolicyPerMessageDataHolder(
            			m_logger,
            			cmdMsg
            			);
            	
            	if ( curMessageLazyPolicyPerMessageDataHolder.getHasFirstTransmitThreadQuit() == false ) {
	            	if ( curMessageLazyPolicyPerMessageDataHolder.canFirstTransmissionThreadAtAllDelayProcessing() == false ) {
	            		curMessageLazyPolicyPerMessageDataHolder.firstTransmitThreadQuitting();
	            		return;
	            	}
            	}
            }
            
            long forwardStartTime = System.nanoTime();
            boolean transToFirstDest = true;
            long transTimeToFirstDest = 0;

            Vector<Integer> destUserIndices = null;
            if ( cmdMsg.getDestUserIndices() != null ) {
            	destUserIndices = cmdMsg.getDestUserIndices();
            }
            else {
            	destUserIndices = m_dataHolder.getDestsForSourceUserIndex( cmdMsg.getSourceUserIndex() );
            }

            if ( destUserIndices != null && destUserIndices.size() > 0 ) {

                int indexOfDestToStartTransmittingTo = 0;
            	if ( lazyPfSchedPol ) {
            		indexOfDestToStartTransmittingTo = curMessageLazyPolicyPerMessageDataHolder.getNextIndexToTransmitTo();
        			curMessageLazyPolicyPerMessageDataHolder.prepareForTransmission();
            	}

                if ( ( lazyPfSchedPol && curMessageLazyPolicyPerMessageDataHolder.getHasFirstTransmitThreadQuit() == false ) || 
                		indexOfDestToStartTransmittingTo < destUserIndices.size() ) {
                	
                	for ( int i = indexOfDestToStartTransmittingTo; i < destUserIndices.size(); i++ ) {
	                    
                    	if  ( destUserIndices.get( i ) == m_userIndex ) {
                    		/*
                    		 * This case does not happen if the scheduler optimizes the scheduling of
                    		 * transmission and the only element of destUserIndices is the index of
                    		 * the local user. The reason is that the scheduler will not even schedule
                    		 * a transmission thread in that case.
                    		 */
                    		continue;
                    	}
                    	
	                    int destUserIndex = destUserIndices.elementAt( i );
                        double delayForMsg = cmdMsg.getDelaySoFar();
                        if ( lazyPfSchedPol ) {
                        	delayForMsg = curMessageLazyPolicyPerMessageDataHolder.getDelayForMsg( i );
                        }
                        delayForMsg = MathUtils.round( delayForMsg, 3 );
	                    
                        boolean reportReceiveTime = false;
                        if ( ( i == 0 || i == destUserIndices.size() - 1 ) &&
                                cmdMsg.getReportTransCostForMessage() ) {
                            reportReceiveTime = true;
                        }
                        
                        cmdMsg.setReportReceiveTime( reportReceiveTime );
                        
	                    sendMessageToUser(
	                            destUserIndex,
	                            cmdMsg
	                            );
	                    numDestsTransmittedTo++;
	                    lastDestTransmittedTo = destUserIndex;
	
                        if ( transToFirstDest ) {
                            if ( lazyPfSchedPol ) {
                                if ( curMessageLazyPolicyPerMessageDataHolder.getHasFirstTransmitThreadQuit() == false ) {
                                    transToFirstDest = false;
                                    transTimeToFirstDest = System.nanoTime() - forwardStartTime;
                                }
                            }
                            else {
                                transToFirstDest = false;
                                transTimeToFirstDest = System.nanoTime() - forwardStartTime;
                            }
                        }
                        
                        if ( lazyPfSchedPol ) {
                        	if ( curMessageLazyPolicyPerMessageDataHolder.getHasFirstTransmitThreadQuit() == false ) {
                        		boolean continueToDelayProcessing = true;
                        		if ( i == destUserIndices.size() - 1 ) {
                        			continueToDelayProcessing = false;
                        		}
                        		else {
	                        		continueToDelayProcessing = 
	                        			curMessageLazyPolicyPerMessageDataHolder.canFirstTransmissionThreadContinueToDelayProcessing();
                        		}

                        		if ( continueToDelayProcessing == false ) {
                        			curMessageLazyPolicyPerMessageDataHolder.setNextIndexToTransmitTo( i + 1 );
                        			curMessageLazyPolicyPerMessageDataHolder.setOriginalForwardStartTime( forwardStartTime );
                        			curMessageLazyPolicyPerMessageDataHolder.firstTransmitThreadQuitting();
                        			
		                            m_dataHolder.reportTransInfoToPerfCollector(
		                                    cmdMsg.getMessageType() == MessageType.INPUT
		                                        ? MessageType.PERF_INPUT_TRANS_TIME : MessageType.PERF_OUTPUT_TRANS_TIME,
		                                    cmdMsg.getSysConfigVersion(),
		                                    cmdMsg.getSourceUserIndex(),
		                                    cmdMsg.getSeqId(),
		                                    System.nanoTime() - forwardStartTime,
		                                    transTimeToFirstDest,
		                                    numDestsTransmittedTo,
		                                    lastDestTransmittedTo,
		                                    ( (ASelfOptArchThread) Thread.currentThread() ).getThreadCoreAffinity(),
		                                    cmdMsg.getReportTransCostForMessage()
		                                    );
		
		                            return;
                        		}
                            }
	                    }
	                }
                }
            }

            long forwardEndTime = System.nanoTime();
            
            m_dataHolder.reportTransInfoToPerfCollector(
                    cmdMsg.getMessageType() == MessageType.INPUT
                        ? MessageType.PERF_INPUT_TRANS_TIME : MessageType.PERF_OUTPUT_TRANS_TIME,
                    cmdMsg.getSysConfigVersion(),
                    cmdMsg.getSourceUserIndex(),
                    cmdMsg.getSeqId(),
                    forwardEndTime - forwardStartTime,
                    transTimeToFirstDest,
                    numDestsTransmittedTo,
                    lastDestTransmittedTo,
                    ( (ASelfOptArchThread) Thread.currentThread() ).getThreadCoreAffinity(),
                    cmdMsg.getReportTransCostForMessage()
                    );

            if ( cmdMsg.getMessageType() == MessageType.INPUT ) {
                m_logger.getLoggerUserInfo().addInputTransmissionTime(
                        cmdMsg.getSourceUserIndex(),
                        cmdMsg.getSeqId(),
                        forwardEndTime - forwardStartTime
                        );
            }
            else {
                m_logger.getLoggerUserInfo().addOutputTransmissionTime(
                        cmdMsg.getSourceUserIndex(),
                        cmdMsg.getSeqId(),
                        forwardEndTime - forwardStartTime
                        );
            }
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASerializedObjectTCPLoggableMessageForwarder: Error in sendMsg",
                    e
                    );
        }
    }
    
    private void sendMessageToUser(
            int destUserIndex,
            CommandMessage cmdMsg
            ) {
        try {
            ObjectOutputStream oos = m_ooss.get( destUserIndex );
            if ( oos == null ) {
                return;
            }
            oos.writeObject( cmdMsg );
            oos.flush();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASerializedObjectTCPLoggableMessageForwarder: Error while sending message to user",
                    e
                    );
        }
    }
    
    public void resetMsgSender() {
        super.resetMsgSender();
    }

    protected boolean isUserADestination(
            int userIndex
            ) {
        return m_dataHolder.isUserADestination( userIndex );
    }
    
    public Vector<Integer> getDestsForSourceUserIndex(
    		int sourceUserIndex
    		) {
    	return m_dataHolder.getDestsForSourceUserIndex( sourceUserIndex );
    }

}
