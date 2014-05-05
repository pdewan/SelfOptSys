package selfoptsys;

import bus.uigen.ObjectEditor;
import bus.uigen.uiFrame;

import commonutils.config.*;
import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;

import selfoptsys.comm.*;
import selfoptsys.config.*;
import selfoptsys.network.*;
import selfoptsys.overlay.*;
import selfoptsys.perf.*;
import selfoptsys.sched.*;
import selfoptsys.systemapps.recordingmanager.*;
import selfoptsys.ui.*;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ALogger 
	implements Logger, LocalLogger, SchedulingLogger, LocalMessageDest {
    
    protected Loggable m_loggable = null;
    
    protected SessionRegistry m_sessionRegistry;
    
    protected Logger m_loggerRMIStub;
    
    /*
     * Start of: performance optimization instance variables
     */
    protected PerformanceOptimizationClient m_performanceOptimizationClient = null;
    
    protected boolean m_collectPerfDataForUser = false;
	protected boolean m_outputCorrespondsToInput = true;
	
	protected long m_inputProcStartTime = 0;
    protected int m_quantumSize;
    
	protected boolean m_autoReportProcCosts = true;
	protected boolean m_autoReportTransCosts = true;
	protected boolean m_autoReplayCommandsFromReplayLog = true;
	protected boolean m_autoReportEachCommandToTimeServer = true;
	
    protected BlockingQueue<Integer> m_inputEnterToken;
    /*
     * End of: performance optimization instance variables
     */
    
    protected OperationMode m_operationMode;
    
    protected int[] m_userTurns = null;
    
    protected boolean m_measurePerfForUser = false;
    protected boolean m_haveAFakeLoggable = false;
    
    protected CommunicationType m_commType;
    
    protected ConfigParamProcessor m_mainCpp;
    
    protected Scheduler m_scheduler;
    
    protected String m_registryHost = null;
    protected int m_registryPort = 0;

    protected LoggerUI m_loggerUI;
    
    protected boolean m_showAllUIs = true;
    
    protected boolean m_userInputsCommands = true;
    
    protected boolean m_lastReceivedInputMsgWasReplayOrForLatecomer = false;
	
    protected boolean m_replayCommandsToLatecomers = true;
	
    protected boolean m_simulatingNetworkLatencies = false;
	
	protected boolean m_runningUIAsMaster = true;
    
	protected boolean m_waitForUsersToScheduleTasks = false;
	
	protected AtomicLong m_startThinkTime = new AtomicLong( Long.MAX_VALUE );
	
	protected String m_pingerFilePath;
	
	protected SimulatedLatencyManager m_simulatedLatencyManager;
	protected int m_simulatedLatencyIndex;
    
	protected AtomicInteger m_currentSysConfigVersion = new AtomicInteger( -1 );
	protected ALoggerSysConfigInfo m_currentSysConfig;
	protected Map<Integer, ALoggerSysConfigInfo> m_sysConfigs;
	
	protected int m_lazyDelayLocal;
	protected int m_lazyDelayRemote;

	protected int m_coreToUseForProcessingThread;
	protected int m_coreToUseForTransmissionThread;
    
    protected Logger m_userLoggerFromWhoToQueryCommands;
    
    protected ReplayClient m_replayClient;

    public ALogger( 
            Loggable myLoggable,
            String registryHost,
            int registryPort,
            boolean haveAFakeLoggable,
            boolean myUserInputsCommands,
            boolean runningUIAsMaster,
            boolean waitForUserToScheduleTasks
            ) {
    	m_mainCpp = AMainConfigParamProcessor.getInstance();
    	
    	m_registryHost = registryHost;
    	m_registryPort = registryPort;
    	
    	m_runningUIAsMaster = runningUIAsMaster;
    	
    	m_waitForUsersToScheduleTasks = waitForUserToScheduleTasks;
    	
    	ConfigUtils.setProcessorCoreAffinityMask();
    	
    	m_commType = CommunicationType.valueOf( m_mainCpp.getStringParam( Parameters.COMMUNICATION_TYPE ) );
    	if ( m_commType == CommunicationType.TCP_SOCKETS_SERIALIZED_OBJECT ) {
    	    AMessageForwarderFactorySelector.setFactory( new ASerializedObjectTCPMessageForwarderFactory() );
    	    ALoggableMessageForwarderFactorySelector.setFactory( new ASerializedObjectTCPLoggableMessageForwarderFactory() );
    	    ATCPListeningThreadFactorySelector.setFactory( new ASerializedObjectTCPListeningThreadFactory() );
    	}
    	else if ( m_commType == CommunicationType.TCP_SOCKETS_BLOCKING ) {
    	    
    	}
        else if ( m_commType == CommunicationType.TCP_SOCKETS_NON_BLOCKING ) {
            AMessageForwarderFactorySelector.setFactory( new ANonBlockingTCPMessageForwarderFactory() );
            ALoggableMessageForwarderFactorySelector.setFactory( new ANonBlockingTCPLoggableMessageForwarderFactory() );
            ATCPListeningThreadFactorySelector.setFactory( new ANonBlockingTCPListeningThreadFactory() );
        }
        else if ( m_commType == CommunicationType.UDP_SOCKETS_BLOCKING ) {
            
        }
        else if ( m_commType == CommunicationType.RMI ) {
            
        }

        m_loggable = myLoggable;
        
        m_haveAFakeLoggable = haveAFakeLoggable;

        m_measurePerfForUser = false;
        m_collectPerfDataForUser = false;
        if ( m_haveAFakeLoggable ) {
            m_measurePerfForUser = false;
            m_collectPerfDataForUser = false;
        }
        else if ( m_operationMode == OperationMode.RECORD &&
        		m_loggable.getUserIndex() == RecordingManagerStarter.RECORDING_LOGGABLE_USER_INDEX ) {
        	m_measurePerfForUser = false;
        	m_collectPerfDataForUser = false;
        }
        else {
        	int[] measurePerfForUsers = MiscUtils.getSpecifiedUserIndices( m_mainCpp.getStringArrayParam( Parameters.USERS_FOR_WHO_TO_MEASURE_PERFORMANCE ) );
            boolean measurePerfForAllUsers = m_mainCpp.getBooleanParam( Parameters.MEASURE_PERFORMANCE_FOR_ALL_USERS );
            if ( measurePerfForAllUsers ) {
                m_measurePerfForUser = true;
            }
            else if ( measurePerfForUsers != null ) {
    	    	for ( int i = 0; i < measurePerfForUsers.length; i++ ) {
    	    		if ( measurePerfForUsers[i] == m_loggable.getUserIndex() ) {
    	    			m_measurePerfForUser = true;
    	    			break;
    	    		}
    	    	}
        	}
        	
            int[] usersForWhoToCollectPerfData = MiscUtils.getSpecifiedUserIndices( m_mainCpp.getStringArrayParam( Parameters.USERS_FOR_WHO_TO_COLLECT_PERFORMANCE_DATA ) );
            boolean collectPerfDataForAllUsers = m_mainCpp.getBooleanParam( Parameters.COLLECT_PERFORMANCE_DATA_FOR_ALL_USERS );
            if ( collectPerfDataForAllUsers ) {
                m_collectPerfDataForUser = true;
            }
            else if ( usersForWhoToCollectPerfData != null ) {
                for ( int i = 0; i < usersForWhoToCollectPerfData.length; i++ ) {
                    if ( usersForWhoToCollectPerfData[i] == m_loggable.getUserIndex() ) {
                        m_collectPerfDataForUser = true;
                        break;
                    }
                }
            }
        }
        
        /*
         * Prepare for replay if necessary
         */
        m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
        if ( m_operationMode == OperationMode.REPLAY ) {
            m_userTurns = m_mainCpp.getIntArrayParam( Parameters.USER_TURNS );
            ReplayUserInfo replayUserInfo = new AReplayUserInfo(
                    getUserIndex(),
                    m_measurePerfForUser
                    );
            m_replayClient = new AReplayClient(
                    getUserIndex(),
                    replayUserInfo,
                    (Logger) this
                    );
        }
        
        m_userInputsCommands = myUserInputsCommands;
        
        m_showAllUIs = m_mainCpp.getBooleanParam( Parameters.SHOW_ALL_UIS );
        
        if ( m_showAllUIs ) {
        	m_loggerUI = new ALoggerUI( this );
        	uiFrame frame = ObjectEditor.edit( m_loggerUI );

        	frame.addWindowListener( new WindowAdapter() {
    			public void windowClosing( WindowEvent e ){
    				quit();
    			}
    		});
        }
        
        m_simulatingNetworkLatencies = m_mainCpp.getBooleanParam( Parameters.SIMULATING_NETWORK_LATENCIES );
        
//        if ( m_waitForUsersToScheduleTasks ) {
//            AMetaSchedulerFactorySelector.setFactory( new AUserControlledMetaSchedulerFactory() );
//        }
        
        /*
         * take care of scheduling issues, such as number of threads, the core used by each thread, etc.
         */
        {
            m_quantumSize = m_mainCpp.getIntParam( Parameters.SCHEDULING_QUANTUM );
            
            int numCores = winsyslib.WinSysLibUtilities.getNumProcessors();
            
            m_coreToUseForProcessingThread = m_mainCpp.getIntParam( Parameters.CORE_TO_USE_FOR_PROCESSING_TASK_THREAD );
            if ( m_coreToUseForProcessingThread > numCores - 1 ) {
            	m_coreToUseForProcessingThread = 0;
            }

            m_coreToUseForTransmissionThread = m_mainCpp.getIntParam( Parameters.CORE_TO_USE_FOR_TRANSMISSION_TASK_THREAD );
            if ( m_coreToUseForTransmissionThread > numCores -1 ) {
            	m_coreToUseForTransmissionThread = numCores - 1;
            }
            
	        String schedulerFactoryClassName = 
	        	m_mainCpp.getStringParam( Parameters.META_SCHEDULER_FACTORY_CLASS_NAME );
	        if ( schedulerFactoryClassName == null || schedulerFactoryClassName.equals( "" ) ) {
	        	schedulerFactoryClassName = "selfoptsys.sched.ADefaultMetaSchedulerFactory";
	        }
	        
	        try {
		        MetaSchedulerFactory msfs = 
		        	(MetaSchedulerFactory) Class.forName( schedulerFactoryClassName ).newInstance();
		        AMetaSchedulerFactorySelector.setFactory( msfs );
	        }
	        catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "ALogger: Error while selecting factory for meta scheduler",
                        e
                        );
	        }
            
            m_scheduler = AMetaSchedulerFactorySelector.getFactory().createScheduler(
                    (SchedulingLogger) this,
                    m_coreToUseForProcessingThread,
                    m_coreToUseForTransmissionThread
                    );
        }
        
