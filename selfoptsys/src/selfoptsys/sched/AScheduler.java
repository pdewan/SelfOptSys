package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;

import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;


import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;
import commonutils.config.*;


public abstract class AScheduler 
	extends ASelfOptArchThread
	implements Scheduler {

	protected ConfigParamProcessor m_mainCpp;
	
	protected SchedulingLogger m_logger = null;
	protected MessageForwarder m_msgForwarder = null;
	
	protected BlockingQueue<SchedulableEntity> m_queuedMsgs;
	
	protected ProcessingArchitectureType m_architecture;
	protected OperationMode m_operationMode;
	
	protected int m_lazyDelay = 0;
	
    protected boolean m_inOrderMsgDeliveryRequired = false;
	protected Map<Integer, Integer> m_latestSeqIdForUserIndexReceived;
	protected Map<Integer, List<CommandMessage>> m_cmdsReceivedForUserIndex;
    
    public AScheduler(
			SchedulingLogger logger
			) {
		m_mainCpp = AMainConfigParamProcessor.getInstance();
		
		m_logger = logger;
		
		m_queuedMsgs = new LinkedBlockingQueue<SchedulableEntity>( MAX_QUEUED_MSGS );
		
		m_lazyDelay = m_mainCpp.getIntParam( Parameters.LAZY_DELAY );

		m_architecture = ProcessingArchitectureType.valueOf( m_mainCpp.getStringParam( Parameters.PROCESSING_ARCHITECTURE ) );
		m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
		
        int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
        coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );
        
        m_latestSeqIdForUserIndexReceived = new Hashtable<Integer, Integer>();
        m_cmdsReceivedForUserIndex = new Hashtable<Integer, List<CommandMessage>>();
	}
	
	public synchronized void scheduleMsgDelivery(
	        SchedulableEntity schedEntity
	        ) {
		try {
			m_queuedMsgs.put( schedEntity );
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AScheduler: Error in scheduleMsgDelivery",
                    e
                    );
		}
	}
	
	public void run() {
	    super.run();
	    
		try {
			for (;;) {
				SchedulableEntity schedEntity = m_queuedMsgs.take();
				scheduleMsg( 
						schedEntity.getMsg(),
						schedEntity.getWaitForQList(),
						schedEntity.getSignalDoneQList()
						);
			}
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AScheduler: Error while running",
                    e
                    );
		}
	}
	
	public void stopScheduler() {
		this.interrupt();
	}
	
	protected abstract void scheduleMsg( 
			CommandMessage msg,
            List<BlockingQueue<Integer>> waitForQList,
            List<BlockingQueue<Integer>> signalForQList
			);
	
	public boolean isInOrderMessageDeliveryRequired() {
	    return m_inOrderMsgDeliveryRequired;
	}
    public void isInOrderMessageDeliveryRequired(
            boolean inOrderMessageDeliveryRequired
            ) {
        m_inOrderMsgDeliveryRequired = inOrderMessageDeliveryRequired;
    }
    
    public void resetScheduler() {
        if ( m_inOrderMsgDeliveryRequired ) {
            m_cmdsReceivedForUserIndex.clear();
            m_latestSeqIdForUserIndexReceived.clear();
        }
    }
    
    protected void addMsgToListOfMsgsReceivedForUserIndex(
            CommandMessage msg
            ) {
        List<CommandMessage> msgs = m_cmdsReceivedForUserIndex.get( msg.getSourceUserIndex() );
        if ( msgs == null ) {
            msgs = new LinkedList<CommandMessage>();
            m_cmdsReceivedForUserIndex.put(
                    msg.getSourceUserIndex(),
                    msgs
                    );
        }
        int insertionPoint = 0;
        for ( int i = 0; i < msgs.size(); i++ ) {
            if ( msgs.get( i ).getSeqId() < msg.getSeqId() ) {
                insertionPoint++;
            }
            else {
                break;
            }
        }
        msgs.add(
                insertionPoint,
                msg
                );
    }
    
    protected CommandMessage getMessageWhoseTasksCanBeReleased() {
        
        CommandMessage msg = null;
        
        Iterator<Integer> userIndexItr = m_cmdsReceivedForUserIndex.keySet().iterator();
        while ( userIndexItr.hasNext() ) {
            int userIndex = userIndexItr.next();
            
            Integer latestSeqIdForUserIndexInt = m_latestSeqIdForUserIndexReceived.get( userIndex );
            int latestSeqIdForUserIndex = -1;
            if ( latestSeqIdForUserIndexInt != null ) {
                latestSeqIdForUserIndex = latestSeqIdForUserIndexInt.intValue();
            }
            
            int nextSeqIdAvailableForUserIndex = -1;
            List<CommandMessage> cmdsAvailableForUserIndex = m_cmdsReceivedForUserIndex.get( userIndex );
            if ( cmdsAvailableForUserIndex != null && cmdsAvailableForUserIndex.size() > 0 ) {
                nextSeqIdAvailableForUserIndex = cmdsAvailableForUserIndex.get( 0 ).getSeqId();
            }

            if ( latestSeqIdForUserIndex == nextSeqIdAvailableForUserIndex - 1 ) {
                msg = cmdsAvailableForUserIndex.remove( 0 );
                break;
            }
            else if ( nextSeqIdAvailableForUserIndex != -1 &&
                    latestSeqIdForUserIndex >= nextSeqIdAvailableForUserIndex ) {
                while ( cmdsAvailableForUserIndex.get( 0 ).getSeqId() <= latestSeqIdForUserIndex ) {
                    cmdsAvailableForUserIndex.remove( 0 );
                    if ( cmdsAvailableForUserIndex.size() == 0 ) {
                        break;
                    }
                }
                if ( cmdsAvailableForUserIndex.size() > 0 ) {
                    nextSeqIdAvailableForUserIndex = cmdsAvailableForUserIndex.get( 0 ).getSeqId();
                    if ( latestSeqIdForUserIndex == nextSeqIdAvailableForUserIndex - 1 ) {
                        msg = cmdsAvailableForUserIndex.remove( 0 );
                        break;
                    }
                }
            }
        }
        
        return msg;
        
    }
}
