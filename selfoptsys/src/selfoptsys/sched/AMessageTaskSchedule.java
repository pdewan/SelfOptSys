package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;
import winsyslib.*;

import commonutils.config.*;


public class AMessageTaskSchedule 
	implements MessageTaskSchedule {
	
	protected SchedulingPolicy m_schedulingPolicy;
	
	protected List<BlockingQueue<Integer>> m_taskTwoQList;
	protected List<BlockingQueue<Integer>> m_taskThreeQList;
	protected List<BlockingQueue<Integer>> m_taskFourQList;
	protected List<BlockingQueue<Integer>> m_taskFiveQList;
    protected List<BlockingQueue<Integer>> m_taskSixQList;
	
	protected BlockingQueue<Integer> m_taskFunneler;
	
	protected int m_numCores;
	
	protected int m_coreToUseForProcessingThread;
	protected int m_coreToUseForTransmissionThread;
	
	protected int m_sharedProcAndTransThreadCore = -1;
	
//	protected boolean m_localProcessingNeeded = false;
//	protected boolean m_localTransmissionNeeded = false;
	
	public AMessageTaskSchedule(
			SchedulingPolicy schedulingPolicy,
			int coreToUseForProcessingThread,
			int coreToUseForTransmissionThread,
			BlockingQueue<Integer> taskFunneler,
			List<BlockingQueue<Integer>> taskTwoBBQList,
			List<BlockingQueue<Integer>> taskThreeBBQList,
			List<BlockingQueue<Integer>> taskFourBBQList,
			List<BlockingQueue<Integer>> taskFiveBBQList,
            List<BlockingQueue<Integer>> taskSixBBQList,
            boolean localProcessingNeeded,
            boolean localTransmissionNeeded
			) {
		m_schedulingPolicy = schedulingPolicy;
		
		m_taskTwoQList = taskTwoBBQList;
		m_taskThreeQList = taskThreeBBQList;
		m_taskFourQList = taskFourBBQList;
		m_taskFiveQList = taskFiveBBQList;
		m_taskSixQList = taskSixBBQList;
		
		m_taskFunneler = taskFunneler;
		
		m_numCores = WinSysLibUtilities.getNumProcessors();
		
		m_coreToUseForProcessingThread = coreToUseForProcessingThread;
		m_coreToUseForTransmissionThread = coreToUseForTransmissionThread;

		m_sharedProcAndTransThreadCore = 
			m_coreToUseForProcessingThread == m_coreToUseForTransmissionThread ? m_coreToUseForProcessingThread : -1;
		
//		m_localProcessingNeeded = localProcessingNeeded;
//		m_localTransmissionNeeded = localTransmissionNeeded;
	}
	
	public SchedulingPolicy getSchedulingPolicy() {
		return m_schedulingPolicy;
	}
	
	public List<BlockingQueue<Integer>> getWaitQ( 
			TaskType taskType,
			boolean masterUser,
			ProcessingArchitectureType architecture
			) {

		List<BlockingQueue<Integer>> returnVal = null;
		
		if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
            if ( masterUser ) {
                returnVal = getMultiCoreProcessFirstWaitForQListForMasterUser( taskType );
            }
            else {
                returnVal = getMultiCoreProcessFirstWaitForQListForSlaveUser( taskType );
            }
		}
		else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
			if ( masterUser ) {
				returnVal = getMultiCoreTransmitFirstWaitForQListForMasterUser( taskType );
			}
			else {
				returnVal = getMultiCoreTransmitFirstWaitForQListForSlaveUser( taskType );
			}
		}
		else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
			if ( masterUser ) {
				returnVal = getMultiCoreConcurrentWaitForQListForMasterUser( taskType );
			}
			else {
				returnVal = getMultiCoreConcurrentWaitForQListForSlaveUser( taskType );
			}
		}
		else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
			if ( masterUser ) {
				returnVal = getMultiCoreLazyProcessFirstWaitForQListForMasterUser( taskType, architecture );
			}
			else {
				returnVal = getMultiCoreLazyProcessFirstWaitForQListForSlaveUser( taskType );
			}
		}
	
		return returnVal;

	}
	
	public List<BlockingQueue<Integer>> getSignalDoneQ( 
			TaskType taskType,
			boolean masterUser,
			ProcessingArchitectureType architecture
			) {

	    List<BlockingQueue<Integer>> returnVal = null;
		
		if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
            if ( masterUser ) {
                returnVal = getMultiCoreProcessFirstSignalDoneQListForMasterUser( taskType );
            }
            else {
                returnVal = getMultiCoreProcessFirstSignalDoneQListForSlaveUser( taskType );
            }
		}
		else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
			if ( masterUser ) {
				returnVal = getMultiCoreTransmitFirstSignalDoneQListForMasterUser( taskType );
			}
			else {
				returnVal = getMultiCoreTransmitFirstSignalDoneQListForSlaveUser( taskType );
			}
		}
		else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
			if ( masterUser ) {
				returnVal = getMultiCoreConcurrentSignalDoneQListForMasterUser( taskType );
			}
			else {
				returnVal = getMultiCoreConcurrentSignalDoneQListForSlaveUser( taskType );
			}
		}
		else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
			if ( masterUser ) {
				returnVal = getMultiCoreLazyProcessFirstSignalDoneQListForMasterUser( taskType, architecture );
			}
			else {
				returnVal = getMultiCoreLazyProcessFirstSignalDoneQListForSlaveUser( taskType );
			}
		}
		
		return returnVal;

	}
	
	protected List<BlockingQueue<Integer>> getMultiCoreProcessFirstWaitForQListForMasterUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_PROC ) {
            /*
             * With process first, input processing doesn't wait for anything
             */
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            /*
             * With process first, output processing waits only for input processing
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_coreToUseForProcessingThread == i ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_TRANS ) {
            /*
             * With process first, input transmission waits for output processing but
             * only if the transmission thread is running on the same core as a
             * processing thread; otherwise, it doesn't wait.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localProcessingNeeded ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            /*
             * With process first, output transmission must wait for input transmission
             */
            return m_taskFourQList;
        }

        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreProcessFirstWaitForQListForSlaveUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_PROC ) {
            /*
             * Slaves do not process inputs
             */
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            /*
             * With process first, output processing on a slave does not wait for anything
             */
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_TRANS ) {
            /*
             * Slaves do not transmit inputs
             */
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            /*
             * With process first, output transmission waits only for output processing but
             * only if the transmission thread is running on the same core as a
             * processing thread.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localProcessingNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }

        return null;
    }

    protected List<BlockingQueue<Integer>> getMultiCoreProcessFirstSignalDoneQListForMasterUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_coreToUseForProcessingThread == i ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localTransmissionNeeded ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else if ( m_coreToUseForProcessingThread == i ){
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_coreToUseForTransmissionThread == i ) {
                    qList.add( m_taskFourQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createTaskFunnelerQListForTransmissionThreads();
        }

        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreProcessFirstSignalDoneQListForSlaveUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localTransmissionNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else if ( m_coreToUseForProcessingThread == i ) {
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_TRANS ) {
//            return createTaskFunnelerQListForTransmissionThreads();
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createTaskFunnelerQListForTransmissionThreads();
        }

        return null;
    }

    protected List<BlockingQueue<Integer>> getMultiCoreTransmitFirstWaitForQListForMasterUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
            /*
             * With transmit first, input transmission does not wait for anything
             */
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            /*
             * With transmit first, input processing waits for input transmission but
             * only if the processing thread is running on the same core as the
             * transmission thread.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localTransmissionNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            /*
             * With transmit first, output transmission waits for input processing if
             * the transmission thread is running on the same core as a processing thread;
             * otherwise, the output transmission waits for input transmission;
             */
            return m_taskThreeQList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
//        else if ( taskType == TaskType.OUTPUT_PROC && m_localTransmissionNeeded ) {
            /*
             * With transmit first, output processing waits for output transmission
             * if the processing thread is running on the same core as the
             * transmission thread; otherwise it waits for input processing
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localTransmissionNeeded ) {
                    qList.add( m_taskFourQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreTransmitFirstWaitForQListForSlaveUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            /*
             * With transmit first, output processing waits for output transmission but
             * only if the processing thread is running on the same core as the
             * transmission thread.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localTransmissionNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        
        return null;
    }

    protected List<BlockingQueue<Integer>> getMultiCoreTransmitFirstSignalDoneQListForMasterUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localProcessingNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else if ( m_coreToUseForProcessingThread == i ){
                    qList.add( m_taskFourQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localProcessingNeeded ) {
                    qList.add( m_taskFourQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ){
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            return createTaskFunnelerQListForProcessingThreads();
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreTransmitFirstSignalDoneQListForSlaveUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
//            return createTaskFunnelerQListForTransmissionThreads();
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localProcessingNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ){
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            return createTaskFunnelerQListForProcessingThreads();
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreConcurrentWaitForQListForMasterUser(
            TaskType taskType
            ) {
        if ( taskType == TaskType.INPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            return createNullQList();
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreConcurrentWaitForQListForSlaveUser(
            TaskType taskType
            ) {
        if ( taskType == TaskType.INPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            return createNullQList();
        }
        
        return null;
    }

    protected List<BlockingQueue<Integer>> getMultiCoreConcurrentSignalDoneQListForMasterUser(
            TaskType taskType
            ) {
        if ( taskType == TaskType.INPUT_TRANS ) {
            return createTaskFunnelerQListForTransmissionThreads();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createTaskFunnelerQListForProcessingThreads();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createTaskFunnelerQListForTransmissionThreads();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            return createTaskFunnelerQListForProcessingThreads();
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreConcurrentSignalDoneQListForSlaveUser(
            TaskType taskType
            ) {
        if ( taskType == TaskType.INPUT_TRANS ) {
//            return createTaskFunnelerQListForTransmissionThreads();
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createTaskFunnelerQListForTransmissionThreads();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            return createTaskFunnelerQListForProcessingThreads();
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreLazyProcessFirstWaitForQListForMasterUser(
            TaskType taskType,
            ProcessingArchitectureType architecture
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
            /*
             * With lazy process first, input transmission does not wait for anything
             */
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            /*
             * With lazy process first, input processing waits for input transmission but
             * only if the processing thread is running on the same core as the
             * transmission thread. Note that the transmission thread in this case should
             * stop after transmitting for the maximum allowed lazy delay and will then
             * unblock the processing task.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
//                if ( m_sharedProcAndTransThreadCore == i && m_localTransmissionNeeded ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_TRANS_PART_2 ) {
            /*
             * With lazy process first, input transmission part 2 waits for processing thread,
             * but only if the processing thread shares its core with a transmission thread.
             */
            return m_taskThreeQList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            /*
             * With lazy process first, output transmission waits for the second transmission if
             * the transmission thread is running on the same core as a processing thread;
             * otherwise, the output transmission waits for input transmission;
             */
            return m_taskFourQList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            /*
             * With lazy process first, output processing waits for output transmission
             * if the processing thread is running on the same core as the
             * transmission thread; otherwise it waits for input processing.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskFiveQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS_PART_2 ) {
            /*
             * With lazy process first, output transmission part 2 waits for processing thread if
             * the transmission thread is running on the same core as a processing thread;
             * otherwise, the output transmission part 2 is never scheduled
             */
            return m_taskSixQList;
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreLazyProcessFirstWaitForQListForSlaveUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_TRANS_PART_2 ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            /*
             * With transmit first, output processing waits for output transmission but
             * only if the processing thread is running on the same core as the
             * transmission thread.
             */
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS_PART_2 ) {
            /*
             * With transmit first, output processing waits for output transmission but
             * only if the processing thread is running on the same core as the
             * transmission thread.
             */
            return m_taskThreeQList;
        }
        
        return null;
    }

    protected List<BlockingQueue<Integer>> getMultiCoreLazyProcessFirstSignalDoneQListForMasterUser(
            TaskType taskType,
            ProcessingArchitectureType architecture
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else if ( m_coreToUseForProcessingThread == i ){
                    qList.add( m_taskFiveQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.INPUT_TRANS_PART_2 ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskFourQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ) {
                    qList.add( m_taskFourQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskFiveQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ){
                    qList.add( m_taskSixQList.get( i ) );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskSixQList.get( i ) );
                }
                else if ( m_coreToUseForProcessingThread == i ){
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS_PART_2 ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ){
                    qList.add( m_taskFunneler );
                }
                else if ( m_coreToUseForTransmissionThread == i ) {
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        
        return null;
    }
    
    protected List<BlockingQueue<Integer>> getMultiCoreLazyProcessFirstSignalDoneQListForSlaveUser(
            TaskType taskType
            ) {
        
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        
        if ( taskType == TaskType.INPUT_TRANS ) {
//            return createTaskFunnelerQListForTransmissionThreads();
            return createNullQList();
        }
        else if ( taskType == TaskType.INPUT_PROC ) {
            return createNullQList();
        }
        else if ( taskType == TaskType.OUTPUT_TRANS ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskTwoQList.get( i ) );
                }
                else if ( m_coreToUseForTransmissionThread == i ){
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_PROC ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskThreeQList.get( i ) );
                }
                else if ( m_coreToUseForProcessingThread == i ){
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        else if ( taskType == TaskType.OUTPUT_TRANS_PART_2 ) {
            for ( int i = 0; i < m_numCores; i++ ) {
                if ( m_sharedProcAndTransThreadCore == i ) {
                    qList.add( m_taskFunneler );
                }
                else {
                    qList.add( null );
                }
            }
            
            return qList;
        }
        return null;
        
    }
	
    protected List<BlockingQueue<Integer>> createTaskFunnelerQList() {
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        for ( int i = 0; i < m_numCores; i++ ) {
            qList.add( m_taskFunneler );
        }
        return qList;
	}

	protected List<BlockingQueue<Integer>> createTaskFunnelerQListForProcessingThreads() {
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        for ( int i = 0; i < m_numCores; i++ ) {
            if ( m_coreToUseForProcessingThread == i ) {
                qList.add( m_taskFunneler );
            }
            else {
                qList.add( null );
            }
        }
        
        return qList;
    }

    protected List<BlockingQueue<Integer>> createTaskFunnelerQListForTransmissionThreads() {
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        for ( int i = 0; i < m_numCores; i++ ) {
            if ( m_coreToUseForTransmissionThread == i ) {
                qList.add( m_taskFunneler );
            }
            else {
                qList.add( null );
            }
        }
        
        return qList;
    }

    protected List<BlockingQueue<Integer>> createNullQList() {
        List<BlockingQueue<Integer>> qList = new LinkedList<BlockingQueue<Integer>>();
        for ( int i = 0; i < m_numCores; i++ ) {
            qList.add( null );
        }
        return qList;
    }
    
}