//        if ( m_loggerUI != null && m_waitForUsersToScheduleTasks ) {
//            UserControlledMetaSchedulerUI userControlledSchedulerUI = new AUserControlledMetaSchedulerUI( (UserControlledMetaScheduler) m_scheduler );
//            m_loggerUI.setUserControlledSchedulerUI( userControlledSchedulerUI );
//        }
        
    	( (AScheduler) m_scheduler).setWindowsPriority( WindowsThreadPriority.ABOVE_NORMAL );
    	( (AScheduler) m_scheduler).start();
        
    	m_pingerFilePath = m_mainCpp.getStringParam( Parameters.PINGER_FILE );
    	
    	m_simulatedLatencyManager = new ASimulatedLatencyManager();
    	
        m_sysConfigs = new Hashtable<Integer, ALoggerSysConfigInfo>();
        m_lazyDelayLocal = m_mainCpp.getIntParam( Parameters.LAZY_DELAY );
        m_lazyDelayRemote = m_mainCpp.getIntParam( Parameters.LAZY_DELAY );

        try {
	    	m_loggerRMIStub = 
	        	(Logger) UnicastRemoteObject.exportObject( (Logger) this, 0 );

	        Registry registry = LocateRegistry.getRegistry(
	        		m_registryHost,
	        		m_registryPort
	        		);

	        m_sessionRegistry = (SessionRegistry) registry.lookup( "SessionRegistry" );
	        
	        if ( m_operationMode == OperationMode.REPLAY ) {
	        	m_sessionRegistry.join(
	        			m_loggable.getUserIndex(),
	        			this,
	        			m_currentSysConfigVersion.get() == -1 ? true : false,
	        			m_userInputsCommands,
	        			m_runningUIAsMaster,
	        			false,
	        			m_haveAFakeLoggable
	        			);
	        }
	        
        } catch ( Exception e ) {
        	ErrorHandlingUtils.logSevereExceptionAndContinue(
        	        "ALogger: Constructor",
        	        e
        	        );
        }
     
        m_inputEnterToken = new ArrayBlockingQueue<Integer>( 1 );
        try {
            m_inputEnterToken.put( 0 );
        }
        catch ( Exception e ) {}
        
    }
    
    public void sendInputMsg( CommandMessage msg ) {
        try {
            msg.setSysConfigVersion( m_currentSysConfigVersion.get() );
            
            if ( m_operationMode == OperationMode.REPLAY && 
                    m_autoReportEachCommandToTimeServer ) {
            	recordReplayServerReport(
                        MessageType.TS_INPUT_ENTERED,
                        m_loggable.getUserIndex(),
                        msg.getSourceUserIndex(),
                        msg.getSeqId(),
                        System.nanoTime()
                        );
            }

            /*
             * Performance optimization code
             */
            {
            	/*
            	 * Even if we are not collecting performance stats for the user,
            	 * we need to time at which the input was entered
            	 */
                if ( m_outputCorrespondsToInput &&
                        m_performanceOptimizationClient != null ) {
                    long endThinkTime = System.nanoTime();
                    long thinkTime = endThinkTime - m_startThinkTime.get();
                    
                    /*
                     * TODO: Not sure why this is here. Can we remove it?
                     */
                    if ( thinkTime < 0 ) {
                        thinkTime = 0;
                    }

                    m_performanceOptimizationClient.sendPerformanceReport(
                            MessageType.PERF_INPUT_ENTERED,
                            msg.getSysConfigVersion(),
                            m_loggable.getUserIndex(),
                            msg.getSeqId(),
                            0,
                            0,
                            0,
                            0,
                            -1,
                            MathUtils.round( (double) thinkTime / (double) 1000000000, 4 ),
                            -1,
                            msg.getReportProcCostForMessage(),
                            false
                            );
                }
            }

            /*
             * TODO: Need better solution to stop commands from being input
             */
            m_inputEnterToken.take();
            
            m_scheduler.scheduleMsgDelivery( 
            		new ASchedulableEntity( msg, null, null )
            		);
            
            m_inputEnterToken.put( 0 );
        } 
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while sending input message",
                    e
                    );
        }
    }
    
    public void receiveMessage( Message msg ) {
      m_scheduler.scheduleMsgDelivery( 
		new ASchedulableEntity( (CommandMessage) msg, null, null )
		);
    }
    
    public void sendOutputMsg( CommandMessage msg ) {
        try {
            if ( m_outputCorrespondsToInput ) {
            	msg.isMsgForLatecomerOrNewMaster( m_lastReceivedInputMsgWasReplayOrForLatecomer );
        	}
            else {
                msg.setSysConfigVersion( m_currentSysConfigVersion.get() );
            }
        	
            /*
             * Performance optimization code
             */
            {
	        	if ( m_outputCorrespondsToInput &&
	        	        m_performanceOptimizationClient != null  &&
	                 !msg.isMsgForLatecomerOrNewMaster() ) {
	    			long inputProcEndTime = System.nanoTime();
	    			
	    			m_performanceOptimizationClient.sendPerformanceReport(
	    			        MessageType.PERF_INPUT_PROC_TIME,
	                        msg.getSysConfigVersion(),
	                        msg.getSourceUserIndex(),
	                        msg.getSeqId(),
	                        inputProcEndTime - m_inputProcStartTime,
	                        0,
	                        0,
	                        0,
	                        -1,
	                        0,
	                        ( (ASelfOptArchThread) Thread.currentThread() ).getThreadCoreAffinity(),
	                        msg.getReportProcCostForMessage(),
	                        false
	                        );
	    		}
            }
   		
            m_scheduler.scheduleMsgDelivery( 
            		new ASchedulableEntity( msg, null, null )
            		);
        } 
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while sending output message",
                    e
                    );
        }
    }
    
    public void processInputMsg( CommandMessage msg ) {
    	
    	if ( m_haveAFakeLoggable ) {
    		return;
    	}
    	
    	/*
    	 * This case is never true if scheduling optimization is enabled
    	 */
    	if ( msg.getDestUserIndices() != null &&
    			msg.getDestUserIndices().contains( m_loggable.getUserIndex() ) == false ) {
    		return;
    	}
    	
    	m_lastReceivedInputMsgWasReplayOrForLatecomer = msg.isMsgForLatecomerOrNewMaster();
    	
		if ( m_replayCommandsToLatecomers ) {
		    m_sysConfigs.get( msg.getSysConfigVersion() ).InputCommandHistory.add( msg );
		}

		if ( m_collectPerfDataForUser ) {
			if ( m_performanceOptimizationClient != null ) {
				m_inputProcStartTime = System.nanoTime();
			}
		}

    	m_loggable.deliverInputMsg( msg );

    }
    
    public void processOutputMsg( CommandMessage msg ) {
    	try {
        	
        	if ( m_haveAFakeLoggable ) {
        		return;
        	}

        	/*
        	 * This case is never true if scheduling optimization is enabled
        	 */
        	if ( msg.getDestUserIndices() != null &&
        			msg.getDestUserIndices().contains( m_loggable.getUserIndex() ) == false ) {
        		return;
        	}
        	
        	if ( m_currentSysConfig.IsMaster ) {
    			if ( m_replayCommandsToLatecomers ) {
    	            m_sysConfigs.get( msg.getSysConfigVersion() ).OutputCommandHistory.add( msg );
    			}
    		}
    		
    		long outputProcStartTime = System.nanoTime();
            m_loggable.deliverOutputMsg( msg );
            long outputProcEndTime = System.nanoTime();
            
            m_startThinkTime.set( outputProcEndTime );

            if ( m_measurePerfForUser &&
    				!msg.isMsgForLatecomerOrNewMaster() ) {

	            if ( m_operationMode == OperationMode.REPLAY &&
	                    m_autoReportEachCommandToTimeServer ) {
	                recordReplayServerReport(
                            MessageType.TS_OUTPUT_PROCESSED,
                            m_loggable.getUserIndex(),
                            msg.getSourceUserIndex(),
                            msg.getSeqId(),
                            System.nanoTime()
                            );
	            }
            }

            if ( m_collectPerfDataForUser &&
                    m_performanceOptimizationClient != null  &&
                    !msg.isMsgForLatecomerOrNewMaster() ) {
                m_performanceOptimizationClient.sendPerformanceReport(
                        MessageType.PERF_OUTPUT_PROC_TIME,
                        msg.getSysConfigVersion(),
                        msg.getSourceUserIndex(),
                        msg.getSeqId(),
                        outputProcEndTime - outputProcStartTime,
                        0,
                        0,
                        0,
                        -1,
                        0,
                        ( (ASelfOptArchThread) Thread.currentThread() ).getThreadCoreAffinity(),
                        msg.getReportProcCostForMessage(),
                        false
                        );
            }
            
            if ( m_operationMode == OperationMode.REPLAY &&
                    m_autoReplayCommandsFromReplayLog && 
                    msg.isMsgForLatecomerOrNewMaster() == false ) {
                m_loggable.replayMessageFromReplayLogIfMyTurn();
            }
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while delivering input message",
                    e
                    );
    	}
    }
        
    public void forwardInputMsg( CommandMessage msg ) {

//        int threadCoreAffinity = ( (ASelfOptArchThread) (Thread.currentThread() ) ).getThreadCoreAffinity();
        
    	if ( msg.isMsgForLatecomerOrNewMaster() ) {
    		return;
    	}
    	
    	if ( msg.getSysConfigVersion() == m_currentSysConfigVersion.get() ) {
    		m_currentSysConfig.InputForwarder.sendMsg( msg );
    	}
    	else {
    	    m_sysConfigs.get( msg.getSysConfigVersion() ).InputForwarder.sendMsg( msg );
    	}
    }
    
    public void forwardOutputMsg( CommandMessage msg ) {
    	if ( m_lastReceivedInputMsgWasReplayOrForLatecomer ||
    			msg.isMsgForLatecomerOrNewMaster() ) {
    		return;
    	}
    	
    	if ( msg.getSysConfigVersion() == m_currentSysConfigVersion.get() ) {
    	    m_currentSysConfig.OutputForwarder.sendMsg( msg );
    	}
    	else {
    		m_sysConfigs.get( msg.getSysConfigVersion() ).OutputForwarder.sendMsg( msg );
    	}
    }
    
    public boolean preGetOutputMsgDest() {
    	return false;
    }
    public MessageDest getOutputMsgDest(
    		int sysConfigVersion
    		) {
    	return m_sysConfigs.get( sysConfigVersion ).OutputDestRmiStub;
    }
    
    public boolean preGetInputMsgDest() {
    	return false;
    }
    public MessageDest getInputMsgDest(
    		int sysConfigVersion  
	        ) {
    	return m_sysConfigs.get( sysConfigVersion ).InputDestRmiStub;
    }
    
    public void registerTimeServer(
            TimeServer timeServer
            ) {
        try {
            m_replayClient.registerWithReplayServer( timeServer );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::registerTimeServer(): Error while registering with the time server",
                    e
                    );
        }
    }
    
    public void registerPerformanceOptimizationServer(
    		PerformanceOptimizationServer performanceOptimizationServer
    		) {
        
        PerformanceOptimizationUserInfo performanceOptimizationUserInfo = new APerformanceOptimizationUserInfo(
                getUserIndex(),
                m_loggerRMIStub,
                hasAFakeLoggable(),
                m_autoReportProcCosts,
                m_autoReportTransCosts,
                m_simulatedLatencyIndex,
                m_collectPerfDataForUser,
                m_coreToUseForProcessingThread,
                m_coreToUseForTransmissionThread
                );
        m_performanceOptimizationClient = new APerformanceOptimizationClient(
                getUserIndex(),
                performanceOptimizationUserInfo
                );
        m_performanceOptimizationClient.registerWithPerformanceOptimizationServer(
                performanceOptimizationServer
                );
        
        try {
            if ( m_collectPerfDataForUser ) {
                
                m_currentSysConfig.InputForwarder.setPerformanceOptimizationClient( m_performanceOptimizationClient );
                m_currentSysConfig.OutputForwarder.setPerformanceOptimizationClient( m_performanceOptimizationClient );
                
                m_currentSysConfig.InputDest.setPerformanceOptimizationClient( m_performanceOptimizationClient );
                m_currentSysConfig.OutputDest.setPerformanceOptimizationClient( m_performanceOptimizationClient );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::registerPerformanceServer: Exception while configuring local message receivers and senders",
                    e
                    );
        }
    	
    }
    
