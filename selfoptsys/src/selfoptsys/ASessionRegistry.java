package selfoptsys;

import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

import selfoptsys.comm.*;
import selfoptsys.config.*;
import selfoptsys.network.*;
import selfoptsys.overlay.*;
import selfoptsys.perf.*;
import selfoptsys.sched.*;
import selfoptsys.systemapps.recordingmanager.*;
import selfoptsys.ui.*;
import commonutils.basic.*;
import commonutils.basic2.*;
import commonutils.config.*;


public class ASessionRegistry 
	implements SessionRegistry, LocalSessionRegistry {
    
	private SessionRegistryUI m_sessionRegistryUI;
    private String m_registryHost;
    private int m_registryPort;
    
    private OperationMode m_operationMode;
    
    protected TimeServer m_timeServer;
    
    private PerformanceOptimizationServer m_performanceOptimizationServer;
    private PerformanceOptimizationServer m_performanceOptimizationServerRmiStub;
    
    private ConfigParamProcessor m_mainCpp;
    
    private SchedulingPolicy m_schedulingPolicy;
    
	private Random m_rand = new Random();
	
	private boolean m_systemQuitting = false;
	
	private boolean m_replayCommandsToLatecomers = true;
	
	private boolean m_outputCorrespondsToInput = true;
	private boolean m_autoReportProcCosts = true;
    private boolean m_autoReportEachCommandToTimeServer = true;
	private boolean m_autoReportTransCosts = true;
	
	protected boolean m_runPerfOptimizer = false;
	protected boolean m_collectPerformanceData = false;
	
	protected int m_currentSysConfigVersion = 0;
	protected ASessionRegistrySysConfigInfo m_currentSysConfig;
	protected Map<Integer, ASessionRegistrySysConfigInfo> m_sysConfigs;
	
    protected boolean m_simulatingLatencies;
    protected SimulatedLatencyManager m_simulatedLatencyManager;
    protected boolean m_useNetworkLatencyMatrixAsIs = true;
    
    protected boolean m_startOfExperimentSignalled = false;
    
    protected List<TimeServerReportMessage> m_sysConfigChangeReports;
    
    public ASessionRegistry(
    		String registryHost,
    		int registryPort
    		) {
        m_mainCpp = AMainConfigParamProcessor.getInstance();
    	
    	m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
    	m_registryHost = registryHost;
        m_registryPort = registryPort;
        m_replayCommandsToLatecomers = m_mainCpp.getBooleanParam( Parameters.REPLAY_COMMANDS_TO_LATECOMERS );

        ConfigUtils.setProcessorCoreAffinityMask();
        
        if ( m_registryHost == null || m_registryHost.equals( "" ) ) {
	        try {
	        	m_registryHost = InetAddress.getLocalHost().toString();
	        }
	        catch ( Exception e ) {
	            ErrorHandlingUtils.logSevereExceptionAndContinue(
	                    "ASessionRegsitry: Error while getting local host IP address",
	                    e
	                    );
	        }
        }
        
        m_schedulingPolicy = SchedulingPolicy.valueOf( m_mainCpp.getStringParam( Parameters.SCHEDULING_POLICY ) );
        
        m_sysConfigs = new Hashtable<Integer, ASessionRegistrySysConfigInfo>();
        m_currentSysConfig = new ASessionRegistrySysConfigInfo();
        m_sysConfigs.put(
                0,
                m_currentSysConfig
                );
        
        m_outputCorrespondsToInput = m_mainCpp.getBooleanParam( Parameters.INPUTS_AND_OUTPUTS_HAVE_A_ONE_TO_ONE_MAPPING );

        m_autoReportEachCommandToTimeServer = m_mainCpp.getBooleanParam( Parameters.REPORT_EACH_COMMAND_TO_TIME_SERVER );
        m_autoReportProcCosts = m_mainCpp.getBooleanParam( Parameters.REPORT_PROC_COST_FOR_EACH_COMMAND );
        m_autoReportTransCosts = m_mainCpp.getBooleanParam( Parameters.REPORT_TRANS_COST_FOR_EACH_COMMAND );
        
        /*
         * TODO: Why does the session registry need to know about the scheduler?
         */
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
        
        if ( m_operationMode == OperationMode.RECORD ) {
	        String recordingManagerStarterFactoryClassName = 
	        	m_mainCpp.getStringParam( Parameters.RECORDING_MANAGER_FACTORY_CLASS_NAME );
	        if ( recordingManagerStarterFactoryClassName == null || recordingManagerStarterFactoryClassName.equals( "" ) ) {
	        	recordingManagerStarterFactoryClassName = "selfoptsys.systemapps.recordingmanager.ARecordingManagerStarterFactory";
	        }
	        
	        try {
		        RecordingManagerStarterFactory rmsf = 
		        	(RecordingManagerStarterFactory) Class.forName( recordingManagerStarterFactoryClassName ).newInstance();
		        ARecordingManagerStarterFactorySelector.setRecordingManagerStarterFactory( rmsf );
	        }
	        catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "ASessionRegsitry: Error while selecting factory for recording manager",
                        e
                        );
	        }
        }
        
        m_simulatingLatencies = m_mainCpp.getBooleanParam( Parameters.SIMULATING_NETWORK_LATENCIES );
        if ( m_simulatingLatencies ) {
            m_simulatedLatencyManager = new ASimulatedLatencyManager();
            String nwLatencyMatrixFile = m_mainCpp.getStringParam( Parameters.NETWORK_LATENCY_MATRIX_FILE );
            m_simulatedLatencyManager.loadLatenciesFromFile( nwLatencyMatrixFile );
            m_useNetworkLatencyMatrixAsIs = m_mainCpp.getBooleanParam( Parameters.USE_NETWORK_LATENCY_MATRIX_AS_IS );
        }
        
        /*
         * Performance optimization setup
         */
        m_runPerfOptimizer = m_mainCpp.getBooleanParam( Parameters.OPTIMIZE_PERFORMANCE );
        
        boolean collectPerfDataForAllUsers = m_mainCpp.getBooleanParam( Parameters.COLLECT_PERFORMANCE_DATA_FOR_ALL_USERS );
        int[] usersForWhoToCollectPerfData = MiscUtils.getSpecifiedUserIndices(
                m_mainCpp.getStringArrayParam( Parameters.USERS_FOR_WHO_TO_COLLECT_PERFORMANCE_DATA )
                );
        if ( collectPerfDataForAllUsers || 
                ( usersForWhoToCollectPerfData != null && usersForWhoToCollectPerfData.length > 0 ) ) {
            m_collectPerformanceData = true;
        }
        
        m_sysConfigChangeReports = new LinkedList<TimeServerReportMessage>();
        
    }
    
    public void startRegistry() {
        try {
            System.err.println( "SessionRegistry: STARTING ..." );

            Registry registry = LocateRegistry.getRegistry( m_registryHost, m_registryPort );
            try {
            	registry.lookup( "Dummy" );
            }
            catch ( NotBoundException e ) {}
            catch ( Exception e ) {
                /*
                 * NotBoundException indicates that the registry exists, so we should
                 * not attempt to recreate it
                 */
            	registry = LocateRegistry.createRegistry( m_registryPort );
            }
            
            SessionRegistry stub = (SessionRegistry) UnicastRemoteObject.exportObject( (SessionRegistry) this, 0 );
            registry.rebind( "SessionRegistry", stub );
            System.err.println( "SessionRegistry: STARTED!" );
            
            if ( m_collectPerformanceData || m_runPerfOptimizer ) {
                try {
                    String sysConfigOptimizerFactoryClassName = 
                        m_mainCpp.getStringParam( Parameters.SYS_CONFIG_OPTIMIZER_FACTORY_CLASS_NAME );
                    if ( sysConfigOptimizerFactoryClassName == null || sysConfigOptimizerFactoryClassName.equals( "" ) ) {
                        sysConfigOptimizerFactoryClassName = "selfoptsys.perf.ASysConfigOptimizerFactory";
                    }
                    
                    try {
                        SysConfigOptimizerFactory factory = 
                            (SysConfigOptimizerFactory) Class.forName( sysConfigOptimizerFactoryClassName ).newInstance();
                        ASysConfigOptimizerFactorySelector.setFactory( factory );
                    }
                    catch ( Exception e ) {
                        ErrorHandlingUtils.logSevereExceptionAndContinue(
                                "ASessionRegsitry: Error while selecting perf report collector factory",
                                e
                                );
                    }
                    
                    m_performanceOptimizationServer = new APerformanceOptimizationServer(
                            m_registryHost,
                            m_registryPort
                            ); 
                    if ( m_simulatingLatencies ) {
                        m_performanceOptimizationServer.setSimulatedLatencies( m_simulatedLatencyManager.getLatencies() );
                    }
                    m_performanceOptimizationServer.init();
                }
                catch ( Exception e ) {
                    ErrorHandlingUtils.logSevereExceptionAndContinue(
                            "ASessionRegsitry: Error creating a perf report collector",
                            e
                            );
                }
            }


        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while starting registry",
                    e
                    );
        }
    }
    
    public void startRecordingManager() {
    	RecordingManagerStarter recordingManagerStarter =
    		ARecordingManagerStarterFactorySelector.getSchedulerFactory().createScheduler(
    				m_registryHost,
    				m_registryPort
    				);
    	new Thread( recordingManagerStarter ).start();
    }
    
    public void registerTimeServer(
            TimeServer timeServer
            ) {
        m_timeServer = timeServer;
    }    
        
	/*
	 * --------------- IMPORTANT ---------------
	 * 
	 * The "synchronized" keyword is extremely important here. During an experiment,
	 * we register many loggers concurrently. This method becomes a funnel. By going
	 * through this method, we never update more than one logger at the same time.
	 * Moreover, we have no concurrency issues with the structures in the time server
	 * and the session registry during the initial registration period.
	 * 
	 * Thus, this method is important to ensuring thread-safe code.
	 */
    public synchronized void join(
			int userIndex,
			Logger remoteLogger,
			boolean firstTimeJoin,
			boolean inputsCommands,
			boolean runningUIAsMaster,
			boolean joiningByCommandFromTimeServer,
            boolean fakeUser
			) {
		
		if ( m_operationMode != OperationMode.REPLAY ) {
			System.err.println( "ERROR: ASessionRegistry::join()" );
			System.err.println( "       This method should only be called when in replay mode" );
			return;
		}
		
		if ( m_operationMode == OperationMode.REPLAY &&
		        firstTimeJoin ) {
		    m_sysConfigChangeReports.add(
		            new ATimeServerReportMessage(
		                    MessageType.TS_CONFIGURATION_CHANGE_START,
		                    -1,
		                    -1,
		                    -1,
		                    System.nanoTime()
		                    )
		            );
		}
		
		int timeAtWhichUserIsJoining = MiscUtils.getTimeAtWhichUserIsJoining(
		        m_mainCpp.getIntArrayParam( Parameters.TIMES_AT_WHICH_USERS_JOIN ),
		        userIndex
		        );
		
		try {
		    if ( timeAtWhichUserIsJoining == 0 || 
		            joiningByCommandFromTimeServer ) {
		        
		        int masterIndex = -1;
		        if ( timeAtWhichUserIsJoining == 0 ) {
    		        masterIndex = MiscUtils.getMasterIndexWhenUserIsJoining(
    		                ProcessingArchitectureType.valueOf( m_mainCpp.getStringParam( Parameters.PROCESSING_ARCHITECTURE ) ),
    		                m_mainCpp.getIntArrayParam( Parameters.MASTER_USERS ),
    		                userIndex
    		                );
		        }
		        else {
		            ProcessingArchitectureType curProcArch = getProcessingArchitecture();
		            if ( curProcArch == ProcessingArchitectureType.REPLICATED ) {
		                masterIndex = userIndex;
		            }
		            else {
		                masterIndex = m_currentSysConfig.MasterUserIndices.get( 0 );
		            }
		        }
		        
    			if ( userIndex == masterIndex ) {
    				joinAsMaster( 
    						userIndex, 
    						remoteLogger,
    						firstTimeJoin,
    						inputsCommands,
    						runningUIAsMaster,
    						fakeUser
    						);
    			}
    			else {
    				joinAsSlave(
    						userIndex,
    						masterIndex,
    						remoteLogger,
    						firstTimeJoin,
    						inputsCommands,
    						fakeUser
    						);
    			}
		    }
		
		    if ( !joiningByCommandFromTimeServer ) {
		        remoteLogger.registerTimeServer( m_timeServer );
		    }

	        if ( m_operationMode == OperationMode.REPLAY &&
	                firstTimeJoin ) {
	            m_sysConfigChangeReports.add(
	                    new ATimeServerReportMessage(
	                            MessageType.TS_CONFIGURATION_CHANGE_END,
	                            -1,
	                            -1,
	                            -1,
	                            System.nanoTime()
	                            )
	                    );
	        }
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while handling a join call from a logger",
                    e
                    );
		}
	}
	
	public synchronized void joinAsMaster(
			int userIndex,
			Logger userLogger,
			boolean firstTimeJoin,
			boolean inputsCommands,
			boolean runningUIAsMaster,
            boolean fakeUser
			) {
		if ( m_sessionRegistryUI != null ) {
			m_sessionRegistryUI.addUser( userIndex );
		}

		if ( firstTimeJoin ) {
    		m_currentSysConfig.OverlayManager.userJoining( 
    				userIndex, 
    				userIndex, 
    				inputsCommands
    				);
    		Vector<AnOverlayMapping> inputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex );
    		populateMappingsWithLoggerInfo( inputDestMappings );
    		Vector<AnOverlayMapping> inputSourceMappings = m_currentSysConfig.OverlayManager.getInputSourceMappingsForUser( userIndex );
    		populateMappingsWithLoggerInfo( inputSourceMappings );
    		Vector<AnOverlayMapping> outputDestMappings = m_currentSysConfig.OverlayManager.getOutputDestMappingsForUser( userIndex );
    		populateMappingsWithLoggerInfo( outputDestMappings );
    		Vector<AnOverlayMapping> outputSourceMappings = m_currentSysConfig.OverlayManager.getOutputSourceMappingsForUser( userIndex );
    		populateMappingsWithLoggerInfo( outputSourceMappings );
    		
    		try {
    		    HashMap<Integer, Integer> userIndexToLatencyIndexMap = null;
    		    if ( firstTimeJoin && m_simulatingLatencies ) {
        			userLogger.setSimulatedLatencies( m_simulatedLatencyManager.getLatencies() );
                    int latencyUserIndex = -1;
                    if ( m_useNetworkLatencyMatrixAsIs == false ) {
                        latencyUserIndex = m_simulatedLatencyManager.assignLatencyIndexForUserIndex( userIndex );
                    }
                    else {
                        latencyUserIndex = userIndex;
                        m_simulatedLatencyManager.mapLatencyIndexForUserIndex( userIndex, latencyUserIndex );
                    }
        			userIndexToLatencyIndexMap = m_simulatedLatencyManager.getUserIndexToLatencyIndexMappings();
        			userLogger.setUserIndexToLatencyIndexMap( userIndexToLatencyIndexMap );
                    userLogger.setSimulatedLatencyIndex( latencyUserIndex );
    		    }
    		    
    		    userLogger.setReplayCommandsToLatecomers( m_replayCommandsToLatecomers );
    			userLogger.setOutputCorrespondsToInput( m_outputCorrespondsToInput );
    			userLogger.setAutoReportProcCosts( m_autoReportProcCosts );
    			userLogger.setAutoReportEachCommandToTimeServer( m_autoReportEachCommandToTimeServer );
    			userLogger.setAutoReportTransCosts( m_autoReportTransCosts );
    			
    			Logger masterLogger = null;
                if ( m_replayCommandsToLatecomers && m_currentSysConfig.MasterUserIndices.size() > 0 ) {
                    int randIndex = m_rand.nextInt( m_currentSysConfig.MasterUserIndices.size() );
                    int indexOfACurrentMaster = -1;
                    for ( int i = 0; i < m_currentSysConfig.MasterUserIndices.size(); i++ ) {
                        int nextIndex = (randIndex + i ) % m_currentSysConfig.MasterUserIndices.size();
                        indexOfACurrentMaster = m_currentSysConfig.MasterUserIndices.elementAt( nextIndex );
                        if ( indexOfACurrentMaster != userIndex ) {
                            break;
                        }
                    }
                    masterLogger = m_currentSysConfig.UserInfos.get( indexOfACurrentMaster ).getUserLogger();
                }
                
    			userLogger.prepareForNewSysConfigVersion(
    			        m_currentSysConfigVersion,
    			        userIndex,
    			        masterLogger
    			        );
    			userLogger.setSchedulingPolicy(
    			        m_schedulingPolicy,
    			        m_currentSysConfigVersion
    			        );
    			userLogger.switchToSysConfigVersion( m_currentSysConfigVersion );
    			
    			userLogger.setMappings(
    					userIndex,
    					inputDestMappings,
    					inputSourceMappings,
    					outputDestMappings,
    					outputSourceMappings,
    					m_currentSysConfigVersion
    					);
    			
    			if ( m_performanceOptimizationServerRmiStub != null ) {
    				userLogger.registerPerformanceOptimizationServer( m_performanceOptimizationServerRmiStub );
    			}
    
    		}
    		catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "ASessionRegsitry: Error in joinAsMaster",
                        e
                        );
    		}
    		
    		ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );
    		if ( userInfo == null ) {
    			userInfo = new ASessionRegistryUserInfo(
    					userIndex
    					);
    			m_currentSysConfig.UserInfos.put(
    					userIndex,
    					userInfo
    					);
    		}
    		userInfo.setUserLogger( userLogger );
    		userInfo.setMasterUserIndex( userIndex );
    		userInfo.setInputsCommands( inputsCommands );
    		userInfo.setRunningUIAsMaster( runningUIAsMaster );
    		userInfo.setSchedulingPolicy( m_schedulingPolicy );
    		userInfo.isFakeUser( fakeUser );
    		
    		/*
    		 * TODO:
    		 * 4/4/2010
    		 * Need to pass delays 
    		 * 
    		 * Then need to process delays set by user
    		 */
    		
    		if ( m_sessionRegistryUI != null ) {
    			setMappingsInSessionRegistryUI(
    					userIndex,
    					true,
    					inputDestMappings,
    					inputSourceMappings,
    					outputDestMappings,
    					outputSourceMappings,
    					-1,
    					null,
    					null,
    					m_currentSysConfigVersion
    					);
    		}	
    	
    		setSpecialMasterDestMappings(
    		        userIndex,
                    m_currentSysConfigVersion
                    );
    		m_currentSysConfig.MasterUserIndices.add( userIndex );
    
    		sendCurrentStateToPerfCollector();
		}
		
		try {
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error in joinAsMaster",
                    e
                    );
		}
        
	}
	
	private void setSpecialMasterDestMappings(
			int userIndex,
			int sysConfigVersion
			) {
		try {
			ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );
			
			Vector<AnOverlayMapping> specialInputDestMappings = null;
			if ( !userInfo.isMaster() ) {
				int masterUserIndex = userInfo.getMasterUserIndex();
				ASessionRegistryUserInfo masterUserInfo = m_currentSysConfig.UserInfos.get( masterUserIndex );
				
				if ( masterUserInfo != null ) {
					
					specialInputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( 
							masterUserIndex,
							userIndex
							);
					populateMappingsWithLoggerInfo( specialInputDestMappings );
		
					masterUserInfo.getUserLogger().setSpecialDestMappings( 
							specialInputDestMappings,
							null,
							sysConfigVersion
							);
					if ( m_sessionRegistryUI != null ) {
						setMappingsInSessionRegistryUI(
								-1,
								true,
								null,
								null,
								null,
								null,
								masterUserIndex,
								specialInputDestMappings,
								null,
								sysConfigVersion								
								);
					}
				}
			}
			
			Vector<AnOverlayMapping> specialOutputDestMappings = null;
			for ( int i = 0; i < m_currentSysConfig.MasterUserIndices.size(); i++ ) {
				int masterUserIndex = m_currentSysConfig.MasterUserIndices.elementAt( i );
				ASessionRegistryUserInfo masterUserInfo = m_currentSysConfig.UserInfos.get( masterUserIndex );
				
				specialOutputDestMappings = m_currentSysConfig.OverlayManager.getOutputDestMappingsForUser(
						masterUserIndex,
						userIndex
						);
				
				if ( !userInfo.isMaster() && 
						masterUserIndex == userInfo.getMasterUserIndex() ) {
					specialOutputDestMappings = removeSpecialOutputDestMappingsForDest(
							specialOutputDestMappings,
							userIndex
							);
				}
				
				populateMappingsWithLoggerInfo( specialOutputDestMappings );
				masterUserInfo.getUserLogger().setSpecialDestMappings(
						null,
						specialOutputDestMappings,
						m_currentSysConfigVersion
						);
				if ( m_sessionRegistryUI != null ) {
					setMappingsInSessionRegistryUI(
							-1,
							true,
							null,
							null,
							null,
							null,
							masterUserIndex,
							null,
							specialOutputDestMappings,
							sysConfigVersion
							);
				}	
			}
			
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while setting special dest mappings",
                    e
                    );
		}
	}
	
	private void unsetSpecialMasterDestMappings(
			int userIndex,
			int sysConfigVersion
			) {
		
		try {
			ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );
			
			Vector<AnOverlayMapping> specialInputDestMappings = null;
			if ( !userInfo.isMaster() ) {
				int masterUserIndex = userInfo.getMasterUserIndex();
				ASessionRegistryUserInfo masterUserInfo = m_currentSysConfig.UserInfos.get( masterUserIndex );
				
				if ( masterUserInfo != null ) {
					
					specialInputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( 
							masterUserIndex,
							userIndex
							);
					populateMappingsWithLoggerInfo( specialInputDestMappings );
		
					masterUserInfo.getUserLogger().unsetSpecialDestMappings( 
							specialInputDestMappings,
							null,
							sysConfigVersion
							);
					if ( m_sessionRegistryUI != null ) {
						unsetMappingsInSessionRegistryUI(
								-1,
								true,
								null,
								null,
								null,
								null,
								masterUserIndex,
								specialInputDestMappings,
								null,
								sysConfigVersion
								);
					}
				}
			}
			
			Vector<AnOverlayMapping> specialOutputDestMappings = null;
			for ( int i = 0; i < m_currentSysConfig.MasterUserIndices.size(); i++ ) {
				int masterUserIndex = m_currentSysConfig.MasterUserIndices.elementAt( i );
				ASessionRegistryUserInfo masterUserInfo = m_currentSysConfig.UserInfos.get( masterUserIndex );
				
				specialOutputDestMappings = m_currentSysConfig.OverlayManager.getOutputDestMappingsForUser(
						masterUserIndex,
						userIndex
						);
				
				if ( !userInfo.isMaster() && 
						masterUserIndex == userInfo.getMasterUserIndex() ) {
					specialOutputDestMappings = removeSpecialOutputDestMappingsForDest(
							specialOutputDestMappings,
							userIndex
							);
				}
				
				populateMappingsWithLoggerInfo( specialOutputDestMappings );
				masterUserInfo.getUserLogger().unsetSpecialDestMappings(
						null,
						specialOutputDestMappings,
						m_currentSysConfigVersion
						);
				if ( m_sessionRegistryUI != null ) {
					unsetMappingsInSessionRegistryUI(
							-1,
							true,
							null,
							null,
							null,
							null,
							masterUserIndex,
							null,
							specialOutputDestMappings,
                            sysConfigVersion
							);
				}	
			}
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while unsetting special dest mappings",
                    e
                    );
		}
		
	}

	public synchronized void joinAsSlave(
			int userIndex,
			int masterUserIndex,
			Logger userLogger,
			boolean firstTimeJoin,
			boolean inputsCommands,
			boolean fakeUser
			) {
		
		m_currentSysConfig.MasterUserIndices.removeElement( userIndex );
		
		if ( m_sessionRegistryUI != null ) {
			m_sessionRegistryUI.addUser( userIndex );
		}

		if ( firstTimeJoin ) {
        	m_currentSysConfig.OverlayManager.userJoining(
        			userIndex,
        			masterUserIndex,
        			inputsCommands
        			);
        	Vector<AnOverlayMapping> inputDestMappings;
        	Vector<AnOverlayMapping> inputSourceMappings;
        	Vector<AnOverlayMapping> outputDestMappings;
        	Vector<AnOverlayMapping> outputSourceMappings;
        	
        	inputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex );
        	populateMappingsWithLoggerInfo( inputDestMappings );
        	inputSourceMappings = m_currentSysConfig.OverlayManager.getInputSourceMappingsForUser( userIndex );
        	populateMappingsWithLoggerInfo( inputSourceMappings );
        	outputDestMappings = m_currentSysConfig.OverlayManager.getOutputDestMappingsForUser( userIndex );
        	populateMappingsWithLoggerInfo( outputDestMappings );
        	outputSourceMappings = m_currentSysConfig.OverlayManager.getOutputSourceMappingsForUser( userIndex );
        	populateMappingsWithLoggerInfo( outputSourceMappings );
        	
        	try {
                HashMap<Integer, Integer> userIndexToLatencyIndexMap = null;
        	    if ( firstTimeJoin && m_simulatingLatencies ) {
                    userLogger.setSimulatedLatencies( m_simulatedLatencyManager.getLatencies() );
                    int latencyUserIndex = -1;
                    if ( m_useNetworkLatencyMatrixAsIs == false ) {
                        latencyUserIndex = m_simulatedLatencyManager.assignLatencyIndexForUserIndex( userIndex );
                    }
                    else {
                        latencyUserIndex = userIndex;
                        m_simulatedLatencyManager.mapLatencyIndexForUserIndex( userIndex, latencyUserIndex );
                    }
                    userIndexToLatencyIndexMap = m_simulatedLatencyManager.getUserIndexToLatencyIndexMappings();
                    userLogger.setUserIndexToLatencyIndexMap( userIndexToLatencyIndexMap );
                    userLogger.setSimulatedLatencyIndex( latencyUserIndex );
        	    }
        	    
        		userLogger.setReplayCommandsToLatecomers( m_replayCommandsToLatecomers );
        		userLogger.setOutputCorrespondsToInput( m_outputCorrespondsToInput );
        		userLogger.setAutoReportProcCosts( m_autoReportProcCosts );
                userLogger.setAutoReportTransCosts( m_autoReportTransCosts );
                userLogger.setAutoReportEachCommandToTimeServer( m_autoReportEachCommandToTimeServer );
                
                Logger masterLogger = null;
                if ( firstTimeJoin && m_replayCommandsToLatecomers ) {
                    if ( m_currentSysConfig.MasterUserIndices.size() > 0 ) {
                        int randIndex = m_rand.nextInt( m_currentSysConfig.MasterUserIndices.size() );
                        int indexOfACurrentMaster = -1;
                        for ( int i = 0; i < m_currentSysConfig.MasterUserIndices.size(); i++ ) {
                            int nextIndex = (randIndex + i ) % m_currentSysConfig.MasterUserIndices.size();
                            if ( m_currentSysConfig.UserInfos.get( nextIndex ).isInputtingCommands() ) {
                                indexOfACurrentMaster = m_currentSysConfig.MasterUserIndices.elementAt( nextIndex );
                                break;
                            }
                        }
                        masterLogger = m_currentSysConfig.UserInfos.get( indexOfACurrentMaster ).getUserLogger();
                    }
                }

                userLogger.prepareForNewSysConfigVersion(
                        m_currentSysConfigVersion,
                        masterUserIndex,
                        masterLogger
                        );
                userLogger.setSchedulingPolicy(
                        m_schedulingPolicy,
                        m_currentSysConfigVersion
                        );
                userLogger.switchToSysConfigVersion( m_currentSysConfigVersion );
                
        		userLogger.setMappings(
        				masterUserIndex,
        				inputDestMappings,
        				inputSourceMappings,
        				outputDestMappings,
        				outputSourceMappings,
        				m_currentSysConfigVersion
        				);
        		
        		if ( m_performanceOptimizationServerRmiStub != null ) {
        			userLogger.registerPerformanceOptimizationServer( m_performanceOptimizationServerRmiStub );
        		}
        		
        	}
        	catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "ASessionRegsitry: Error in joinAsSlave",
                        e
                        );
        	}
        
            ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );
        	if ( userInfo == null ) {
        		userInfo = new ASessionRegistryUserInfo(
        				userIndex
        				);
        		m_currentSysConfig.UserInfos.put(
        				userIndex,
        				userInfo
        				);
        	}
        	userInfo.setUserLogger( userLogger );
        	userInfo.setMasterUserIndex( masterUserIndex );
        	userInfo.setInputsCommands( inputsCommands );
            userInfo.setSchedulingPolicy( m_schedulingPolicy );
            userInfo.isFakeUser( fakeUser );
        	
        	if ( m_sessionRegistryUI != null ) {
        		setMappingsInSessionRegistryUI(
        				userIndex,
        				true,
        				inputDestMappings,
        				inputSourceMappings,
        				outputDestMappings,
        				outputSourceMappings,
        				-1,
        				null,
        				null,
        				m_currentSysConfigVersion
        				);
        	}
        
        	setSpecialMasterDestMappings(
        	        userIndex,
        	        m_currentSysConfigVersion
        	        );
        	
        	sendCurrentStateToPerfCollector();
		}
		
		try {
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error in joinAsSlave",
                    e
                    );
		}
        
	}
	
	private void cleanupMappings(
			int userIndex,
			int sysConfigVersion
			) {
		
		try {
			ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );

			Vector<AnOverlayMapping> inputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex );
			populateMappingsWithLoggerInfo( inputDestMappings );
			Vector<AnOverlayMapping> inputSourceMappings = m_currentSysConfig.OverlayManager.getInputSourceMappingsForUser( userIndex );
			populateMappingsWithLoggerInfo( inputSourceMappings );
			Vector<AnOverlayMapping> outputDestMappings = m_currentSysConfig.OverlayManager.getOutputDestMappingsForUser( userIndex );
			populateMappingsWithLoggerInfo( outputDestMappings );
			Vector<AnOverlayMapping> outputSourceMappings = m_currentSysConfig.OverlayManager.getOutputSourceMappingsForUser( userIndex );
			populateMappingsWithLoggerInfo( outputSourceMappings );
			
			userInfo.getUserLogger().unsetMappings(
					inputDestMappings, 
					inputSourceMappings, 
					outputDestMappings, 
					outputSourceMappings,
					m_currentSysConfigVersion
					);

			unsetSpecialMasterDestMappings(
			        userIndex,
                    sysConfigVersion
                    );
			
			if ( m_sessionRegistryUI != null ) {
				
				unsetMappingsInSessionRegistryUI(
						userIndex,
						true,
						inputDestMappings,
						inputSourceMappings,
						outputDestMappings,
						outputSourceMappings,
						-1,
						null,
						null,
                        sysConfigVersion
						);
			}
			
			if ( m_sessionRegistryUI != null ) {
				m_sessionRegistryUI.removeUser( userIndex );
			}
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while cleaning up mappings",
                    e
                    );
		}

		
	}
	
	/*
	 * This will likely need to be qualified with "synchronized"
	 * When a simulation ends, many loggers quit at once and there
	 * may be concurrency issues here.
	 * 
	 * As an alternative, think of locking parts of this method and
	 * qualifying the ALogger.leaving() with synchronized.
	 */
	public void leaving(
			int userIndex
			) {
		permanentlyLeaving( userIndex );
	}
	
	private void temporarilyLeaving(
	        int userIndex
	        ) {
		try {
			Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> userInfoIterator = m_currentSysConfig.UserInfos.entrySet().iterator();
			while ( userInfoIterator.hasNext() ) {
				Map.Entry<Integer, ASessionRegistryUserInfo> entry = userInfoIterator.next();
				ASessionRegistryUserInfo currentUserInfo = entry.getValue();
				
				if ( currentUserInfo.getUserIndex() == userIndex ) {
					continue;
				}
				
				currentUserInfo.getUserLogger().leaving( userIndex );
			}
			
            if ( m_systemQuitting ) {
                return;
            }
            
			cleanupMappings(
					userIndex,
					m_currentSysConfigVersion
					);
			
			m_currentSysConfig.OverlayManager.userLeaving( userIndex );
			
			m_currentSysConfig.MasterUserIndices.removeElement( userIndex );
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while handling leave notifcation from logger",
                    e
                    );
		}
	}
	
	private void permanentlyLeaving(
	        int userIndex
	        ) {
		try {
			temporarilyLeaving(
			        userIndex
			        );
			
			ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );
			
			userInfo.getUserLogger().leaving( userIndex );
			
			m_currentSysConfig.UserInfos.remove( userIndex );

			if ( m_sessionRegistryUI != null ) {
				m_sessionRegistryUI.removeUser( userIndex );
			}
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while handling permanent leave by user",
                    e
                    );
		}
		
	}
	
	private void populateMappingsWithLoggerInfo(
			Vector<AnOverlayMapping> mappings
			) {
		for ( int i = 0; i < mappings.size(); i++ ) {
			int curUserIndex = mappings.elementAt( i ).getUserIndex();
			Logger logger = m_currentSysConfig.UserInfos.get( curUserIndex ).getUserLogger();
			mappings.elementAt( i ).setUserLogger( logger );
		}
	}
   
    public void reset() {
        m_currentSysConfig.UserInfos.clear();
        m_currentSysConfig.MasterUserIndices.clear();
        
        m_currentSysConfigVersion = 0;
        m_currentSysConfig = new ASessionRegistrySysConfigInfo();
        m_sysConfigs.put(
                0,
                m_currentSysConfig
                );
        
        m_sysConfigChangeReports.clear();
    }
    
    public void setSessionRegistryUI( SessionRegistryUI sessionRegistryUI ) {
        m_sessionRegistryUI = sessionRegistryUI;
    }
    
	private void inputMappingAdded(
			int sourceUserIndex,
			int destUserIndex,
			int rootUserIndex,
			int sysConfigVersion
			) {
		
		if ( m_sessionRegistryUI == null ) {
			return;
		}
		
		ASessionRegistrySysConfigInfo sysConfig = m_sysConfigs.get( sysConfigVersion );
		
		ASessionRegistryUserInfo sourceUserInfo = sysConfig.UserInfos.get( sourceUserIndex );
		ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( destUserIndex );
		
		MappedDirection mappedDirection = null;
		if ( sourceUserIndex == destUserIndex ) {
			mappedDirection = MappedDirection.UI_TO_LOCAL_PC;
		}
		else {
			if ( sourceUserInfo.isMaster() && destUserInfo.isMaster() ) {
				mappedDirection = MappedDirection.PC_TO_REMOTE_PC;
			}
			else {
				mappedDirection = MappedDirection.UI_TO_REMOTE_PC;
			}
		}
		
		UIMapping mapping = new AUIMapping(
				sourceUserIndex,
				destUserIndex,
				rootUserIndex,
				MappedCommand.INPUT,
				mappedDirection,
				m_simulatingLatencies ?
						(int) m_simulatedLatencyManager.getLatency( sourceUserIndex, destUserIndex ) : 0
				);
		m_sessionRegistryUI.addMapping( mapping );
		
	}
	
	private void outputMappingAdded(
			int sourceUserIndex,
			int destUserIndex,
			int rootUserIndex,
			int sysConfigVersion
			) {
		
		if ( m_sessionRegistryUI == null ) {
			return;
		}
		
		ASessionRegistrySysConfigInfo sysConfig = m_sysConfigs.get( sysConfigVersion );
		
		ASessionRegistryUserInfo sourceUserInfo = sysConfig.UserInfos.get( sourceUserIndex );
		ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( destUserIndex );
		
		MappedDirection mappedDirection = null;
		if ( sourceUserIndex == destUserIndex ) {
			mappedDirection = MappedDirection.PC_TO_LOCAL_UI;
		}
		else {
			if( sourceUserInfo.isMaster() && !destUserInfo.isMaster() ) {
				mappedDirection = MappedDirection.PC_TO_REMOTE_UI;
			}
			else {
				mappedDirection = MappedDirection.UI_TO_REMOTE_UI;
			}
		}
		
		UIMapping mapping = new AUIMapping(
				sourceUserIndex,
				destUserIndex,
				rootUserIndex,
				MappedCommand.OUTPUT,
				mappedDirection,
				m_simulatingLatencies ?
						(int) m_simulatedLatencyManager.getLatency( sourceUserIndex, destUserIndex ) : 0
				);
		m_sessionRegistryUI.addMapping( mapping );
		
	}
	
	private void inputMappingRemoved(
			int sourceUserIndex,
			int destUserIndex,
			int rootUserIndex
			) {
		
		if ( m_sessionRegistryUI == null ) {
			return;
		}
		
		UIMapping mapping = new AUIMapping(
				sourceUserIndex,
				destUserIndex,
				rootUserIndex,
				MappedCommand.INPUT,
				null,
				m_simulatingLatencies ?
						(int) m_simulatedLatencyManager.getLatency( sourceUserIndex, destUserIndex ) : 0
				);
		m_sessionRegistryUI.removeMapping( mapping );
		
	}
	
	private void outputMappingRemoved(
			int sourceUserIndex,
			int destUserIndex,
			int rootUserIndex
			) {
		
		if ( m_sessionRegistryUI == null ) {
			return;
		}
		
		UIMapping mapping = new AUIMapping(
				sourceUserIndex,
				destUserIndex,
				rootUserIndex,
				MappedCommand.OUTPUT,
				null,
				m_simulatingLatencies ?
						(int) m_simulatedLatencyManager.getLatency( sourceUserIndex, destUserIndex ) : 0
				);
		m_sessionRegistryUI.removeMapping( mapping );
		
	}
	
	public void registerPerformanceOptimizationServer(
			PerformanceOptimizationServer performanceOptimizationServer
			) {
	    m_performanceOptimizationServerRmiStub = performanceOptimizationServer;
	}
	
    public void startPerformanceOptimizationServer() {
        /*
         * TODO: control starting and stopping of performance optimization server through flags
         * TODO: have separate start stop flag for collecting performance data
         */
        try {
            if ( m_performanceOptimizationServerRmiStub == null ) {
                return;
            }
            
            m_performanceOptimizationServerRmiStub.begin();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while starting perf report optimizer",
                    e
                    );
        }
    }
    
	public void setSchedulingPolicy( 
			SchedulingPolicy schedulingPolicy 
			) {
		m_schedulingPolicy = schedulingPolicy;
		
        m_currentSysConfigVersion++;
	    sendCurrentStateToPerfCollector();
	}
	
	public SchedulingPolicy getSchedulingPolicy() {
		return m_schedulingPolicy;
	}
	
	public ProcessingArchitectureType getProcessingArchitecture() {

		int numMasters = 0;
		
		Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> userInfoIterator = 
		    m_currentSysConfig.UserInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			ASessionRegistryUserInfo userInfo = userInfoIterator.next().getValue();
			
			if ( userInfo.isMaster() ) {
				numMasters++;
			}
		}
		
		if ( numMasters == m_currentSysConfig.UserInfos.size() ) {
			return ProcessingArchitectureType.REPLICATED;
		}
		else if ( numMasters == 1 ) {
			return ProcessingArchitectureType.CENTRALIZED;
		}
		else {
			return ProcessingArchitectureType.HYBRID;
		}
	}
	
	public Overlay getOverlay(
			int userIndex
			) {
		return m_currentSysConfig.OverlayManager.getCurrentOverlay( userIndex );
	}
	
	public synchronized void setupCentralizedArchitecture(
    		int newMasterUserIndex
    		) {
       
        List<Integer> masterUserIndices = new LinkedList<Integer>();
        HashMap<Integer, SchedulingPolicy> schedPols = new HashMap<Integer, SchedulingPolicy>();
        HashMap<Integer, HashMap<PerformanceParameterType, Double>> observedTransCostsPerDest = new HashMap<Integer, HashMap<PerformanceParameterType, Double>>();
        HashMap<Integer, Overlay> overlays = new HashMap<Integer, Overlay>();
        
        Iterator<ASessionRegistryUserInfo> itr = m_currentSysConfig.UserInfos.values().iterator();
        List<Integer> currentUserIndices = new LinkedList<Integer>();
        while ( itr.hasNext() ) {
            currentUserIndices.add( itr.next().getUserIndex() );
        }
        
        masterUserIndices.add( newMasterUserIndex );
        
        itr = m_currentSysConfig.UserInfos.values().iterator();
        while ( itr.hasNext() ) {
            ASessionRegistryUserInfo userInfo = itr.next();
            
            schedPols.put(
                    userInfo.getUserIndex(),
                    userInfo.getSchedulingPolicy()
                    );
            observedTransCostsPerDest.put(
                    userInfo.getUserIndex(),
                    userInfo.getAllObservedTransCosts()
                    );
            
            Map<Integer, List<Integer>> children = new Hashtable<Integer, List<Integer>>();
            List<Integer> childrenOfMaster = new LinkedList<Integer>();
            for ( int i = 0; i < currentUserIndices.size(); i++ ) {
                if ( currentUserIndices.get( i ) == newMasterUserIndex ) {
                    continue;
                }
                childrenOfMaster.add( currentUserIndices.get( i ) );
                children.put(
                        currentUserIndices.get( i ),
                        null
                        );
            }
            children.put(
                    newMasterUserIndex,
                    childrenOfMaster
                    );
            
            if ( userInfo.isInputtingCommands() ) {
                int inputtingUserIndex = userInfo.getUserIndex();
                
                int[] parents = new int[ m_currentSysConfig.UserInfos.size() ];
                int[] addOrder = new int[ m_currentSysConfig.UserInfos.size() ];
                int[] numChildren = new int[ m_currentSysConfig.UserInfos.size() ];
                
                
                addOrder[0] = newMasterUserIndex;
                numChildren[ newMasterUserIndex ] = currentUserIndices.size() - 1;
                parents[ newMasterUserIndex ] = -1;

                int addOrderPos = 1;
                for ( int i = 0; i < currentUserIndices.size(); i++ ) {
                    int curUserIndex = currentUserIndices.get( i );
                    if ( curUserIndex == newMasterUserIndex ) {
                        continue;
                    }
                    
                    numChildren[ curUserIndex ] = 0;
                    parents[ curUserIndex ] = newMasterUserIndex;
                    addOrder[ addOrderPos ] = curUserIndex;
                    addOrderPos++;
                }
                
                Overlay overlay = new AnOverlay(
                        parents,
                        addOrder,
                        numChildren,
                        children
                        );
                overlays.put(
                        inputtingUserIndex,
                        overlay
                        );
            }
        }
        
        try {
            setupNewSysConfig(
                    m_currentSysConfigVersion,
                    masterUserIndices,
                    overlays,
                    schedPols,
                    observedTransCostsPerDest
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegistry: Could not setup new centralized sys config",
                    e
                    );
        }
        
	}

	public synchronized void setupReplicatedArchitecture() {
        
        List<Integer> masterUserIndices = new LinkedList<Integer>();
        HashMap<Integer, SchedulingPolicy> schedPols = new HashMap<Integer, SchedulingPolicy>();
        HashMap<Integer, HashMap<PerformanceParameterType, Double>> observedTransCostsPerDest = new HashMap<Integer, HashMap<PerformanceParameterType,Double>>();
        HashMap<Integer, Overlay> overlays = new HashMap<Integer, Overlay>();
        
        Iterator<ASessionRegistryUserInfo> itr = m_currentSysConfig.UserInfos.values().iterator();
        List<Integer> currentUserIndices = new LinkedList<Integer>();
        while ( itr.hasNext() ) {
            currentUserIndices.add( itr.next().getUserIndex() );
        }
        
        itr = m_currentSysConfig.UserInfos.values().iterator();
        while ( itr.hasNext() ) {
            ASessionRegistryUserInfo userInfo = itr.next();
            
            masterUserIndices.add( userInfo.getUserIndex() );
            
            schedPols.put(
                    userInfo.getUserIndex(),
                    userInfo.getSchedulingPolicy()
                    );
            observedTransCostsPerDest.put(
                    userInfo.getUserIndex(),
                    userInfo.getAllObservedTransCosts()
                    );
            
            if ( userInfo.isInputtingCommands() ) {
                int inputtingUserIndex = userInfo.getUserIndex();
                
                int[] parents = new int[ m_currentSysConfig.UserInfos.size() ];
                int[] addOrder = new int[ m_currentSysConfig.UserInfos.size() ];
                int[] numChildren = new int[ m_currentSysConfig.UserInfos.size() ];
                Map<Integer, List<Integer>> children = new Hashtable<Integer, List<Integer>>();
                List<Integer> childrenOfInputtingUser = new LinkedList<Integer>();
                
                addOrder[0] = inputtingUserIndex;
                numChildren[ inputtingUserIndex ] = currentUserIndices.size() - 1;
                parents[ inputtingUserIndex ] = -1;

                int addOrderPos = 1;
                for ( int i = 0; i < currentUserIndices.size(); i++ ) {
                    int curUserIndex = currentUserIndices.get( i );
                    if ( curUserIndex == inputtingUserIndex ) {
                        continue;
                    }
                    
                    numChildren[ curUserIndex ] = 0;
                    parents[ curUserIndex ] = inputtingUserIndex;
                    addOrder[ addOrderPos ] = curUserIndex;
                    addOrderPos++;
                    children.put(
                            curUserIndex,
                            null
                            );
                    childrenOfInputtingUser.add( curUserIndex );
                }
                children.put(
                        inputtingUserIndex,
                        childrenOfInputtingUser
                        );
                
                Overlay overlay = new AnOverlay(
                        parents,
                        addOrder,
                        numChildren,
                        children
                        );
                overlays.put(
                        inputtingUserIndex,
                        overlay
                        );
            }
        }
        
        try {
            setupNewSysConfig(
                    m_currentSysConfigVersion,
                    masterUserIndices,
                    overlays,
                    schedPols,
                    observedTransCostsPerDest
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegistry: Could not setup new replicated sys config",
                    e
                    );
        }
        
	}
	
	private void setMappingsInSessionRegistryUI(
			int userIndex,
			boolean userIsJoining,
			Vector<AnOverlayMapping> inputDestMappings,
			Vector<AnOverlayMapping> inputSourceMappings,
			Vector<AnOverlayMapping> outputDestMappings,
			Vector<AnOverlayMapping> outputSourceMappings,
			int specialDestMappingsUserIndex,
			Vector<AnOverlayMapping> specialInputDestMappings,
			Vector<AnOverlayMapping> specialOutputDestMappings,
			int sysConfigVersion
			) {
	    ASessionRegistrySysConfigInfo sysConfig = m_sysConfigs.get( sysConfigVersion );
	    
		ASessionRegistryUserInfo userInfo = sysConfig.UserInfos.get( userIndex );
		
		if ( inputDestMappings != null ) {
			for ( int i = 0; i < inputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( inputDestMappings.elementAt( i ).getUserIndex() );
				inputMappingAdded(
						userInfo.getUserIndex(),
						destUserInfo.getUserIndex(),
						inputDestMappings.elementAt( i ).getRootUserIndex(),
						sysConfigVersion
						);
			}
		}
		
		if ( inputSourceMappings != null ) {
			for ( int i = 0; i < inputSourceMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( inputSourceMappings.elementAt( i ).getUserIndex() );
				inputMappingAdded(
						destUserInfo.getUserIndex(),
						userInfo.getUserIndex(),
						inputSourceMappings.elementAt( i ).getRootUserIndex(),
                        sysConfigVersion
						);
			}
		}
		
		if ( outputDestMappings != null ) {
			for ( int i = 0; i < outputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( outputDestMappings.elementAt( i ).getUserIndex() );
				outputMappingAdded(
						userIndex,
						destUserInfo.getUserIndex(),
						outputDestMappings.elementAt( i ).getRootUserIndex(),
                        sysConfigVersion
						);
			}
		}
		
		if ( outputSourceMappings != null ) {
			for ( int i = 0; i < outputSourceMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( outputSourceMappings.elementAt( i ).getUserIndex() );
				outputMappingAdded(
						destUserInfo.getUserIndex(),
						userIndex,
						outputSourceMappings.elementAt( i ).getRootUserIndex(),
                        sysConfigVersion
						);
			}
		}
		
		if ( specialInputDestMappings != null ) {
			for ( int i = 0; i < specialInputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo sourceUserInfo = sysConfig.UserInfos.get( specialDestMappingsUserIndex );
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( specialInputDestMappings.elementAt( i ).getUserIndex() );
				inputMappingAdded(
						sourceUserInfo.getUserIndex(),
						destUserInfo.getUserIndex(),
						specialInputDestMappings.elementAt( i ).getRootUserIndex(),
                        sysConfigVersion
						);
			}
		}
		
		if ( specialOutputDestMappings != null ) {
			for ( int i = 0; i < specialOutputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo sourceUserInfo = sysConfig.UserInfos.get( specialDestMappingsUserIndex );
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( specialOutputDestMappings.elementAt( i ).getUserIndex() );
				outputMappingAdded(
						sourceUserInfo.getUserIndex(),
						destUserInfo.getUserIndex(),
						specialOutputDestMappings.elementAt( i ).getRootUserIndex(),
                        sysConfigVersion
						);
			}
		}
		
		if ( userIndex != -1 ) {
			if ( userInfo.getUserIndex() == userInfo.getMasterUserIndex() ) {
				if ( userInfo.isInputtingCommands() ) {
					inputMappingAdded(
							userIndex,
							userIndex,
							userIndex,
	                        sysConfigVersion
							);
				}
				
				if ( userIsJoining ) {
					outputMappingAdded(
							userIndex,
							userIndex,
							-1,
	                        sysConfigVersion
							);
				}
			}
		}
		
	}
	
	private void unsetMappingsInSessionRegistryUI(
			int userIndex,
			boolean userIsLeaving,
			Vector<AnOverlayMapping> inputDestMappings,
			Vector<AnOverlayMapping> inputSourceMappings,
			Vector<AnOverlayMapping> outputDestMappings,
			Vector<AnOverlayMapping> outputSourceMappings,
			int specialDestMappingsUserIndex,
			Vector<AnOverlayMapping> specialInputDestMappings,
			Vector<AnOverlayMapping> specialOutputDestMappings,
			int sysConfigVersion
			) {
	    
	    ASessionRegistrySysConfigInfo sysConfig = m_sysConfigs.get( sysConfigVersion );
	    
		ASessionRegistryUserInfo userInfo = sysConfig.UserInfos.get( userIndex );
		
		if ( inputDestMappings != null ) {
			for ( int i = 0; i < inputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( inputDestMappings.elementAt( i ).getUserIndex() );
				inputMappingRemoved(
						userIndex,
						destUserInfo.getUserIndex(),
						inputDestMappings.elementAt( i ).getRootUserIndex()
						);
			}
		}
		
		if ( inputSourceMappings != null ) {
			for ( int i = 0; i < inputSourceMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( inputSourceMappings.elementAt( i ).getUserIndex() );
				inputMappingRemoved(
						destUserInfo.getUserIndex(),
						userIndex,
						inputSourceMappings.elementAt( i ).getRootUserIndex()
						);
			}
		}
		
		if ( outputDestMappings != null ) {
			for ( int i = 0; i < outputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( outputDestMappings.elementAt( i ).getUserIndex() );
				outputMappingRemoved(
						userIndex,
						destUserInfo.getUserIndex(),
						outputDestMappings.elementAt( i ).getRootUserIndex()
						);
			}
		}
		
		if ( outputSourceMappings != null ) {
			for ( int i = 0; i < outputSourceMappings.size(); i++ ) {
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( outputSourceMappings.elementAt( i ).getUserIndex() );
				outputMappingRemoved(
						destUserInfo.getUserIndex(),
						userIndex,
						outputSourceMappings.elementAt( i ).getRootUserIndex()
						);
			}
		}
		
		if ( specialInputDestMappings != null ) {
			for ( int i = 0; i < specialInputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo sourceUserInfo = sysConfig.UserInfos.get( specialDestMappingsUserIndex );
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( specialInputDestMappings.elementAt( i ).getUserIndex() );
				inputMappingRemoved(
						sourceUserInfo.getUserIndex(),
						destUserInfo.getUserIndex(),
						specialInputDestMappings.elementAt( i ).getRootUserIndex()
						);
			}
		}
		
		if ( specialOutputDestMappings != null ) {
			for ( int i = 0; i < specialOutputDestMappings.size(); i++ ) {
				ASessionRegistryUserInfo sourceUserInfo = sysConfig.UserInfos.get( specialDestMappingsUserIndex );
				ASessionRegistryUserInfo destUserInfo = sysConfig.UserInfos.get( specialOutputDestMappings.elementAt( i ).getUserIndex() );
				outputMappingRemoved(
						sourceUserInfo.getUserIndex(),
						destUserInfo.getUserIndex(),
						specialOutputDestMappings.elementAt( i ).getRootUserIndex()
						);
			}
		}
		
		if ( userIndex != -1 ) {
			if ( userInfo.getUserIndex() == userInfo.getMasterUserIndex() ) {
				if ( !userInfo.isInputtingCommands() || userIsLeaving ) {
					inputMappingRemoved(
							userIndex,
							userIndex,
							userIndex
							);
				}
				
				if ( userIsLeaving ) {
					outputMappingRemoved(
							userIndex,
							userIndex,
							-1
							);
				}
			}
		}
		
	}

	public void quit() {
		System.err.println( "AGraphicalSessionRegistryUI Quitting ... " );

		try {
		    if ( m_performanceOptimizationServerRmiStub != null ) {
		        m_performanceOptimizationServerRmiStub.quit();
		    }
		    
		    if ( m_mainCpp.getBooleanParam( Parameters.OUTPUT_SYS_CONFIG_CHANGES ) ) {
		        ASysConfigDebugInfoPrinter.outputDebugInfoForRequestedCommArchs(
		                "",
		                m_currentSysConfigVersion,
		                m_sysConfigs
		                );
		    }
		    
			System.err.print( "AGraphicalSessionRegistryUI Unregistering from Java RMI registry ... " );
			Registry registry = LocateRegistry.getRegistry( m_registryHost, m_registryPort );
            registry.unbind( "SessionRegistry" );
			System.err.println( "Done!" );
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while trying to quit",
                    e
                    );
		}
		
		System.err.println( "AGraphicalSessionRegistryUI Exited!" );
	}
	
	public void updateUserInputsCommands(
			int userIndex,
			boolean inputsCommands
			) {
		try {
			ASessionRegistryUserInfo userInfo = m_currentSysConfig.UserInfos.get( userIndex );
			
			if ( userInfo.isInputtingCommands() == inputsCommands ) {
				return;
			}
			userInfo.setInputsCommands( inputsCommands );
			
			if ( !inputsCommands ) {
				Vector<AnOverlayMapping> inputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex );
				populateMappingsWithLoggerInfo( inputDestMappings );
				userInfo.getUserLogger().unsetMappings(
						inputDestMappings,
						null,
						null,
						null,
						m_currentSysConfigVersion
						);
				
				unsetSpecialMasterDestMappings(
				        userIndex,
				        m_currentSysConfigVersion
				        );
				
				if ( m_sessionRegistryUI != null ) {
					unsetMappingsInSessionRegistryUI(
							userInfo.getUserIndex(),
							false,
							m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex ),
							null,
							null,
							null,
							-1,
							null,
							null,
							m_currentSysConfigVersion
							);
				}
				
				m_currentSysConfig.OverlayManager.updateUserInputsCommands(
						userIndex,
						inputsCommands
						);
			}
			else {
			    m_currentSysConfig.OverlayManager.updateUserInputsCommands(
						userIndex,
						inputsCommands
						);
				Vector<AnOverlayMapping> inputDestMappings = m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex );
				populateMappingsWithLoggerInfo( inputDestMappings );
				userInfo.getUserLogger().setMappings(
						userInfo.getMasterUserIndex(),
						inputDestMappings,
						null,
						null,
						null,
						m_currentSysConfigVersion
						);

				setSpecialMasterDestMappings(
				        userIndex,
				        m_currentSysConfigVersion
				        );
				
				if ( m_sessionRegistryUI != null ) {
					setMappingsInSessionRegistryUI(
							userInfo.getUserIndex(),
							false,
							m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex ),
							null,
							null,
							null,
							-1,
							null,
							null,
							m_currentSysConfigVersion
							);
				}
			}
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while handling update to setting user setting commands or not",
                    e
                    );
		}
	}
	
	private Vector<AnOverlayMapping> removeSpecialOutputDestMappingsForDest( 
			Vector<AnOverlayMapping> specialOutputDestMappings,
			int userIndex 
			) {
		
		Vector<AnOverlayMapping> newSpecialOutputDestMappings = new Vector<AnOverlayMapping>();
		
		for ( int i = 0; i < specialOutputDestMappings.size(); i++ ) {
			if ( specialOutputDestMappings.elementAt( i ).getUserIndex() == userIndex ) {
				continue;
			}
			
			newSpecialOutputDestMappings.add( specialOutputDestMappings.elementAt( i ) );
		}
		
		return newSpecialOutputDestMappings;
	}

	private void sendCurrentStateToPerfCollector() {
		try {
			if ( m_performanceOptimizationServerRmiStub != null ) {
			    final int curSysConfigVersion = m_currentSysConfigVersion;
				final Vector<Integer> userIndices = new Vector<Integer>();
				final Vector<Integer> masterUserIndices = new Vector<Integer>();
				final Vector<Integer> inputtingUserIndices = new Vector<Integer>();
				final Vector<Integer> runningUIAsMaster = new Vector<Integer>();
				final HashMap<Integer, Overlay> curNetworkOverlays = new HashMap<Integer, Overlay>();
				Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> userInfoIterator = 
				    m_currentSysConfig.UserInfos.entrySet().iterator();
				final HashMap<Integer, SchedulingPolicy> schedulingPolicies = new HashMap<Integer, SchedulingPolicy>();
				while ( userInfoIterator.hasNext() ) {
					Map.Entry<Integer, ASessionRegistryUserInfo> entry = userInfoIterator.next();
					curNetworkOverlays.put(
							entry.getKey(),
							m_currentSysConfig.OverlayManager.getCurrentOverlay( entry.getKey() ) 
							);
					userIndices.add( entry.getKey() );
					if ( entry.getValue().isMaster() && entry.getValue().getRunningUIAsMaster() ) {
						runningUIAsMaster.add( entry.getKey() );
					}
					if ( entry.getValue().isInputtingCommands() ) {
						inputtingUserIndices.add( entry.getKey() );
					}
					if ( entry.getValue().isMaster() ) {
					    masterUserIndices.add( entry.getKey() );
					}
					schedulingPolicies.put( entry.getKey(), entry.getValue().getSchedulingPolicy() );
				}
				
				Runnable r = new Runnable() {
				    public void run() {
				        try {
				            /*
				             * TODO: used a shared thread pool here
				             */
				            m_performanceOptimizationServerRmiStub.setCurrentSystemConfiguration(
    		                        curSysConfigVersion,
    		                        userIndices,
    		                        masterUserIndices,
    		                        inputtingUserIndices,
    		                        runningUIAsMaster,
    		                        curNetworkOverlays,
    		                        schedulingPolicies
    		                        );
				        }
				        catch ( Exception e ) {
				            ErrorHandlingUtils.logSevereExceptionAndContinue(
				                    "ASessionRegsitry: Error while sending current state to perf report collector",
				                    e
				                    );
				        }
				    }
				};
				( new Thread( r ) ).start();
				
			}
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while sending current state to perf report collector",
                    e
                    );
		}
	}
	
	public void setSystemQuittingFlag(
			boolean flag
			) {
		m_systemQuitting = flag;
		
		if ( m_systemQuitting == true ) {
		    backupCurrentState();
		}
	}
	
	public synchronized boolean setupNewSysConfig(
            int prevSysConfigVersion,
            List<Integer> masterUserIndices,
            HashMap<Integer, Overlay> overlays,
            HashMap<Integer, SchedulingPolicy> schedulingPolicies,
            HashMap<Integer, HashMap<PerformanceParameterType, Double>> observedTransCosts
            ) {
	    try {
    	    if ( prevSysConfigVersion != m_currentSysConfigVersion ) {
    	        return false;
    	    }
    	    
    	    if ( m_systemQuitting ) {
    	        return true;
    	    }
    	    
            if ( m_operationMode == OperationMode.REPLAY ) {
                m_sysConfigChangeReports.add(
                        new ATimeServerReportMessage(
                                MessageType.TS_CONFIGURATION_CHANGE_START,
                                -1,
                                -1,
                                -1,
                                System.nanoTime()
                                )
                        );
            }
            
            boolean pausingInput = false;
            if ( m_currentSysConfig.MasterUserIndices.size() != masterUserIndices.size() ) {
                pausingInput = true;
            }
            else {
                for ( int i = 0; i < m_currentSysConfig.MasterUserIndices.size(); i++ ) {
                    if ( masterUserIndices.contains( m_currentSysConfig.MasterUserIndices.get( i ) ) == false ) {
                        pausingInput = true;
                        break;
                    }
                }
            }
            
            Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> userInfoItr = null;
            
            if ( pausingInput ) {
                userInfoItr = m_currentSysConfig.UserInfos.entrySet().iterator();
                while ( userInfoItr.hasNext() ) {
                    ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                	userInfo.getUserLogger().takeEnterInputToken();
                }
            }
            
            /*
    	     * Create new sys config 
    	     */
            ASessionRegistrySysConfigInfo newSysConfig = ASessionRegistrySysConfigInfo.copy( m_currentSysConfig );
    	    int newSysConfigVersion = m_currentSysConfigVersion + 1;
            newSysConfig.SysConfigVersion = newSysConfigVersion;
            m_sysConfigs.put(
                    newSysConfigVersion,
                    newSysConfig
                    );
            
            userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
            while ( userInfoItr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                newSysConfig.OverlayManager.userLeaving( userInfo.getUserIndex() );
            }
            
            /*
             * TODO:
             * Fix bug. Scheduling policy should be set on a per inputting user basis.
             */
            Iterator<Map.Entry<Integer, SchedulingPolicy>> schedPolItr = schedulingPolicies.entrySet().iterator();
            SchedulingPolicy bestSchedPol = schedPolItr.next().getValue();
            
            userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
            while ( userInfoItr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                if ( masterUserIndices.size() == 1 ) {
                    userInfo.setMasterUserIndex( masterUserIndices.get( 0 ) );
                    newSysConfig.OverlayManager.userJoining(
                            userInfo.getUserIndex(),
                            userInfo.getMasterUserIndex(),
                            userInfo.isInputtingCommands()
                            );
                }
                else {
                    userInfo.setMasterUserIndex( userInfo.getUserIndex() );
                    newSysConfig.OverlayManager.userJoining(
                            userInfo.getUserIndex(),
                            userInfo.getMasterUserIndex(),
                            userInfo.isInputtingCommands()
                            );
                }
                userInfo.setSchedulingPolicy( bestSchedPol );
            }
            
            newSysConfig.MasterUserIndices = new Vector<Integer>();
            for ( int i = 0; i < masterUserIndices.size(); i++ ) {
                newSysConfig.MasterUserIndices.add( masterUserIndices.get( i ) );
            }
            
            if ( overlays != null ) {
                newSysConfig.OverlayManager.setupOverlayForRootUserIndices( overlays );
            }

            Iterator<Map.Entry<Integer, HashMap<PerformanceParameterType, Double>>> observedTransCostsItr = observedTransCosts.entrySet().iterator();
            while ( observedTransCostsItr.hasNext() ) {
                Map.Entry<Integer, HashMap<PerformanceParameterType, Double>> entry = observedTransCostsItr.next();
                int userIndex = entry.getKey();
                HashMap<PerformanceParameterType, Double> observedTransCostsForCurUser = entry.getValue();
                ASessionRegistryUserInfo userInfo = newSysConfig.UserInfos.get( userIndex );
                userInfo.setObservedTransCosts( observedTransCostsForCurUser );
            }

            /*
             * Send new sys config info to loggers
             */
            
            prepareLoggersForNewSysConfig( newSysConfig );
            
            sendSysConfigMappingsToLoggers( newSysConfig );
            
            switchLoggersToNewSysConfigVersion( newSysConfig );
            
            /*
             * Update graphical session registry UI with new sys config mappings
             */
            
            userInfoItr = m_currentSysConfig.UserInfos.entrySet().iterator();
            while ( userInfoItr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                int userIndex = userInfo.getUserIndex();
                unsetMappingsInSessionRegistryUI(
                        userIndex,
                        true,
                        m_currentSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex ),
                        m_currentSysConfig.OverlayManager.getInputSourceMappingsForUser( userIndex ),
                        m_currentSysConfig.OverlayManager.getOutputDestMappingsForUser( userIndex ),
                        m_currentSysConfig.OverlayManager.getOutputSourceMappingsForUser( userIndex ),
                        -1,
                        null,
                        null,
                        m_currentSysConfigVersion
                        );
            }
            userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
            while ( userInfoItr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                int userIndex = userInfo.getUserIndex();
                setMappingsInSessionRegistryUI(
                        userIndex,
                        true,
                        newSysConfig.OverlayManager.getInputDestMappingsForUser( userIndex ),
                        newSysConfig.OverlayManager.getInputSourceMappingsForUser( userIndex ),
                        newSysConfig.OverlayManager.getOutputDestMappingsForUser( userIndex ),
                        newSysConfig.OverlayManager.getOutputSourceMappingsForUser( userIndex ),
                        -1,
                        null,
                        null,
                        newSysConfigVersion
                        );
            }
            
            /*
             * Switch registry to new sys config
             */
            
            if ( pausingInput ) {
                userInfoItr = m_currentSysConfig.UserInfos.entrySet().iterator();
                while ( userInfoItr.hasNext() ) {
                    ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                	userInfo.getUserLogger().releaseEnterInputToken();
                }
            }
            
            m_currentSysConfig = newSysConfig;
            m_currentSysConfigVersion = newSysConfigVersion;
            
            sendCurrentStateToPerfCollector();
	    }
	    catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while trying to setup new sys config",
                    e
                    );
	    }
	    
	    return true;
	}
	
	private void prepareLoggersForNewSysConfig(
            ASessionRegistrySysConfigInfo newSysConfig
            ) {
	    try {
            ProcessingArchitectureType procArch = ProcessingArchitectureType.REPLICATED;
            if ( newSysConfig.MasterUserIndices.size() == 1 ) {
                procArch = ProcessingArchitectureType.CENTRALIZED;
            }

            Logger masterLogger = null;
            if ( m_replayCommandsToLatecomers && m_currentSysConfig.MasterUserIndices.size() > 0 ) {
                int randIndex = m_rand.nextInt( m_currentSysConfig.MasterUserIndices.size() );
                int indexOfACurrentMaster = -1;
                for ( int i = 0; i < m_currentSysConfig.MasterUserIndices.size(); i++ ) {
                    int nextIndex = (randIndex + i ) % m_currentSysConfig.MasterUserIndices.size();
                    if ( m_currentSysConfig.UserInfos.get( nextIndex ).isInputtingCommands() ) {
                        indexOfACurrentMaster = m_currentSysConfig.MasterUserIndices.elementAt( nextIndex );
                        break;
                    }
                }
                masterLogger = m_currentSysConfig.UserInfos.get( indexOfACurrentMaster ).getUserLogger();
            }

            Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> itr =
                newSysConfig.UserInfos.entrySet().iterator();
            while ( itr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = itr.next().getValue();
                if ( userInfo.isFakeUser() ) {
                    continue;
                }
                
                userInfo.getUserLogger().prepareForNewSysConfigVersion(
                        newSysConfig.SysConfigVersion,
                        procArch == ProcessingArchitectureType.CENTRALIZED ? newSysConfig.MasterUserIndices.get( 0 ) : userInfo.getUserIndex(),
                        masterLogger
                        );
            }
	    }
	    catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while preparing loggers for new sys config",
                    e
                    );
	    }
	}
	
	private void sendSysConfigMappingsToLoggers(
	        ASessionRegistrySysConfigInfo newSysConfig
	        ) {
	    try {
    	    Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> itr =
    	        newSysConfig.UserInfos.entrySet().iterator();
            while ( itr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = itr.next().getValue();
                if ( userInfo.isFakeUser() ) {
                    continue;
                }

                Vector<AnOverlayMapping> inputDests = newSysConfig.OverlayManager.getInputDestMappingsForUser( userInfo.getUserIndex() );
                populateMappingsWithLoggerInfo( inputDests );
                Vector<AnOverlayMapping> inputSources = newSysConfig.OverlayManager.getInputSourceMappingsForUser( userInfo.getUserIndex() );
                populateMappingsWithLoggerInfo( inputSources );
                Vector<AnOverlayMapping> outputDests = newSysConfig.OverlayManager.getOutputDestMappingsForUser( userInfo.getUserIndex() );
                populateMappingsWithLoggerInfo( outputDests );
                Vector<AnOverlayMapping> outputSources = newSysConfig.OverlayManager.getOutputSourceMappingsForUser( userInfo.getUserIndex() );
                populateMappingsWithLoggerInfo( outputSources );
                userInfo.getUserLogger().setMappings(
                        userInfo.getMasterUserIndex(),
                        inputDests,
                        inputSources,
                        outputDests,
                        outputSources,
                        newSysConfig.SysConfigVersion
                        );
            }
	    }
	    catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while sending new sys config to loggers",
                    e
                    );
	    }
	}
	
	private void switchLoggersToNewSysConfigVersion(
	        ASessionRegistrySysConfigInfo newSysConfig
	        ) {
	    try {
	        Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
            while ( userInfoItr.hasNext() ) {
                Map.Entry<Integer, ASessionRegistryUserInfo> entry = userInfoItr.next();
                ASessionRegistryUserInfo userInfo = entry.getValue();
                if ( userInfo.isFakeUser() ) {
                    continue;
                }

                userInfo.getUserLogger().setSchedulingPolicy(
                        userInfo.getSchedulingPolicy(),
                        newSysConfig.SysConfigVersion
                        );
                userInfo.getUserLogger().setObservedTransCost(
                        userInfo.getAllObservedTransCosts(),
                        newSysConfig.SysConfigVersion
                        );
            }
        
            for ( int i = 0; i < newSysConfig.MasterUserIndices.size(); i++ ) {
                ASessionRegistryUserInfo userInfo = newSysConfig.UserInfos.get( newSysConfig.MasterUserIndices.get( i ) );
                if ( userInfo.isFakeUser() ) {
                    continue;
                }

                userInfo.getUserLogger().switchToSysConfigVersion( newSysConfig.SysConfigVersion );
            }
            userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
            while ( userInfoItr.hasNext() ) {
                ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
                if ( userInfo.isFakeUser() ) {
                    continue;
                }

                if ( newSysConfig.MasterUserIndices.contains( userInfo.getUserIndex() ) ) {
                    continue;
                }
                
                userInfo.getUserLogger().switchToSysConfigVersion( newSysConfig.SysConfigVersion );
            }
            
            if ( m_operationMode == OperationMode.REPLAY ) {
                m_sysConfigChangeReports.add(
                        new ATimeServerReportMessage(
                                MessageType.TS_CONFIGURATION_CHANGE_END,
                                -1,
                                -1,
                                -1,
                                System.nanoTime()
                                )
                        );
            }
	    }
	    catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while switching loggers to new sys config",
                    e
                    );
	    }
	}
	
	public void signalStartOfExperiment() {
	    m_startOfExperimentSignalled = true;
	}
	
	private void backupCurrentState() {
	    m_currentSysConfig = ASessionRegistrySysConfigInfo.copy( m_currentSysConfig );
	}
	
	public List<TimeServerReportMessage> getTimeServerReports() {
	    return m_sysConfigChangeReports;
	}
	
	/*
	 * Handling user actions in UI
	 */
	public void uiUpdateDelayBetweenUsers(
    		int sourceUser,
    		int destUser,
    		int delay
    		) {
	}
	
    public static void main( String args[] ) {

        ConfigParamProcessor mainCpp = AMainConfigParamProcessor.getInstance(
        		args,
        		true
        		);        
        String configFilePath = mainCpp.getStringParam( Parameters.CUSTOM_CONFIGURATION_FILE );
        AMainConfigParamProcessor.overrideValuesByThoseSpecifiedInSource(
        		mainCpp,
		        ASettingsFileConfigParamProcessor.getInstance(
		                configFilePath,
		                true
		                )
		        );
    	
    	final String registryHost = mainCpp.getStringParam( Parameters.RMI_REGISTRY_HOST );
    	final int registryPort = mainCpp.getIntParam( Parameters.RMI_REGISTRY_PORT );
    	final OperationMode operationMode = OperationMode.valueOf( mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
    	
    	Runnable r = new Runnable() {
    		public void run() {
		        LocalSessionRegistry sr = new ASessionRegistry(
		        		registryHost,
		        		registryPort
		        		);
		        sr.startRegistry();
		        
		        if ( operationMode == OperationMode.RECORD ) {
		        	sr.startRecordingManager();
		        }
    		}
    	};
    		
    	(new Thread( r ) ).start();
    }
}
