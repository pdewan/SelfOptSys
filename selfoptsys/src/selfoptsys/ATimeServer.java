package selfoptsys;

import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;
import commonutils.misc.*;
import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public class ATimeServer 
	implements TimeServer, LocalTimeServer {
    
    private int m_numUsers = 0;
    
    private BlockingQueue<Object> m_firstReadyReports = null;
    private BlockingQueue<Object> m_secondReadyReports = null;
    private boolean m_usersWillReportReadySecondTime = false;
    private int m_numUsersLeftToReportDone = 0;
    
    private OperationMode m_operationMode;
    
    private int[] m_userTurns = null;
    
    private LocalSessionRegistry m_sessionRegistry = null;
    
    private BlockingQueue<Object> m_quitBB = null;
    
    private TimeServer m_timeServerStub = null;
    
    private BlockingQueue<Object> m_startBB = null;
    
    private ConfigParamProcessor m_mainCpp;
    
    private String m_sessionRegistryRegistryHost = null;
    private int m_sessionRegistryRegistryPort = 0;
    private String m_outputFile = null;
    
    Map<Integer, ATimeServerUserInfo> m_userInfos;
    
    protected boolean m_runPerfOptimizer = false;
    
    protected List<Integer> m_timesAtWhichUsersJoin;
    protected List<List<Integer>> m_usersWhoJoinAtTime;
    
    protected boolean m_experimentStarted = false;
    
    protected List<Long> m_configurationChangeStartTimes;
    protected List<Long> m_configurationChangeEndTimes;
    
    protected List<ResultsPrinter> m_resultsPrinters;
    
    protected String m_loggingLevel = "DEBUG";
    
    protected ClockSyncServer m_clockSyncServer;

    public ATimeServer(
    		String sessionRegistryRegistryHost,
    		int sessionRegistryRegistryPort,
    		String outputFile,
    		BlockingQueue<Object> quitBB
            ) {
    	m_mainCpp = AMainConfigParamProcessor.getInstance();
    	
        m_loggingLevel = m_mainCpp.getStringParam( Parameters.LOGGING_LEVEL );
        LoggingUtils.setLoggingLevel( m_loggingLevel );

        m_sessionRegistryRegistryHost = sessionRegistryRegistryHost;
    	m_sessionRegistryRegistryPort = sessionRegistryRegistryPort;
    	m_outputFile = outputFile;
    	
        m_quitBB = quitBB;
        m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
        
        m_usersWillReportReadySecondTime = m_mainCpp.getBooleanParam( Parameters.LOGGABLE_WILL_REPORT_READY_TWICE );
        
        ConfigUtils.setProcessorCoreAffinityMask();
        
        if ( m_operationMode == OperationMode.REPLAY ) {
            m_numUsers = m_mainCpp.getIntParam( Parameters.NUM_USERS );
            m_firstReadyReports = new ArrayBlockingQueue<Object>( m_numUsers );
            m_secondReadyReports = new ArrayBlockingQueue<Object>( m_numUsers );
            m_userTurns = m_mainCpp.getIntArrayParam( Parameters.USER_TURNS );
            
            m_startBB = new ArrayBlockingQueue<Object>( 1 );
            ( new TimeServerStartExperimentReplayThread( m_startBB, this ) ).start();
        }
        
        m_resultsPrinters = new LinkedList<ResultsPrinter>();
        
        m_runPerfOptimizer = m_mainCpp.getBooleanParam( Parameters.OPTIMIZE_PERFORMANCE );
        
        m_timesAtWhichUsersJoin = MiscUtils.getSortedTimesAtWhichUsersJoin(
                m_mainCpp.getIntArrayParam( Parameters.TIMES_AT_WHICH_USERS_JOIN )
                );
        m_usersWhoJoinAtTime = MiscUtils.getUserJoinsByTimeSortedByTime(
                m_mainCpp.getIntArrayParam( Parameters.TIMES_AT_WHICH_USERS_JOIN )
                );
        
        m_configurationChangeStartTimes = new LinkedList<Long>();
        m_configurationChangeEndTimes = new LinkedList<Long>();
        
        m_clockSyncServer = new AClockSyncServer();
        
    }
    
    public void startTimeServer() {
        try {
            System.err.println( "TimeServer: Starting Time Server ..." );
            System.err.println( "TimeServer: Setting up registry ..." );
            m_sessionRegistry = new ASessionRegistry(
            		m_sessionRegistryRegistryHost,
            		m_sessionRegistryRegistryPort
            		);
            m_sessionRegistry.startRegistry();
            
            m_timeServerStub = 
                (TimeServer) UnicastRemoteObject.exportObject( (TimeServer) this, 0 );
            m_sessionRegistry.registerTimeServer( m_timeServerStub );
            System.err.println( "TimeServer: Registry running." );
            
            try {
                m_clockSyncServer.start();
            }
            catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "APerformanceOptimizationServer::start(): Exception occurred while starting clock sync server",
                        e
                        );
            }

            if ( m_operationMode == OperationMode.REPLAY ) {
                m_startBB.put( new Integer( 0 ) );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "TimeServer: Could not start time server",
                    e
                    );
        }
        
    }
    
    public void startExperimentReplay() {
        try {
        	m_sessionRegistry.setSystemQuittingFlag( false );
        	
            m_sessionRegistry.reset();
            
            m_firstReadyReports = new ArrayBlockingQueue<Object>( m_numUsers );
            m_secondReadyReports = new ArrayBlockingQueue<Object>( m_numUsers );
        	m_userInfos = new Hashtable<Integer, ATimeServerUserInfo>();
        	
            if ( m_operationMode == OperationMode.REPLAY ) {
                for ( int i = 0; i < m_numUsers; i++ ) {
                    System.err.print( "\r Waiting for " + ( m_numUsers - i ) + " to report ready first time!" );
                	m_firstReadyReports.take();
                }
                System.err.print( "\r All users reported ready first time!" );
                System.err.println( "" );
                
                if ( m_usersWillReportReadySecondTime ) {
                    for ( int i = 0; i < m_numUsers; i++ ) {
                        System.err.print( "\r Waiting for " + ( m_numUsers - i ) + " to report ready second time!" );
                    	m_secondReadyReports.take();
                    }
                    System.err.print( "\r All reported ready second time!" );
                }
                System.err.println( "" );
            }
    
            if ( m_runPerfOptimizer ) {
                m_sessionRegistry.startPerformanceOptimizationServer();
            }
            
            m_experimentStarted = true;
            
            int indexOfFirstInputUser = m_userTurns[0];
            Thread.sleep(2000); // hack: some loggables may not be ready fully
            Iterator<Map.Entry<Integer, ATimeServerUserInfo>> itr = m_userInfos.entrySet().iterator();
            while ( itr.hasNext() ) {
                Map.Entry<Integer, ATimeServerUserInfo> entry = itr.next();
                if ( entry.getKey() != indexOfFirstInputUser ) {
                    entry.getValue().getReplayClient().beginExperiment();
                }
            }
            m_userInfos.get( indexOfFirstInputUser ).getReplayClient().beginExperiment();
            
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "TimeServer: Could not start experiment replay",
                    e
                    );
        }
    }
    
    public void joinUsersAsNecessary() {
        
        try {
            int prevJoinTime = 0;
            for ( int i = 0; i < m_timesAtWhichUsersJoin.size(); i++ ) {
                int nextJoinTime = m_timesAtWhichUsersJoin.get( i );
                int sleepTime = nextJoinTime - prevJoinTime;
                Thread.sleep( sleepTime );
                List<Integer> joiningUserIndices = m_usersWhoJoinAtTime.get( i );
                for ( int j = 0; j < joiningUserIndices.size(); j++ ) {
                    m_userInfos.get( joiningUserIndices.get( j ) ).getReplayClient().joinByCommandFromReplayServer();
                }
                prevJoinTime = nextJoinTime;
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATimeServer: Error while in join users loop",
                    e
                    );
        }
        
    }
    
    public synchronized void reportReadyFirstTime(
            int userIndex,
            ReplayClient replayClient,
            ReplayUserInfo replayUserInfo
            ) {
        
        ATimeServerUserInfo newUserInfo = m_userInfos.get( userIndex );
        if ( newUserInfo != null ) {
            return;
        }
        
        if ( replayUserInfo.getMeasurePerformance() ) {
            m_numUsersLeftToReportDone++;
        }
        
        ATimeServerUserInfo userInfo = new ATimeServerUserInfo(
        		userIndex,
        		replayClient,
        		replayUserInfo
        		);
        m_userInfos.put(
        		userIndex,
        		userInfo
        		);

        try {
        	specialReportReadyFirstTime( userIndex );
        	m_firstReadyReports.put( new Integer( 0 ) );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "TimeServer: Error handling logger reporting ready the first time",
                    e
                    );
        }

    }
    
    protected void specialReportReadyFirstTime(
    		int userIndex
    		) {
    	
    }

    public synchronized void reportReadySecondTime(
    		int userIndex
    		) {
    	
        try {
        	specialReportReadySecondTime( userIndex );
        	m_secondReadyReports.put( new Integer( 0 ) );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "TimeServer: Error handling logger reporting ready the second time",
                    e
                    );
        }

    }
    
    protected void specialReportReadySecondTime(
    		int userIndex
    		) {
    }
    
    public synchronized void reportDone(
            int userIndex
            ) {
        m_numUsersLeftToReportDone--;
        if ( m_numUsersLeftToReportDone == 0 ) {
            finishExperimentReplay();
        }
    }
    
    private void syncClockWithReplayClients() {
        try {
            Iterator<Map.Entry<Integer, ATimeServerUserInfo>> userInfoIterator = m_userInfos.entrySet().iterator();
            while ( userInfoIterator.hasNext() ) {
                ReplayClient replayClient = userInfoIterator.next().getValue().getReplayClient();
                replayClient.syncClockWithReplayServer();
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATimeServer::syncClockWithReplayClients: Exception occurred while syncing local clock with clocks on replay clients",
                    e
                    );
        }
    }
    
    private void finishExperimentReplay() {
        try {
            m_experimentStarted = false;

            syncClockWithReplayClients();
            
        	m_sessionRegistry.setSystemQuittingFlag( true );

        	Iterator<Map.Entry<Integer, ATimeServerUserInfo>> userInfoIterator = m_userInfos.entrySet().iterator();
        	while ( userInfoIterator.hasNext() ) {
        		Map.Entry<Integer, ATimeServerUserInfo> entry = userInfoIterator.next();
        		
                ATimeServerUserInfo userInfo = entry.getValue();
                List<TimeServerReportMessage> loggerTimeServerReports = userInfo.getReplayClient().getReplayServerReports();
                for ( int i = 0; i < loggerTimeServerReports.size(); i++ ) {
                    int userIndex = entry.getKey();
                    if ( loggerTimeServerReports.get( i ).getMessageType() == MessageType.TS_INPUT_ENTERED ) {
                        userInfo.addInputEnteredTime(
                                loggerTimeServerReports.get( i ).getSeqId(),
                                loggerTimeServerReports.get( i ).getEventTime() + m_clockSyncServer.getClockSkews().get( userIndex )
                                );
                    }
                    else if ( loggerTimeServerReports.get( i ).getMessageType() == MessageType.TS_OUTPUT_PROCESSED ) {
                        userInfo.addOutputProcessedTime(
                                loggerTimeServerReports.get( i ).getCmdSourceUserIndex(),
                                loggerTimeServerReports.get( i ).getSeqId(),
                                loggerTimeServerReports.get( i ).getEventTime() + m_clockSyncServer.getClockSkews().get( userIndex )
                                );
                    }
                }
                
                userInfo.getReplayClient().prepareToQuit();
        	}
        	
        	userInfoIterator = m_userInfos.entrySet().iterator();
            while ( userInfoIterator.hasNext() ) {
                Map.Entry<Integer, ATimeServerUserInfo> entry = userInfoIterator.next();
                ATimeServerUserInfo userInfo = entry.getValue();
                userInfo.getReplayClient().quit();
            }
        	
            int firstInputtingUserIndex = m_userTurns[ 0 ];
            long firstInputEnteredTime = m_userInfos.get( firstInputtingUserIndex ).getInputEnteredTime( 0 );
            List<TimeServerReportMessage> sessionRegistryTimeServerReports = m_sessionRegistry.getTimeServerReports();
            for ( int i = 0; i < sessionRegistryTimeServerReports.size(); i++ ) {
                if ( sessionRegistryTimeServerReports.get( i ).getMessageType() == MessageType.TS_CONFIGURATION_CHANGE_START ) {
                    if ( sessionRegistryTimeServerReports.get( i ).getEventTime() > firstInputEnteredTime ) {
                        m_configurationChangeStartTimes.add(
                                sessionRegistryTimeServerReports.get( i ).getEventTime()
                                );
                    }
                }
                else if ( sessionRegistryTimeServerReports.get( i ).getMessageType() == MessageType.TS_CONFIGURATION_CHANGE_END ) {
                    if ( sessionRegistryTimeServerReports.get( i ).getEventTime() > firstInputEnteredTime ) {
                        m_configurationChangeEndTimes.add(
                                sessionRegistryTimeServerReports.get( i ).getEventTime()
                                );
                    }
                }
            }
            
            printSimResults();
            
            reportExperimentHasFinished();
            
            m_sessionRegistry.quit();
            
            m_quitBB.put( new Integer( 0 ) );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "TimeServer: Could not finish experiment",
                    e
                    );
        }
    }
    
    protected void reportExperimentHasFinished() {
    	
    }
    
    private void printSimResults() {
    	
    	for ( int i = 0; i < m_resultsPrinters.size(); i++ ) {
    		m_resultsPrinters.get( i ).printTimingInformation(
    				m_outputFile,
    				m_userTurns,
    				m_userInfos,
    				m_configurationChangeStartTimes,
    				m_configurationChangeEndTimes
    				);
    	}
    	
    }
    
    public void registerResultsPrinter(
    		ResultsPrinter resultsPrinter
    		) {
    	m_resultsPrinters.add( resultsPrinter );
    }
    
    public ClockSyncServer getClockSyncServer() {
        ClockSyncServer clockSyncSever = null;
        
        try {
            clockSyncSever = m_clockSyncServer.getRmiStub();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATimeServer::getClockSyncServer: Error while obtaining clock sync server rmi stub",
                    e
                    );
        }
        
        return clockSyncSever;
    }
    
    public static void main( String[] args ) {
    	
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
    	
        String outputDirectory = mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY );
    	String outputFile = mainCpp.getStringParam( Parameters.TIME_SERVER_OUTPUT_FILE );
    	String registryHost = mainCpp.getStringParam( Parameters.RMI_REGISTRY_HOST );
    	int registryPort = mainCpp.getIntParam( Parameters.RMI_REGISTRY_PORT );
    	
    	BlockingQueue<Object> quitBB = new ArrayBlockingQueue<Object>( 1 );
        
        LocalTimeServer timeServer = new ATimeServer(
        		registryHost,
        		registryPort,
        		outputDirectory + "\\" + outputFile,
        		quitBB
        		);
        
        timeServer.registerResultsPrinter( new ADefaultResultsPrinter() );
        timeServer.registerResultsPrinter( new ADetailedDefaultResultsPrinter() );
        
        timeServer.startTimeServer();
        
        ( new AQuitThread(
        		quitBB,
        		"TimeServer quitting!",
        		1,
        		50
        		) ).start();
    }
}

class TimeServerStartExperimentReplayThread 
    extends ASelfOptArchThread {
    
    private BlockingQueue<Object> m_startBB = null;
    private LocalTimeServer m_ts = null;
    
    public TimeServerStartExperimentReplayThread(
    		BlockingQueue<Object> startBB,
            LocalTimeServer ts
            ) {
        m_startBB = startBB;
        m_ts = ts;

        int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
        coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );
    }
    
    public void run() {
        super.run();
        
        for (;;) {
            try {
                m_startBB.take();
                System.err.println( "TimeServerStartExperimentReplayThread: starting experiment replay" );
                m_ts.startExperimentReplay();
                
                m_ts.joinUsersAsNecessary();
            }
            catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "TimeServerStartExperimentReplayThread: Exception occurred while running",
                        e
                        );
            }
        }
    }
}