//    public void registerPerfReportCollector(
//            PerfReportCollector perfReportCollector
//            ) {
//        try {
//        	
//            MessageDest trcMsgDest = perfReportCollector.getMessageDest();
//            
//            String processorName = "";
//            String processorIdentifier = "";
//            long processorSpeed = 0;
//            if ( m_haveAFakeLoggable == false ) {
//                /*
//                 * When testing a multi-computer scenario locally, we need to fake differences between the IDs of the
//                 * users processors. The reason is that it tests our perf collector functionality better. We accomplish
//                 * this by giving each user's processor a different name.
//                 * 
//                 * processorName = SystemInformationUtilities.getProcessorName() + "__" + m_myLoggable.getUserIndex();
//                 */
//                processorName = WinSysLibUtilities.getProcessorName();
//                processorIdentifier = WinSysLibUtilities.getProcessorIdentifier();
//                processorSpeed = WinSysLibUtilities.getProcessorSpeed();
//                if ( processorSpeed >= 1951 && processorSpeed <= 2049 ) {
//                    processorSpeed = 2000;
//                }
//                else if ( processorSpeed == 498 ) {
//                    processorName = "P3";
//                }
//            }
//            else {
////                processorName = Constants.FAKE_LOGGABLE_PROCESSOR_NAME;
////                processorIdentifier = Constants.FAKE_LOGGABLE_PROCESSOR_IDENTIFIER;
////                processorSpeed = Constants.FAKE_LOGGABLE_PROCESSOR_SPEED;
////                processorName = "              Intel(R) Pentium(R) 4 CPU 1700MHz";
////                processorIdentifier = "x86 Family 15 Model 0 Stepping 10";
////                processorSpeed = 1680;
//                processorName = "Intel(R) Core(TM)2 Duo CPU     E4400  @ 2.00GHz";
//                processorIdentifier = "x86 Family 6 Model 15 Stepping 13";
//                processorSpeed = 2000;
//            }
//            
//            perfReportCollector.registerUser( 
//                    m_loggable.getUserIndex(),
//                    ( NetworkLatencyCollectorAgent ) this,
//                    InetAddress.getLocalHost(),
//                    processorName,
//                    processorIdentifier,
//                    processorSpeed,
//                    m_simulatedLatencyIndex,
//                    m_collectPerfStatsForUser,
//                    m_coreToUseForProcessingThread,
//                    m_coreToUseForTransmissionThread,
//                    m_quantumSize
//                    );
//                    
//            if ( m_collectPerfStatsForUser ) {
//                MessageForwarder forwarder = ( new ASerializedObjectTCPMessageForwarderFactory() ).createMessageForwarder(
//                		m_loggable.getUserIndex()
//                		);
//                forwarder.addDest(
//                        Constants.PERF_COLLECTOR_USER_INDEX,
//                		trcMsgDest
//                		);
//                
//                long prevMsgSendTime = 0;
//                long prevMsgDelayTime = 0;
//                for ( int i = 0; i < 30; i++ ) {
//                    ClockSkewMsg msg = new AClockSkewMsg(
//                            m_loggable.getUserIndex()
//                            );
//                    msg.setSendTimeAtSender( System.nanoTime() );
//                    
//                    if ( i > 0 ) {
//                        msg.setTimeElapsedSinceLastSendOnSender( System.nanoTime() - prevMsgSendTime );
//                        msg.setPrevMsgDelayTime( prevMsgDelayTime );
//                    }
//                    
//                    long startSendTime = System.nanoTime();
//                    forwarder.sendMsg( msg );
//                    prevMsgDelayTime = System.nanoTime() - startSendTime;
//                    
//                    prevMsgSendTime = System.nanoTime();
//                    
//                    Thread.sleep( 10 );
//                }
//
//                m_perfReportCollectorForwarder = new APerfReportCollectorForwarder(
//                        m_loggable.getUserIndex(),
//                        (LocalLogger) this,
//                        m_collectPerfStatsForUser,
//                        m_autoReportProcCosts,
//                        m_autoReportTransCosts,
//                        forwarder
//                        );
//                m_perfReportCollectorForwarder.setWindowsPriority( WindowsThreadPriority.BELOW_NORMAL );
//                m_perfReportCollectorForwarder.start();
//                
//                m_currentSysConfig.InputForwarder.setPerfReportCollectorForwarder( m_perfReportCollectorForwarder );
//                m_currentSysConfig.OutputForwarder.setPerfReportCollectorForwarder( m_perfReportCollectorForwarder );
//                
//                m_currentSysConfig.InputDest.setPerfReportCollectorForwarder( m_perfReportCollectorForwarder );
//                m_currentSysConfig.OutputDest.setPerfReportCollectorForwarder( m_perfReportCollectorForwarder );
//                
//                m_perfReportCollector = perfReportCollector;
//            }
//            
//            m_currentSysConfig.CoreUsedByProcessingThread = m_coreToUseForProcessingThread;
//            m_currentSysConfig.CoreUsedByTransmissionThread = m_coreToUseForTransmissionThread;
//            
//        }
//        catch ( Exception e ) {
//            ErrorHandlingUtils.logSevereExceptionAndContinue(
//                    "ALogger: Error while registering perf report collector",
//                    e
//                    );
//        }
//    }
    
    public void beginExperiment() {
        m_loggable.beginExperiment();
    }
    
    public void prepareToQuit() {
        try {
            
            if ( m_measurePerfForUser ) {
                if ( m_performanceOptimizationClient != null ) {
                    m_performanceOptimizationClient.teardown();
                }
            }
            
            m_loggable.loggerAboutToQuit();
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while trying to quit",
                    e
                    );
        }
    }
    
    public void quit() {
    	try {
    		m_sessionRegistry.leaving( m_loggable.getUserIndex() );
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while trying to quit",
                    e
                    );
    	}
    }
    
	public int getUserIndex() {
		return m_loggable.getUserIndex();
	}
    
    public int getMasterUserIndex() {
    	return m_loggable.getMasterUserIndex();
    }
    
    /*
     * This method is meant to be invoked from the UI generated
     * by ObjectEditor.
     */
    public void joinAsMaster() {
    	if ( m_operationMode == OperationMode.REPLAY ) {
    		System.err.println( "ERROR: ALogger::joinAsMaster()" );
    		System.err.println( "       This method should not be called in replay mode" );
    		return;
    	}
    	
    	try {
			m_sessionRegistry.joinAsMaster( 
    				m_loggable.getUserIndex(),
    				m_loggerRMIStub,
                    m_currentSysConfigVersion.get() == -1 ? true : false,
        			m_userInputsCommands,
        			m_runningUIAsMaster,
                    m_haveAFakeLoggable
    				);
			
			if ( m_loggerUI != null ) {
				m_loggerUI.setMasterUserIndex( m_loggable.getUserIndex() );
				m_loggerUI.setIsJoined( true );
			}
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while joining as a master user",
                    e
                    );
    	}
    }
    
    /*
     * This method is meant to be invoked from the UI generated
     * by ObjectEditor.
     */
    public void joinAsSlave(
    		int masterUserIndex
    		) {
    	if ( m_operationMode == OperationMode.REPLAY ) {
    		System.err.println( "ERROR: ALogger::joinAsSlave()" );
    		System.err.println( "       This method should not be called in replay mode" );
    		return;
    	}
    	
    	try {
    		m_sessionRegistry.joinAsSlave( 
    				m_loggable.getUserIndex(),
    				masterUserIndex,
    				m_loggerRMIStub,
                    m_currentSysConfigVersion.get() == -1 ? true : false,
        			m_userInputsCommands,
                    m_haveAFakeLoggable
    				);
    		
			if ( m_loggerUI != null ) {
				m_loggerUI.setMasterUserIndex( masterUserIndex );
				m_loggerUI.setIsJoined( true );
			}
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while joining as a slave user",
                    e
                    );
    	}
    }
    
    public void setMappings(
    		int masterUserIndex,
			Vector<AnOverlayMapping> inputDestMappings,
			Vector<AnOverlayMapping> inputSourceMappings,
			Vector<AnOverlayMapping> outputDestMappings,
			Vector<AnOverlayMapping> outputSourceMappings,
			int sysConfigVersion
			) {
    	
    	try {
    		if ( inputDestMappings != null ) {
		    	for ( int i = 0; i < inputDestMappings.size(); i++ ) {
		    		
		    		int rootUserIndex = inputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = inputDestMappings.elementAt( i ).getUserLogger();
		    		
		        	remoteLogger.mapAsInputSource(
		        			rootUserIndex,
		        			m_loggable.getUserIndex(),
		        			m_simulatedLatencyIndex,
		        			m_loggerRMIStub,
		        			sysConfigVersion
		        			);
		    		
		    	}
    		}
	    	
	    	if ( inputSourceMappings != null ) {
	    		for ( int i = 0; i < inputSourceMappings.size(); i++ ) {
		    		
		    		int rootUserIndex = inputSourceMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = inputSourceMappings.elementAt( i ).getUserLogger();
		    		
		        	remoteLogger.mapAsInputDest(
		        			rootUserIndex,
		        			m_loggable.getUserIndex(),
                            m_simulatedLatencyIndex,
		        			m_sysConfigs.get( sysConfigVersion ).InputDestRmiStub,
		        			sysConfigVersion
		        			);
		        	
		    	}
	    	}
	    	
	    	if ( outputDestMappings != null ) {
		    	for ( int i = 0; i < outputDestMappings.size(); i++ ) {
		    		
		    		int rootUserIndex = outputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = outputDestMappings.elementAt( i ).getUserLogger();
		        	
		        	remoteLogger.mapAsOutputSource(
		        			rootUserIndex,
		        			m_loggable.getUserIndex(),
                            m_simulatedLatencyIndex,
		        			m_loggerRMIStub,
		        			sysConfigVersion
		        			);
		    		
		    	}
	    	}
	    	
	    	if ( outputSourceMappings != null ) {
		    	for ( int i = 0; i < outputSourceMappings.size(); i++ ) {
		    		
		    		int rootUserIndex = outputSourceMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = outputSourceMappings.elementAt( i ).getUserLogger();
		    		
		        	remoteLogger.mapAsOutputDest(
		        			rootUserIndex,
		        			m_loggable.getUserIndex(),
                            m_simulatedLatencyIndex,
                            m_sysConfigs.get( sysConfigVersion ).OutputDestRmiStub,
                            sysConfigVersion
		        			);
		    		
		    	}
	    	}
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while setting mappings",
                    e
                    );
    	}
    	
    }
    
    public void setUserIndexToLatencyIndexMap(
            Map<Integer, Integer> userIndexToLatencyIndexMap
            ) {
        if ( userIndexToLatencyIndexMap != null ) {
            Iterator<Map.Entry<Integer, Integer>> itr = 
                userIndexToLatencyIndexMap.entrySet().iterator();
            while ( itr.hasNext() ) {
                Map.Entry<Integer, Integer> entry = itr.next();
                m_simulatedLatencyManager.mapLatencyIndexForUserIndex(
                        entry.getKey(),
                        entry.getValue()
                        );
            }
        }
    }
    
    public void unsetMappings(
			Vector<AnOverlayMapping> inputDestMappings,
			Vector<AnOverlayMapping> inputSourceMappings,
			Vector<AnOverlayMapping> outputDestMappings,
			Vector<AnOverlayMapping> outputSourceMappings,
			int sysConfigVersion
			) {
    	
    	try {
    		if ( inputDestMappings != null ) {
	    		for ( int i = 0; i < inputDestMappings.size(); i++ ) {
	    			
	    			int rootUserIndex = inputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = inputDestMappings.elementAt( i ).getUserLogger();
		    		
		    		remoteLogger.unmapAsInputSource( 
		    				rootUserIndex,
		    				m_loggerRMIStub,
		    				sysConfigVersion 
		    				);
	    		}
    		}
    		
    		if ( inputSourceMappings != null ) {
	    		for ( int i = 0; i < inputSourceMappings.size(); i++ ) {
	    			
	    			int rootUserIndex = inputSourceMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = inputSourceMappings.elementAt( i ).getUserLogger();
		    		
		    		remoteLogger.unmapAsInputDest( 
		    				rootUserIndex,
		    				m_loggable.getUserIndex(),
		    				sysConfigVersion 
		    				);
	    		}
    		}
    		
    		if ( outputDestMappings != null ) {
				for ( int i = 0; i < outputDestMappings.size(); i++ ) {
	    			
	    			int rootUserIndex = outputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = outputDestMappings.elementAt( i ).getUserLogger();
		    		remoteLogger.unmapAsOutputSource( 
		    				rootUserIndex,
		    				m_loggerRMIStub,
		    				sysConfigVersion 
		    				);
				}
    		}
    		
    		if ( outputSourceMappings != null ) {
	    		for ( int i = 0; i < outputSourceMappings.size(); i++ ) {
	    			
	    			int rootUserIndex = outputSourceMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = outputSourceMappings.elementAt( i ).getUserLogger();
		    		remoteLogger.unmapAsOutputDest( 
		    				rootUserIndex,
		    				m_loggable.getUserIndex(),
		    				sysConfigVersion
		    				);
	    		}
    		}
    		
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while unsetting mappings",
                    e
                    );
    	}
    	
    }
    
    public void setSpecialDestMappings(
    		Vector<AnOverlayMapping> specialInputDestMappings,
    		Vector<AnOverlayMapping> specialOutputDestMappings,
    		int sysConfigVersion
    		) {
    	
    	try {
    		if ( specialInputDestMappings != null ) {
		    	for ( int i = 0; i < specialInputDestMappings.size(); i++ ) {
		    		
		    		int rootUserIndex = specialInputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = specialInputDestMappings.elementAt( i ).getUserLogger();
		    		
		        	remoteLogger.mapAsInputSource(
		        			rootUserIndex,
		        			m_loggable.getUserIndex(),
                            m_simulatedLatencyIndex,
		        			m_loggerRMIStub,
		        			sysConfigVersion
		        			);
		    		
		    	}
    		}

    		if ( specialOutputDestMappings != null ) {
		    	for ( int i = 0; i < specialOutputDestMappings.size(); i++ ) {
		    		
		    		int rootUserIndex = specialOutputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = specialOutputDestMappings.elementAt( i ).getUserLogger();
		    		
		        	remoteLogger.mapAsOutputSource(
		        			rootUserIndex,
		        			m_loggable.getUserIndex(),
                            m_simulatedLatencyIndex,
		        			m_loggerRMIStub,
		        			sysConfigVersion
		        			);
		    		
		    	}
    		}
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while setting special dest mappings",
                    e
                    );
    	}

    	
    }
    
    public void unsetSpecialDestMappings(
    		Vector<AnOverlayMapping> specialInputDestMappings,
    		Vector<AnOverlayMapping> specialOutputDestMappings,
    		int sysConfigVersion
    		) {
    	
    	try {
    		if ( specialInputDestMappings != null ) {
	    		for ( int i = 0; i < specialInputDestMappings.size(); i++ ) {
	    			
	    			int rootUserIndex = specialInputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = specialInputDestMappings.elementAt( i ).getUserLogger();
		    		
		    		remoteLogger.unmapAsInputSource(
		    				rootUserIndex,
		        			m_loggerRMIStub,
		        			sysConfigVersion
		        			);
	    		}
    		}

    		if ( specialOutputDestMappings != null ) {
	    		for ( int i = 0; i < specialOutputDestMappings.size(); i++ ) {
	    			
	    			int rootUserIndex = specialOutputDestMappings.elementAt( i ).getRootUserIndex();
		    		Logger remoteLogger = specialOutputDestMappings.elementAt( i ).getUserLogger();
		    		
		    		remoteLogger.unmapAsOutputSource(
		    				rootUserIndex,
		        			m_loggerRMIStub,
		        			sysConfigVersion
		        			);
	    		}
    		}
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while unsetting special dest mappings",
                    e
                    );
    	}
    	
    }
    
	public void leaving(
			int userIndex
			) {
		
		if ( userIndex == m_loggable.getUserIndex() ) {
			
			if ( m_loggerUI != null ) {
				m_loggerUI.setMasterUserIndex( -1 );
				m_loggerUI.setIsJoined( false );
			}
			
			m_loggable.quit();
			return;
		}
		
		/*
		 * TODO:
		 * 4/4/2010
		 * Need to fix this
		 */
		
//		if ( m_loggable.getMasterUserIndex() == userIndex ) {
//			becomeMaster();
//		}

	}
	
    public void mapAsInputDest( 
    		int rootUserIndex,
    		int userIndex,
            int simulatedLatencyIndex,
    		MessageDest msgDest,
    		int sysConfigVersion
    		) {
    	
    	try {
    	    m_simulatedLatencyManager.mapLatencyIndexForUserIndex( userIndex, simulatedLatencyIndex );
    		LoggableMessageForwarder inputSender = m_sysConfigs.get( sysConfigVersion ).InputForwarder;
    		inputSender.addDest(
	    			rootUserIndex,
	    			userIndex,
	    			msgDest
	    			);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while mapping input dest",
                    e
                    );
    	}
    	
    }
    
    public void mapAsOutputDest(
    		int rootUserIndex,
    		int userIndex,
            int simulatedLatencyIndex,
    		MessageDest msgDest,
    		int sysConfigVersion
    		) {
    	
    	try {
            m_simulatedLatencyManager.mapLatencyIndexForUserIndex( userIndex, simulatedLatencyIndex );
    		LoggableMessageForwarder outputSender = m_sysConfigs.get( sysConfigVersion ).OutputForwarder;
    		outputSender.addDest(
					rootUserIndex,
					userIndex,
					msgDest
					);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while mapping output dest",
                    e
                    );
    	}
    	
    }
    
    public void unmapAsInputDest( 
    		int rootUserIndex,
    		int userIndex,
    		int sysConfigVersion
    		) {
    	
    	try {
    		LoggableMessageForwarder inputSender = m_sysConfigs.get( sysConfigVersion ).InputForwarder;
    		inputSender.removeDest( 
    				rootUserIndex,
    				userIndex
    				);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while unmapping input dest",
                    e
                    );
    	}

    }
    
    public void unmapAsOutputDest(
    		int rootUserIndex,
    		int userIndex,
    		int sysConfigVersion
    		) {

    	try {
    		LoggableMessageForwarder outputSender = m_sysConfigs.get( sysConfigVersion ).OutputForwarder;
    		outputSender.removeDest(
    				rootUserIndex,
    				userIndex
    				);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while unmapping output dest",
                    e
                    );

    	}

    }
    
    public void mapAsInputSource(
    		int rootUserIndex,
    		int userIndex,
            int simulatedLatencyIndex,
    		Logger remoteLogger,
    		int sysConfigVersion
    		) {
    	
    	try {
            int localSysConfigVersion = sysConfigVersion;
            if ( m_haveAFakeLoggable ) {
                localSysConfigVersion = 0;
            }
            
            m_simulatedLatencyManager.mapLatencyIndexForUserIndex( userIndex, simulatedLatencyIndex );
	    	remoteLogger.mapAsInputDest(
	    			rootUserIndex,
	    			m_loggable.getUserIndex(),
                    m_simulatedLatencyIndex,
	    			m_sysConfigs.get( localSysConfigVersion ).InputDestRmiStub,
					sysConfigVersion
	    			);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while mapping input source",
                    e
                    );
    	}
    	
    }
    
    public void mapAsOutputSource(
    		int rootUserIndex,
    		int userIndex,
            int simulatedLatencyIndex,
    		Logger remoteLogger,
    		int sysConfigVersion
    		) {
    	
    	try {
    	    int localSysConfigVersion = sysConfigVersion;
    	    if ( m_haveAFakeLoggable ) {
    	        localSysConfigVersion = 0;
    	    }
    	    
            m_simulatedLatencyManager.mapLatencyIndexForUserIndex( userIndex, simulatedLatencyIndex );
	    	remoteLogger.mapAsOutputDest(
	    			rootUserIndex,
	    			m_loggable.getUserIndex(),
                    m_simulatedLatencyIndex,
                    m_sysConfigs.get( localSysConfigVersion ).OutputDestRmiStub,
					sysConfigVersion
	    			);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while mapping output source",
                    e
                    );
    	}
    	
    }
    
    public void unmapAsInputSource( 
    		int rootUserIndex,
    		Logger remoteLogger,
    		int sysConfigVersion
    		) {
    	
    	try {
	    	remoteLogger.unmapAsInputDest(
	    			rootUserIndex,
	    			m_loggable.getUserIndex(),
	    			sysConfigVersion
	    			);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while unmapping input source",
                    e
                    );
    	}
    	
    }
    
    public void unmapAsOutputSource(
    		int rootUserIndex,
    		Logger remoteLogger,
    		int sysConfigVersion
    		) {
    	
    	try {
	    	remoteLogger.unmapAsOutputDest( 
	    			rootUserIndex,
	    			m_loggable.getUserIndex(),
	    			sysConfigVersion
	    			);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while unmapping output source",
                    e
                    );
    	}
    	
    }
    
	public void requestingAllInputCommandsTransmittedSoFar(
    		int userIndex,
    		MessageDest msgDest,
            int sysConfigVersionTag
    		) {
    	
    	if ( m_currentSysConfig.InputCommandHistory.size() == 0 ) {
    		return;
    	}
    	
    	CommandMessage[] msgs = new CommandMessage[ m_currentSysConfig.InputCommandHistory.size() ];
    	m_currentSysConfig.InputCommandHistory.copyInto(msgs);
    	
    	for ( int i = 0; i < msgs.length; i++ ) {
    		msgs[i].isMsgForLatecomerOrNewMaster( true );
    		msgs[i].setSysConfigVersion( sysConfigVersionTag );
    		msgs[i].setSenderUserIndex( m_loggable.getUserIndex() );
    	}
    	
    	ALoggableLatecomerForwarder forwarderThread = new ALoggableLatecomerForwarder(
    			m_loggable.getUserIndex(),
    			userIndex,
    			msgDest,
    			msgs,
    			this
    			);
    	forwarderThread.start();
    	
    }
    
    public void requestingAllOutputCommandsTransmittedSoFar(
    		int userIndex,
    		MessageDest msgDest,
            int sysConfigVersionTag
    		) {
    	
    	if ( m_currentSysConfig.OutputCommandHistory.size() == 0 ) {
    		return;
    	}
    	
    	CommandMessage[] msgs = new CommandMessage[ m_currentSysConfig.OutputCommandHistory.size() ];
    	m_currentSysConfig.OutputCommandHistory.copyInto(msgs);
    	
    	for ( int i = 0; i < msgs.length; i++ ) {
    		msgs[i].isMsgForLatecomerOrNewMaster( true );
    		msgs[i].setSysConfigVersion( sysConfigVersionTag );
            msgs[i].setSenderUserIndex( m_loggable.getUserIndex() );
    	}
    	
    	ALoggableLatecomerForwarder forwarderThread = new ALoggableLatecomerForwarder(
    			m_loggable.getUserIndex(),
    			userIndex,
    			msgDest,
    			msgs,
    			this
    			);
    	forwarderThread.start();
    	
    }
    
    public boolean hasAFakeLoggable() {
    	return m_haveAFakeLoggable;
    }
    
    public  void setSchedulingPolicy( 
			SchedulingPolicy schedulingPolicy,
			int sysConfigVersion
			) {
        ALoggerSysConfigInfo sysConfig = m_sysConfigs.get( sysConfigVersion);
        sysConfig.SchedulingPolicy = schedulingPolicy;
    }
    
    public void setCommunicationArchitectureType(
    		CommunicationArchitectureType commArchType
    		) {
    	m_currentSysConfig.InputForwarder.setCommunicationArchitectureType( commArchType );
    	m_currentSysConfig.OutputForwarder.setCommunicationArchitectureType( commArchType );
    }
    
    public boolean getUserInputsCommands() {
    	return m_userInputsCommands;
    }
    public void setUserInputsCommands(
    		boolean inputsCommands
    		) {
    	try {
	    	m_userInputsCommands = inputsCommands;
	    	m_loggable.setUserInputsCommands( inputsCommands );
	    	m_sessionRegistry.updateUserInputsCommands(
	    			m_loggable.getUserIndex(),
	    			inputsCommands
	    			);
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while setting whether or not user inputs commands",
                    e
                    );
    	}
    }
        
    public void setReplayCommandsToLatecomers(
    		boolean replayCommandsToLatecomers
    		) {
    	m_replayCommandsToLatecomers = replayCommandsToLatecomers;
    }
    
    public void setOutputCorrespondsToInput(
    		boolean outputCorrespondsToInput
    		) {
    	m_outputCorrespondsToInput = outputCorrespondsToInput;
    }
    public boolean getOutputCorrespondsToInput() {
        return m_outputCorrespondsToInput;
    }
    
    public void setAutoReportProcCosts(
    		boolean autoReportProcTimes
    		) {
    	m_autoReportProcCosts = autoReportProcTimes;
    }
    
    public void setAutoReportEachCommandToTimeServer(
            boolean autoReportEachCommandToTimeServer
            ) {
        m_autoReportEachCommandToTimeServer = autoReportEachCommandToTimeServer;
    }
    
    public void setAutoReportTransCosts(
            boolean reportTransCosts
            ) {
        m_autoReportTransCosts = reportTransCosts;
    }

    public void setAutoReplayCommandsFromReplayLog(
    		boolean autoReplayCommandsFromReplayLog
    		) {
    	m_autoReplayCommandsFromReplayLog = autoReplayCommandsFromReplayLog;
    }
    
    public void sendPerfReportCollectorReportMessage(
            Message perfReportCollectorReportMessage
            ) {
        /*
         * TODO:
         * Need to implement this when we get Second Life going with the latest selfOptArch
         */
    }
    
    public void setSimulatedLatencies(
            double[][] simulatedLatencies
            ) {
        /*
         * Fake loggables never input so they don't need to know latencies.
         * This also saves memory and allows us to run many fake loggables
         * on a single machine.
         */
        if ( m_haveAFakeLoggable ) {
            return;
        }
        
        if( m_simulatedLatencyManager.getLatencies() != null ) {
            return;
        }
        
        m_simulatedLatencyManager.setLatencies( simulatedLatencies );
    }
    
    public void setSimulatedLatencyIndex(
            int simulatedLatencyIndex
            ) {
        m_simulatedLatencyIndex = simulatedLatencyIndex;
        m_simulatedLatencyManager.mapLatencyIndexForUserIndex( m_loggable.getUserIndex(), m_simulatedLatencyIndex );
    }
    
