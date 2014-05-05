package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;
import winsyslib.*;

import commonutils.config.*;
import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public class AMetaScheduler 
	extends AScheduler {
	
	protected Scheduler m_inputTransScheduler;
    protected Scheduler m_outputTransScheduler;
	protected Scheduler m_inputProcScheduler;
	protected Scheduler m_outputProcScheduler;
	
	protected BlockingQueue<Integer> m_taskFunneler;
	
	protected Lock m_lock = new ReentrantLock();
	
	protected Vector<CommandMessage> m_msgsScheduledForRelease = new Vector<CommandMessage>();
	protected int m_numTasksDoneForCurUserMsgIndexBeingProcessed = 0;
	protected CommandMessage m_msgBeingProcessed = null;
	
//	protected boolean m_msgBeingProcessedHasLocalProcessingTask = false;
//	protected boolean m_msgBeingProcessedHasLocalTransmissionTask = false;
	protected int m_msgBeingProcessedNumCoresUsed = 0;
	
	protected List<BlockingQueue<Integer>> m_taskTwoQList;
	protected List<BlockingQueue<Integer>> m_taskThreeQList;
	protected List<BlockingQueue<Integer>> m_taskFourQList;
	protected List<BlockingQueue<Integer>> m_taskFiveQList;
    protected List<BlockingQueue<Integer>> m_taskSixQList;

	protected int m_numCores;

    protected int m_coreToUseForProcessingThread;
    protected int m_coreToUseForTransmissionThread;
    protected int m_numCoresUsedByProcAndTransThreads = -1;
    protected boolean m_procAndTransThreadsShareCore = false;
    
    protected SchedulingPolicy m_schedulingPolicyForMsgBeingProcessed;
    protected boolean m_loggerWasMasterInSysConfigOfMsgBeingProcessed;
    
	public AMetaScheduler(
			SchedulingLogger logger,
			int coreToUseForProcessingThread,
			int coreToUseForTransmissionThread
			) {
		super( logger );
		
		m_numCores = WinSysLibUtilities.getNumProcessors();
		
		m_coreToUseForTransmissionThread = coreToUseForProcessingThread;
		m_coreToUseForTransmissionThread = coreToUseForTransmissionThread;

		if ( m_coreToUseForProcessingThread == m_coreToUseForTransmissionThread ) {
			m_procAndTransThreadsShareCore = true;
		}
		
		m_numCoresUsedByProcAndTransThreads = 
			m_procAndTransThreadsShareCore ? 1 : 2;
        
        m_taskTwoQList = new LinkedList<BlockingQueue<Integer>>();
		m_taskThreeQList = new LinkedList<BlockingQueue<Integer>>();
		m_taskFourQList = new LinkedList<BlockingQueue<Integer>>();
		m_taskFiveQList = new LinkedList<BlockingQueue<Integer>>();
        m_taskSixQList = new LinkedList<BlockingQueue<Integer>>();

        for ( int i = 0; i < m_numCores; i++ ) {
            m_taskTwoQList.add( new ArrayBlockingQueue<Integer>( 1 ) );
            m_taskThreeQList.add( new ArrayBlockingQueue<Integer>( 1 ) );
            m_taskFourQList.add( new ArrayBlockingQueue<Integer>( 1 ) );
            m_taskFiveQList.add( new ArrayBlockingQueue<Integer>( 1 ) );
            m_taskSixQList.add( new ArrayBlockingQueue<Integer>( 1 ) );
        }
        
		m_taskFunneler = new ArrayBlockingQueue<Integer>( m_numCores );
		
        m_inputTransScheduler = new ASpecializedTransTaskScheduler( 
        		m_coreToUseForTransmissionThread,
                logger 
				);
		( (AScheduler) m_inputTransScheduler ).setWindowsPriority( WindowsThreadPriority.ABOVE_NORMAL );
		( (AScheduler) m_inputTransScheduler ).start();
		
        m_outputTransScheduler = new ASpecializedTransTaskScheduler( 
        		m_coreToUseForTransmissionThread,
                logger 
                );
        ( (AScheduler) m_outputTransScheduler ).setWindowsPriority( WindowsThreadPriority.ABOVE_NORMAL );
        ( (AScheduler) m_outputTransScheduler ).start();
        
		m_inputProcScheduler = new ASpecializedProcTaskScheduler( 
				m_coreToUseForProcessingThread,
		        logger 
				);
        ( (AScheduler) m_inputProcScheduler ).setWindowsPriority( WindowsThreadPriority.ABOVE_NORMAL );
		( (AScheduler) m_inputProcScheduler ).start();
		
		m_outputProcScheduler = new ASpecializedProcTaskScheduler( 
				m_coreToUseForProcessingThread,
		        logger 
				);
        ( (AScheduler) m_outputProcScheduler ).setWindowsPriority( WindowsThreadPriority.ABOVE_NORMAL );
		( (AScheduler) m_outputProcScheduler ).start();
		
		AMetaSchedulerFunnelerThread thread = new AMetaSchedulerFunnelerThread(
				m_taskFunneler,
				this
				);
		thread.start();
		
	}
	
	protected void scheduleMsg( 
			CommandMessage msg,
			List<BlockingQueue<Integer>> waitForQList,
			List<BlockingQueue<Integer>> signalForQList
			) {
		
		m_lock.lock();
		try {
			if ( releaseMsgsTasks( msg ) == false ) {
			    if ( m_inOrderMsgDeliveryRequired && 
			            ( msg.getMessageType() == MessageType.OUTPUT ||
			                    m_logger.getLoggerSysConfig( msg.getSysConfigVersion() ).IsMaster ) ) {
                    addMsgToListOfMsgsReceivedForUserIndex(
                            msg
                            );
			    } 
			    else {
			        m_msgsScheduledForRelease.add( msg );
			    }
			    
			    return;
			}
			
			if ( m_msgsScheduledForRelease.size() > 0 ) {
			    if ( msg.getMessageType() == MessageType.INPUT ) {
			        m_msgsScheduledForRelease.add( msg );
			        msg = m_msgsScheduledForRelease.remove( 0 );
			    }
			}

			m_msgBeingProcessed = msg;
			
//			/*
//			 * The logic that determines whether or not the scheduler can avoid scheduling unnecessary
//			 * processing and transmission threads for the message being processed
//			 */
//			{
//				m_msgBeingProcessedNumCoresUsed = m_numCoresUsedByProcAndTransThreads;
//				
//				/*
//				 * Local processing?
//				 */
//				m_msgBeingProcessedHasLocalProcessingTask = true;
//				if ( msg.getDestUserIndices() != null &&
//						msg.getDestUserIndices().contains( m_logger.getUserIndex() ) == false ) {
//					m_msgBeingProcessedHasLocalProcessingTask = false;
//				}
//				
//				/*
//				 * Local transmission?
//				 */
//				m_msgBeingProcessedHasLocalTransmissionTask = true;
//				if ( msg.getDestUserIndices() != null &&
//						msg.getDestUserIndices().size() == 1 &&
//						msg.getDestUserIndices().get( 0 ) == m_logger.getUserIndex() ) {
//					m_msgBeingProcessedHasLocalTransmissionTask = false;
//				}
//				else if ( msg.getDestUserIndices() != null ) {
//					Vector<Integer> dests = m_logger.getTransDests(
//							msg.getSourceUserIndex(),
//							msg.getMessageType(),
//							msg.getSysConfigVersion()
//							);
//					boolean foundAtLeastOneDestToTransTo = false;
//					for ( int i = 0; i < msg.getDestUserIndices().size(); i++ ) {
//						if ( dests.contains( msg.getDestUserIndices().get( i ) ) ) {
//							foundAtLeastOneDestToTransTo = true;
//							break;
//						}
//					}
//					m_msgBeingProcessedHasLocalTransmissionTask = foundAtLeastOneDestToTransTo;
//				}
//				else {
//					Vector<Integer> dests = m_logger.getTransDests(
//							msg.getSourceUserIndex(),
//							msg.getMessageType(),
//							msg.getSysConfigVersion()
//							);
//					if ( dests == null || dests.size() == 0 ) {
//						m_msgBeingProcessedHasLocalTransmissionTask = false;
//					}
//				}
//				
//				/*
//				 * What is the total number of cores used locally for this command?
//				 */
//				if ( m_msgBeingProcessedNumCoresUsed > 1 && 
//						( m_msgBeingProcessedHasLocalProcessingTask == false ||
//								m_msgBeingProcessedHasLocalTransmissionTask == false ) ) {
//					m_msgBeingProcessedNumCoresUsed--;
//				}
//			}
			
            ALoggerSysConfigInfo sysConfig = m_logger.getLoggerSysConfig( m_msgBeingProcessed.getSysConfigVersion() );
	        m_schedulingPolicyForMsgBeingProcessed = sysConfig.SchedulingPolicy;
	        m_loggerWasMasterInSysConfigOfMsgBeingProcessed = sysConfig.IsMaster;
	        
            if ( m_inOrderMsgDeliveryRequired && 
                    ( m_msgBeingProcessed.getMessageType() == MessageType.OUTPUT || 
                            m_logger.getLoggerSysConfig( m_msgBeingProcessed.getSysConfigVersion() ).IsMaster ) ) {
    	        m_latestSeqIdForUserIndexReceived.put(
    	                m_msgBeingProcessed.getSourceUserIndex(),
    	                m_msgBeingProcessed.getSeqId()
    	                );
            }
		}
		finally {
			m_lock.unlock();
		}
		
		scheduleMe(
		        m_msgBeingProcessed
				);
	}
	
	public void msgTaskCompleted() {
		m_lock.lock();
		CommandMessage nextMsg = null;
		try {
			if ( areAllTasksForMsgCompleted() == false ) {
				return;
			}
			m_numTasksDoneForCurUserMsgIndexBeingProcessed = 0;
			
			m_msgBeingProcessed = null;
			if ( m_msgsScheduledForRelease.size() > 0 ) {
			    nextMsg = m_msgsScheduledForRelease.remove( 0 );
			}
			else if ( m_inOrderMsgDeliveryRequired ) {
    			nextMsg = getMessageWhoseTasksCanBeReleased();
            }
			
			if ( nextMsg == null ) {
			    return;
			}
			m_msgBeingProcessed = nextMsg;
			
            ALoggerSysConfigInfo sysConfig = m_logger.getLoggerSysConfig( m_msgBeingProcessed.getSysConfigVersion() );
            m_schedulingPolicyForMsgBeingProcessed = sysConfig.SchedulingPolicy;
            m_loggerWasMasterInSysConfigOfMsgBeingProcessed = sysConfig.IsMaster;
			
            if ( m_inOrderMsgDeliveryRequired && 
                    ( m_msgBeingProcessed.getMessageType() == MessageType.OUTPUT || 
                            m_logger.getLoggerSysConfig( m_msgBeingProcessed.getSysConfigVersion() ).IsMaster ) ) {
                m_latestSeqIdForUserIndexReceived.put(
                        m_msgBeingProcessed.getSourceUserIndex(),
                        m_msgBeingProcessed.getSeqId()
                        );
            }
		}
		finally {
			m_lock.unlock();
		}
		scheduleMe( nextMsg );
	}
	
	private void scheduleMe(
			CommandMessage msg
			) {
 		SchedulingPolicy schedulingPolicyToUse = m_schedulingPolicyForMsgBeingProcessed;
 		
 		if ( msg.getSuggestedSchedulingPolicyForMsg() != SchedulingPolicy.UNDEFINED ) {
 		    schedulingPolicyToUse = msg.getSuggestedSchedulingPolicyForMsg();
 		}
 		
 		CommandMessage msgToProcess = ACommandMessage.copy( msg );
 		CommandMessage msgToTransmit = ACommandMessage.copy( msg );
 		
        MessageTaskSchedule seqIdTaskSchedule = getMessageTaskSchedule(
                msg.getSeqId(),
                schedulingPolicyToUse
                );
        
		if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST 
                || schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST
                || schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_CONCURRENT
                ) {
			
			List<BlockingQueue<Integer>> procWaitForQList = null;
			List<BlockingQueue<Integer>> procSignalDoneQList = null;
			List<BlockingQueue<Integer>> transWaitForQList = null;
			List<BlockingQueue<Integer>> transSignalDoneQList = null;

			if ( msg.getMessageType() == MessageType.INPUT ) {
				
				procWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.INPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				procSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.INPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.INPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.INPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
					
				/* 
				 * Make sure to ALWAYS schedule tasks that will block immediately first;
				 * otherwise, you may schedule a task that doesn't block and another task 
				 * may come in and start tasks that block the tasks you haven't scheduled
				 * for the first task, even though those tasks must complete before any
				 * new tasks can actually begin. Thus you will get deadlocks! 
				 */
				
				if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
                    scheduleTransmissionThread( msgToTransmit, transWaitForQList, transSignalDoneQList );
                    if ( m_loggerWasMasterInSysConfigOfMsgBeingProcessed ) {
                        scheduleProcessingThread( msgToProcess, procWaitForQList, procSignalDoneQList );
                    }
				}
				else if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
                    if ( m_loggerWasMasterInSysConfigOfMsgBeingProcessed ) {
                        scheduleProcessingThread( msgToProcess, procWaitForQList, procSignalDoneQList );
                    }
                    scheduleTransmissionThread( msgToTransmit, transWaitForQList, transSignalDoneQList );
				}
				else if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
                    scheduleTransmissionThread( msgToTransmit, transWaitForQList, transSignalDoneQList );
                    if ( m_loggerWasMasterInSysConfigOfMsgBeingProcessed ) {
                        scheduleProcessingThread( msgToProcess, procWaitForQList, procSignalDoneQList );
                    }
				}

			}
			else if ( msg.getMessageType() == MessageType.OUTPUT ) {

				procWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.OUTPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				procSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.OUTPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.OUTPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.OUTPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				
				/* 
				 * Make sure to ALWAYS schedule tasks that will block immediately first
				 * Otherwise. You may schedule a task that doesn't block and another task 
				 * may come in and start tasks that block the tasks you haven't scheduled
				 * for the first task, even though those tasks must complete before any
				 * new tasks can actually begin. Thus you will get deadlocks! 
				 */
				
				if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
                    scheduleTransmissionThread( msgToTransmit, transWaitForQList, transSignalDoneQList );
                    scheduleProcessingThread( msgToProcess, procWaitForQList, procSignalDoneQList );
				}
				else if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
                    scheduleProcessingThread( msgToProcess, procWaitForQList, procSignalDoneQList );
                    scheduleTransmissionThread( msgToTransmit, transWaitForQList, transSignalDoneQList );
				}
				else if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
                    scheduleTransmissionThread( msgToTransmit, transWaitForQList, transSignalDoneQList );
                    scheduleProcessingThread( msgToProcess, procWaitForQList, procSignalDoneQList );
				}				
			}
			
		}
		else if ( schedulingPolicyToUse == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
			
			List<BlockingQueue<Integer>> procWaitForQList = null;
			List<BlockingQueue<Integer>> procSignalDoneQList = null;
			List<BlockingQueue<Integer>> transWaitForQList = null;
			List<BlockingQueue<Integer>> transSignalDoneQList = null;
			List<BlockingQueue<Integer>> transPart2WaitForQList = null;
			List<BlockingQueue<Integer>> transPart2SignalDoneQList = null;

			if ( msg.getMessageType() == MessageType.INPUT ) {
				procWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.INPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				procSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.INPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.INPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.INPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transPart2WaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.INPUT_TRANS_PART_2,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transPart2SignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.INPUT_TRANS_PART_2,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);			
				
				if ( m_loggerWasMasterInSysConfigOfMsgBeingProcessed ) {
					m_inputProcScheduler.scheduleMsgDelivery(
							new ASchedulableEntity( msg, procWaitForQList, procSignalDoneQList )
							);
				}
				
				m_inputTransScheduler.scheduleMsgDelivery(
						new ASchedulableEntity( msg, transWaitForQList, transSignalDoneQList )
						);					
				m_inputTransScheduler.scheduleMsgDelivery(
						new ASchedulableEntity( msg, transPart2WaitForQList, transPart2SignalDoneQList )
						);
			}
			else if ( msg.getMessageType() == MessageType.OUTPUT ) {
				procWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.OUTPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				procSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.OUTPUT_PROC,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transWaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.OUTPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transSignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.OUTPUT_TRANS,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transPart2WaitForQList = seqIdTaskSchedule.getWaitQ(
						TaskType.OUTPUT_TRANS_PART_2,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				transPart2SignalDoneQList = seqIdTaskSchedule.getSignalDoneQ(
						TaskType.OUTPUT_TRANS_PART_2,
						m_loggerWasMasterInSysConfigOfMsgBeingProcessed,
						m_architecture
						);
				
				m_outputProcScheduler.scheduleMsgDelivery(
						new ASchedulableEntity( msg, procWaitForQList, procSignalDoneQList )
						);
				
				m_outputTransScheduler.scheduleMsgDelivery(
						new ASchedulableEntity( msg, transWaitForQList, transSignalDoneQList )
						);					
				m_outputTransScheduler.scheduleMsgDelivery(
						new ASchedulableEntity( msg, transPart2WaitForQList, transPart2SignalDoneQList )
						);
			}
			
		}
	}
	
    protected void scheduleProcessingThread(
            CommandMessage msg,
            List<BlockingQueue<Integer>> waitForQList,
            List<BlockingQueue<Integer>> signalDoneQList
            ) {
        Scheduler schedulerToUse = msg.getMessageType() == MessageType.INPUT ? m_inputProcScheduler : m_outputProcScheduler;
        schedulerToUse.scheduleMsgDelivery(
                new ASchedulableEntity( msg, waitForQList, signalDoneQList )
                );
    }
    
	protected void scheduleTransmissionThread(
	        CommandMessage msg,
	        List<BlockingQueue<Integer>> waitForQList,
	        List<BlockingQueue<Integer>> signalDoneQList
	        ) {
        Scheduler schedulerToUse = msg.getMessageType() == MessageType.INPUT ? m_inputTransScheduler : m_outputTransScheduler;
        schedulerToUse.scheduleMsgDelivery(
                new ASchedulableEntity( msg, waitForQList, signalDoneQList )
                );
	}
	
	protected MessageTaskSchedule getMessageTaskSchedule(
			int seqId,
			SchedulingPolicy schedulingPolicyToUse
			) {
		return new AMessageTaskSchedule(
				schedulingPolicyToUse,
				m_coreToUseForProcessingThread,
				m_coreToUseForTransmissionThread,
				m_taskFunneler,
				m_taskTwoQList,
				m_taskThreeQList,
				m_taskFourQList,
				m_taskFiveQList,
				m_taskSixQList,
				false,
				false
//				m_msgBeingProcessedHasLocalProcessingTask,
//				m_msgBeingProcessedHasLocalTransmissionTask
				);
	}

	protected boolean areAllTasksForMsgCompleted() {

//		int noLocalProcOffset = m_msgBeingProcessedHasLocalProcessingTask ? 0 : -1; 
//		int noLocalTransOffset = m_msgBeingProcessedHasLocalTransmissionTask ? 0 : -1; 
		int noLocalProcOffset = 0; 
		int noLocalTransOffset = 0; 
		m_msgBeingProcessedNumCoresUsed = m_numCoresUsedByProcAndTransThreads;
		
		if ( m_schedulingPolicyForMsgBeingProcessed == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
		    /*
		     * When threads for a task execute concurrently, we count how many threads
		     * complete in order to know whether all of the threads have completed
		     */
			if ( m_loggerWasMasterInSysConfigOfMsgBeingProcessed == true ) {
			    /* 
			     * Since this is a master, then transmission and processing threads have to complete
			     * both input and output transmission and processing
			     */
				if ( m_numTasksDoneForCurUserMsgIndexBeingProcessed < ( 3 + 2 * noLocalProcOffset + 2 * noLocalTransOffset ) ) {
					m_numTasksDoneForCurUserMsgIndexBeingProcessed++;
					return false;
				}
			}
			else {
                /* 
                 * Since this is slave, then in the case of
                 * - input commands, only the transmission thread scheduled to transmit the input
                 *   command to the master needs to finish 
                 * - output commands, both the transmission and the processing threads need to finish
                 */
                if ( m_msgBeingProcessed.getMessageType() == MessageType.INPUT ) {
                    return true;
                }
                else if ( m_msgBeingProcessed.getMessageType() == MessageType.OUTPUT ) {
					if ( m_numTasksDoneForCurUserMsgIndexBeingProcessed < ( 1 + noLocalProcOffset + noLocalTransOffset ) ) {
						m_numTasksDoneForCurUserMsgIndexBeingProcessed++;
						return false;
					}
				}
			}
		} else {
		    /*
		     * When threads do not execute sequentially, we wait for the last thread that is scheduled
		     * to run on each core to complete in order to know that all of the threads for the task
		     * have completed
		     */
		    if ( m_loggerWasMasterInSysConfigOfMsgBeingProcessed ) {
		        /* 
		         * Since this is a master, we wait for the last thread scheduled on each of the
		         * cores to complete output processing and transmission.
		         */
                if ( m_numTasksDoneForCurUserMsgIndexBeingProcessed < ( m_msgBeingProcessedNumCoresUsed - 1 ) ) {
                    m_numTasksDoneForCurUserMsgIndexBeingProcessed++;
                    return false;
                }
		    }
		    else {
		        /*
		         * Since this is a slave, then when considering
		         * - input commands, we wait for the single thread scheduled to transmit the input
		         *   to the master to complete
		         * - output commands, we wait for the last thread scheduled on each of the cores
                 *   to complete output processing and transmission
		         */

		        /*
		         * TODO:
		         * Bug:
		         * A NullPointerException is sometimes encountered here. The reason must be that
		         * m_msgBeingProcessed is null. However, it should never be null at this point.
		         */
		        if ( m_msgBeingProcessed.getMessageType() == MessageType.INPUT ) {
		            return true;
		        }
		        else {
	                if ( m_numTasksDoneForCurUserMsgIndexBeingProcessed < ( m_msgBeingProcessedNumCoresUsed - 1 ) ) {
	                    m_numTasksDoneForCurUserMsgIndexBeingProcessed++;
	                    return false;
	                }
		        }
		    }
		}

		return true;
	}
	
	protected boolean releaseMsgsTasks(
			CommandMessage msg
			) {
		
		if ( m_msgBeingProcessed != null ) {
			if ( m_msgBeingProcessed.getMessageType() == MessageType.INPUT &&
                    msg.getMessageType() == MessageType.OUTPUT &&
			        msg.getSourceUserIndex() == m_msgBeingProcessed.getSourceUserIndex() &&
			        msg.getSeqId() == m_msgBeingProcessed.getSeqId() ) {
		        return true;
			}
		}
		else if ( m_inOrderMsgDeliveryRequired ) {
		    if ( msg.isMsgForLatecomerOrNewMaster() == false &&
		            msg.getMessageType() == MessageType.INPUT && 
		            m_logger.getLoggerSysConfig( msg.getSysConfigVersion() ).IsMaster == false ) {
		        return true;
		    }
		    
            int sourceUserIndex = msg.getSourceUserIndex();
            int seqId = msg.getSeqId();

            Integer latestSeqIdInt = m_latestSeqIdForUserIndexReceived.get( sourceUserIndex );
            int latestSeqId = -1;
            if ( latestSeqIdInt != null ) {
                latestSeqId = latestSeqIdInt.intValue();
            }
            
            if ( seqId != latestSeqId + 1 ) {
                return false;
            }
            
            return true;
		}
		else {
			return true;
		}
		
		return false;
	}
}

class AMetaSchedulerFunnelerThread
	extends ASelfOptArchThread {
	
	BlockingQueue<Integer> m_funnel;
	AMetaScheduler m_metaScheduler;
	
	public AMetaSchedulerFunnelerThread(
			BlockingQueue<Integer> funnel,
			AMetaScheduler metaScheduler
			) {
		m_funnel = funnel;
		m_metaScheduler = metaScheduler;

        int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
        coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );
	}
	
	public void run() {
	    super.run();
	    
		for (;;) {
			try {
				m_funnel.take();
				m_metaScheduler.msgTaskCompleted();
			}
			catch ( Exception e ) {
	            ErrorHandlingUtils.logSevereExceptionAndContinue(
	                    "AMetaSchedulerFunnelerThread: Error in run",
	                    e
	                    );
			}
		}
	}
	
}