//    public List<Integer> measureNetworkLatenciesToDestinations(
//            List<InetAddress> destinations
//            ) {
//        
//        List<Integer> latencies = new LinkedList<Integer>();
//        
//        if ( m_haveAFakeLoggable ) {
//            for ( int i = 0; i < destinations.size(); i++ ) {
//                latencies.add( 0 );
//            }
//            return latencies;
//        }
//        
//        final BlockingQueue<String> pingBB = new ArrayBlockingQueue<String>( destinations.size() );
//        int[] arrayLatencies = new int[ destinations.size() ];
//        
//        try {
//            for ( int i = 0; i < destinations.size(); i++ ) {
//                
//                Thread t = new APingerThread(
//                        i,
//                        destinations.get( i ),
//                        m_pingerFilePath,
//                        pingBB
//                        );
//                t.start();
//            }
//            
//            for ( int i = 0; i < destinations.size(); i++ ) {
//                String pingAnswer = pingBB.take();
//                int id = Integer.parseInt( pingAnswer.split( " " )[0] );
//                int latency = Integer.parseInt( pingAnswer.split( " " )[1] );
//                arrayLatencies[ id ] = latency;
//            }
//        }
//        catch ( Exception e ) {
//            ErrorHandlingUtils.logSevereExceptionAndContinue(
//                    "ALogger: Error while measuring network latencies",
//                    e
//                    );
//        }
//        
//        for ( int i = 0; i < destinations.size(); i++ ) {
//            latencies.add( arrayLatencies[ i ] );
//        }
//        
//        return latencies;
//    }
    
    public void joinByCommandFromTimeServer() {
        try {
            m_sessionRegistry.join(
                    m_loggable.getUserIndex(),
                    this,
                    m_currentSysConfigVersion.get() == -1 ? true : false,
                    m_userInputsCommands,
                    m_runningUIAsMaster,
                    true,
                    m_haveAFakeLoggable
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while joining by command from time server",
                    e
                    );
        }
    }
    
    public synchronized void prepareForNewSysConfigVersion(
            int sysConfigVersion,
            int myMasterUserIndex,
            Logger userLoggerFromWhoToQueryCommands
            ) {
        
        m_userLoggerFromWhoToQueryCommands = userLoggerFromWhoToQueryCommands;
        
        ALoggerSysConfigInfo sysConfig = ALoggerSysConfigInfo.copy( m_currentSysConfig );
        sysConfig.SysConfigVersion = sysConfigVersion;
        
        sysConfig.IsMaster = m_loggable.getUserIndex() == myMasterUserIndex ? true : false;
        sysConfig.MasterUserIndex = myMasterUserIndex;
        
        if ( m_currentSysConfig == null ||
                ( m_currentSysConfig.MasterUserIndex == m_loggable.getUserIndex() && 
                myMasterUserIndex != m_loggable.getUserIndex() ) ||
                ( m_currentSysConfig.MasterUserIndex != m_loggable.getUserIndex() && 
                        myMasterUserIndex == m_loggable.getUserIndex() ) ) {
            sysConfig.InputCommandHistory = new Vector<CommandMessage>();
            sysConfig.OutputCommandHistory = new Vector<CommandMessage>();
        }
        
        LoggableMessageForwarder inputSender = null;
        LoggableMessageForwarder outputSender = null;
        MessageDest inputMsgDest = null;
        MessageDest outputMsgDest = null;
        
        inputSender = ALoggableMessageForwarderFactorySelector.getFactory().createMessageForwarder( 
                m_loggable.getUserIndex(),
                false,
                m_autoReportTransCosts,
                this
                );
        inputMsgDest = new ATCPLoggableMessageDest(
                m_loggable.getUserIndex(),
                (LocalMessageDest) this,
                m_simulatingNetworkLatencies,
                WindowsThreadPriority.ABOVE_NORMAL,
                m_simulatedLatencyManager,
                ATCPListeningThreadFactorySelector.getFactory(),
                m_haveAFakeLoggable
                );
        outputSender = ALoggableMessageForwarderFactorySelector.getFactory().createMessageForwarder( 
                m_loggable.getUserIndex(),
                false,
                m_autoReportTransCosts,
                this
                );
        outputMsgDest = new ATCPLoggableMessageDest(
                m_loggable.getUserIndex(),
                (LocalMessageDest) this,
                m_simulatingNetworkLatencies,
                WindowsThreadPriority.ABOVE_NORMAL,
                m_simulatedLatencyManager,
                ATCPListeningThreadFactorySelector.getFactory(),
                m_haveAFakeLoggable
                );
        
        sysConfig.InputForwarder = inputSender;
        sysConfig.OutputForwarder = outputSender;
        sysConfig.InputDest = inputMsgDest;
        sysConfig.OutputDest = outputMsgDest;
        
        inputSender.setPerformanceOptimizationClient( m_performanceOptimizationClient );
        outputSender.setPerformanceOptimizationClient( m_performanceOptimizationClient );
        
        try {
            inputMsgDest.setPerformanceOptimizationClient( m_performanceOptimizationClient );
            outputMsgDest.setPerformanceOptimizationClient( m_performanceOptimizationClient );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "PrepareForNewSysConfigVersion: Error when registering perfReportCollectorForwarder with input and output message dests",
                    e
                    );
        }
        
        inputSender.setCommunicationArchitectureType( CommunicationArchitectureType.UNICAST );
        outputSender.setCommunicationArchitectureType( CommunicationArchitectureType.UNICAST );
        
        ( (AMessageDest) inputMsgDest ).start();
        ( (AMessageDest) outputMsgDest ).start();
        
        try {
            sysConfig.InputDestRmiStub = inputMsgDest.getRmiStub();
            sysConfig.OutputDestRmiStub = outputMsgDest.getRmiStub();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while creating input and output dest rmi stubs",
                    e
                    );
        }
        
        m_sysConfigs.put(
                sysConfig.SysConfigVersion,
                sysConfig
                );

    }
    
    public synchronized void switchToSysConfigVersion(
            int sysConfigVersion
            ) {
        

        try {
            int prevSysConfigVerison = m_currentSysConfigVersion.get();
            
            ALoggerSysConfigInfo newSysConfigInfo = m_sysConfigs.get( sysConfigVersion );
            if ( newSysConfigInfo == null ) {
                return;
            }
            
            if ( m_currentSysConfig == null ) {
                m_currentSysConfigVersion.set( sysConfigVersion );
                m_currentSysConfig = m_sysConfigs.get( sysConfigVersion );
                
                m_loggable.resetLoggableForArchChange( 
                        newSysConfigInfo.MasterUserIndex 
                        );
        
                if ( newSysConfigInfo.MasterUserIndex == m_loggable.getUserIndex() ) {
                    if ( m_userLoggerFromWhoToQueryCommands != null ) {
                        m_userLoggerFromWhoToQueryCommands.requestingAllInputCommandsTransmittedSoFar(
                                m_loggable.getUserIndex(),
                                newSysConfigInfo.InputDestRmiStub,
                                sysConfigVersion
                                );
                    }
                }
                else {
                    if ( m_userLoggerFromWhoToQueryCommands != null ) {
                        m_userLoggerFromWhoToQueryCommands.requestingAllOutputCommandsTransmittedSoFar(
                                m_loggable.getUserIndex(),
                                newSysConfigInfo.OutputDestRmiStub,
                                sysConfigVersion
                                );
                    }
                }
                
                return;
            }
            
            if ( ( m_currentSysConfig.MasterUserIndex != m_loggable.getUserIndex() &&
                    newSysConfigInfo.MasterUserIndex == m_loggable.getUserIndex() ) || 
                    ( m_currentSysConfig.MasterUserIndex == m_loggable.getUserIndex() &&
                            newSysConfigInfo.MasterUserIndex != m_loggable.getUserIndex() ) ) {
                m_loggable.resetLoggableForArchChange( 
                        newSysConfigInfo.MasterUserIndex 
                        );
            }
                
            if ( m_currentSysConfig.MasterUserIndex != newSysConfigInfo.MasterUserIndex ) {
                if ( newSysConfigInfo.MasterUserIndex == m_loggable.getUserIndex() ) {
                    m_scheduler.resetScheduler();
                    m_userLoggerFromWhoToQueryCommands.requestingAllInputCommandsTransmittedSoFar(
                            m_loggable.getUserIndex(),
                            m_sysConfigs.get( sysConfigVersion ).InputDestRmiStub,
                            sysConfigVersion
                            );
                }
                else {
                    if ( prevSysConfigVerison == -1 ) {
                        m_userLoggerFromWhoToQueryCommands.requestingAllOutputCommandsTransmittedSoFar(
                                m_loggable.getUserIndex(),
                                m_sysConfigs.get( sysConfigVersion ).OutputDestRmiStub,
                                sysConfigVersion
                                );
                    }
                }
            }
            
            m_currentSysConfigVersion.set( sysConfigVersion );
            m_currentSysConfig = m_sysConfigs.get( sysConfigVersion );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger: Error while becoming slave or master",
                    e
                    );
        }
    }
    
    public ALoggerSysConfigInfo getLoggerSysConfig(
            int sysConfigVersion
            ) {
        return m_sysConfigs.get( sysConfigVersion );
    }
    
    public ALoggableUserInfo getLoggerUserInfo() {
        return m_loggable.getLoggableUserInfo();
    }
    
    public SchedulingPolicy getSchedulingPolicyForSysConfigVersion(
            int sysConfigVersion
            ) {
        ALoggerSysConfigInfo sysConfigInfo = m_sysConfigs.get( sysConfigVersion );
        return sysConfigInfo.SchedulingPolicy;
    }
    
    public int getLazyLocalDelay() {
        return m_lazyDelayLocal;
    }
    
    public int getLazyRemoteDelay() {
        return m_lazyDelayRemote;
    }
    
    public double getObservedTransCost(
            PerformanceParameterType costType,
            int sysConfigVersion
            ) {
        /*
         * This is a hack. Technically, we can't start with LAZY policy. We have
         * to dynamically switch to it because we need observed trans costs. But
         * just in case an experiment needs it, we return 0 until we get the 
         * actual cost values.
         */
        if ( m_sysConfigs.get( sysConfigVersion ).ObservedTransCosts == null ) {
            return 0;
        }
        return m_sysConfigs.get( sysConfigVersion ).ObservedTransCosts.get( costType );
    }
    public void setObservedTransCost(
            HashMap<PerformanceParameterType, Double> observedTransCosts,
            int sysConfigVersion
            ) {
        m_sysConfigs.get( sysConfigVersion ).ObservedTransCosts = observedTransCosts;
    }
    
    public void takeEnterInputToken() {
        try {
            m_inputEnterToken.take();
        }
        catch ( Exception e ) {}
    }
    public void releaseEnterInputToken() {
        try {
            m_inputEnterToken.put( 0 );
        }
        catch ( Exception e ) {}
    }
    
    public Vector<Integer> getTransDests(
    		int sourceUserIndex,
			MessageType msgType,
			int sysConfigVersion
			) {
    	Vector<Integer> transDests = null;
    	
    	if ( msgType == MessageType.INPUT ) {
    		transDests = ( (LoggableMessageForwarder) m_sysConfigs.get( sysConfigVersion ).InputForwarder ).getDestsForSourceUserIndex( sourceUserIndex );
    	}
    	else if ( msgType == MessageType.OUTPUT ) {
    		transDests = ( (LoggableMessageForwarder) m_sysConfigs.get( sysConfigVersion ).OutputForwarder ).getDestsForSourceUserIndex( sourceUserIndex );
    	}
    	
    	return transDests;
    }
    
    public void reportReadySecondTimeToReplayServer() {
        try {
            m_replayClient.reportReadySecondTimeToReplayServer();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::reportReadySecondTimeToTimeServer(): error while reporting ready for the second time to replay server",
                    e
                    );
        }
    }
    
    public void reportDoneToReplayServer() {
        try {
            m_replayClient.reportDoneToReplayServer();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::reportDoneToTimeServer(): error while reporting as done to replay server",
                    e
                    );
        }
    }
    
    public void recordReplayServerReport(
            MessageType messageType,
            int userIndex,
            int sourceUserIndex,
            int seqId,
            long time
            ) {
        try {
            m_replayClient.recordReplayServerReport(
                    messageType,
                    userIndex,
                    sourceUserIndex,
                    seqId,
                    time
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::reportDoneToTimeServer(): error while reporting as done to replay server",
                    e
                    );
        }
    }
    
}

class APingerThread 
    extends ASelfOptArchThread {
    
    protected int m_id;
    protected InetAddress m_address;
    protected String m_pingerFilePath;
    protected BlockingQueue<String> m_signalFinishedBB;
    
    public APingerThread(
            int id,
            InetAddress address,
            String pingerFilePath,
            BlockingQueue<String> signalFinishedBB
            ) {
        m_id = id;
        m_address = address;
        m_pingerFilePath = pingerFilePath;
        m_signalFinishedBB = signalFinishedBB;
    }
    
    public void run() {
        try {
            int latency = 0;
            String ipAddress = m_address.getHostAddress();
            
            String fileName = UUID.randomUUID().toString();
            File outFile = new File( fileName );
            Process p = Runtime.getRuntime().exec( 
                    "cmd.exe /C " + m_pingerFilePath + " " + ipAddress + " " + fileName
                    );
            p.waitFor();

            InputStream is = new FileInputStream( outFile );
            BufferedReader bin = new BufferedReader( new InputStreamReader( is ) );
            while ( true ) {
                String line = bin.readLine();
                if ( line.contains( "Destination host unreachable" ) ) {
                    latency = Integer.MAX_VALUE;
                    break;
                }
                else if ( line.contains( "Minimum" ) &&
                       line.contains( "Maximum" ) &&
                       line.contains( "Average" ) ) {
                    String[] vals = line.split( " " );
                    String latencyStr = vals[ vals.length - 1 ];
                    latencyStr = latencyStr.substring( 0, latencyStr.length() - 2 );
                    latency = Integer.parseInt( latencyStr );
                    break;
                }
            }
            
            is.close();
            outFile.delete();
            
            m_signalFinishedBB.put( m_id + " " + latency );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APingerThread: Error while measuring input latencies",
                    e
                    );
        }
    }
    
}
    
