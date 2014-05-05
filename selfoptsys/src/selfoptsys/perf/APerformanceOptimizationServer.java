package selfoptsys.perf;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import commonutils.basic.*;
import commonutils.basic2.*;
import commonutils.config.*;
import commonutils.scheduling.*;

import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;
import selfoptsys.network.*;

public class APerformanceOptimizationServer 
	implements PerformanceOptimizationServer, LocalMessageDest {

    protected ConfigParamProcessor m_mainCpp;
    
    protected SessionRegistry m_sessionRegistry;
    protected String m_registryHost;
    protected int m_registryPort;

    protected boolean m_collectPerformanceData = false;
    protected boolean m_optimizePerformance = false;
    
    protected ClockSyncServer m_clockSyncServer;
    
    protected MessageDest m_messageDest;
    protected MessageDest m_messageDestRmiStub;
    
    protected double m_lazyDelay;
    /*
     * TODO: We should have a scheduling quantum for each user as they may be using
     * different operating systems. Use a programmatic way of determining the quantum
     * and then store it for each user.
     */
    protected double m_schedulingQuantum;
    protected double m_significantDiffInLocalRespTimes;
    protected double m_significantDiffInRemoteRespTimes;
    
    protected NetworkLatencyCollectorServer m_networkLatencyCollectorServer;
    protected boolean m_simulatingNetworkLatencies = false;
    
    protected boolean m_receivedFirstReport = false;
    protected int m_minNumReportsRequiredToEstimateCosts = -1;
    protected int m_numCommandsFullyProcessedSinceLastOptimization = 0;
    protected int m_numCommandsFullyProcessedSinceLastSysConfigChange = 0;

    protected Map<Integer, PerformanceOptimizationUserInfo> m_performanceOptimizationUserInfos;
    protected Map<ComputerInfo, ComputerPerformanceData> m_computerPerformanceDataCollection;
    
    protected double m_curThinkTimeEstimate;
    protected Map<Integer, List<Double>> m_thinkTimesForUsers;
    protected List<Double> m_thinkTimes;

    protected SysConfigOptimizer m_sysConfigOptimizer;
    protected int m_latestSystemConfigurationVersion;
    protected Map<Integer, SysConfig> m_systemConfigurations;
    
    protected Vector<Integer> m_usersWhoWillReport;
    protected Map<String, APerformanceOptimizationReportsForCommandProcessor> m_reportsForCommandProcessors;
    
    protected AtomicBoolean m_optimizationInProgressLock = new AtomicBoolean();
    
    protected String m_parameterEstimatesFileOut;
    protected String m_thinkTimeEstimateFileOut;

    public APerformanceOptimizationServer(
            String registryHost,
            int registryPort
            ) {
        m_mainCpp = AMainConfigParamProcessor.getInstance();
        
        m_registryHost = registryHost;
        m_registryPort = registryPort;
        
        m_clockSyncServer = new AClockSyncServer();
        
        m_schedulingQuantum = m_mainCpp.getIntParam( Parameters.SCHEDULING_QUANTUM );
        m_lazyDelay = m_mainCpp.getIntParam( Parameters.LAZY_DELAY );
        m_significantDiffInLocalRespTimes = m_mainCpp.getIntParam( Parameters.SIGNIFICANT_DIFFERENCE_IN_LOCAL_RESPONSE_TIMES );
        m_significantDiffInRemoteRespTimes = m_mainCpp.getIntParam( Parameters.SIGNIFICANT_DIFFERENCE_IN_REMOTE_RESPONSE_TIMES );
        
        m_simulatingNetworkLatencies = m_mainCpp.getBooleanParam( Parameters.SIMULATING_NETWORK_LATENCIES );
        if ( m_simulatingNetworkLatencies == false ) {
            m_networkLatencyCollectorServer = new ANetworkLatencyCollectorServer();
        }
        
        m_optimizePerformance = m_mainCpp.getBooleanParam( Parameters.OPTIMIZE_PERFORMANCE );
        
        boolean collectPerfDataForAllUsers = m_mainCpp.getBooleanParam( Parameters.COLLECT_PERFORMANCE_DATA_FOR_ALL_USERS );
        int[] usersForWhoToCollectPerfData = MiscUtils.getSpecifiedUserIndices(
                m_mainCpp.getStringArrayParam( Parameters.USERS_FOR_WHO_TO_COLLECT_PERFORMANCE_DATA )
                );
        if ( collectPerfDataForAllUsers || 
                ( usersForWhoToCollectPerfData != null && usersForWhoToCollectPerfData.length > 0 ) ) {
            m_collectPerformanceData = true;
        }
        
        m_minNumReportsRequiredToEstimateCosts = m_mainCpp.getIntParam( Parameters.MIN_NUM_REPORTS_REQUIRED_TO_ESTIMATE_COSTS );

        m_performanceOptimizationUserInfos = new Hashtable<Integer, PerformanceOptimizationUserInfo>();
        m_computerPerformanceDataCollection = new Hashtable<ComputerInfo, ComputerPerformanceData>();
        
        m_curThinkTimeEstimate = -1;
        m_thinkTimes = new LinkedList<Double>();
        m_thinkTimesForUsers = new Hashtable<Integer, List<Double>>();
        
        int[] supportedCentralizedArchitectureMasters = m_mainCpp.getIntArrayParam( Parameters.SUPPORTED_CENTRALIZED_ARCHITECTURE_MASTERS );
        List<Integer> supportedCentralizedArchitectureMastersList = new LinkedList<Integer>();
        for ( int i = 0; i < supportedCentralizedArchitectureMasters.length; i++ ) {
            supportedCentralizedArchitectureMastersList.add( supportedCentralizedArchitectureMasters[ i ] );
        }
        m_sysConfigOptimizer = ASysConfigOptimizerFactorySelector.getFactory().getSysConfigOptimizer(
                m_mainCpp.getDoubleParam( Parameters.SCHEDULING_QUANTUM ),
                m_mainCpp.getDoubleParam( Parameters.LAZY_DELAY ),
                m_mainCpp.getBooleanParam( Parameters.SUPPORT_CHANGING_PROCESSING_ARCHITECTURE ),
                m_mainCpp.getBooleanParam( Parameters.SUPPORT_REPLICATED_ARCHITECTURE ),
                m_mainCpp.getBooleanParam( Parameters.SUPPORT_ALL_CENTRALIZED_ARCHITECTURES ),
                supportedCentralizedArchitectureMastersList,
                m_mainCpp.getBooleanParam( Parameters.SUPPORT_MULTICAST_COMMUNICATION ),
                m_mainCpp.getBooleanParam( Parameters.SUPPORT_SCHEDULING_POLICY_CHANGES )
                );
        
        m_systemConfigurations = new Hashtable<Integer, SysConfig>();
        m_latestSystemConfigurationVersion = -1;
        
        m_usersWhoWillReport = new Vector<Integer>();
        m_reportsForCommandProcessors = new Hashtable<String, APerformanceOptimizationReportsForCommandProcessor>();

        m_optimizationInProgressLock.set( false );
        
        m_parameterEstimatesFileOut = m_mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY ) + "\\" +
            m_mainCpp.getStringParam( Parameters.COST_ESTIMATES_FILE_OUT );
        m_thinkTimeEstimateFileOut = m_mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY ) + "\\" +
            m_mainCpp.getStringParam( Parameters.THINK_TIME_ESTIMATE_FILE_OUT );
    }
    
    public void init() {
        try {
            m_messageDest = new ATCPMessageDest(
                    (LocalMessageDest) this,
                    WindowsThreadPriority.NORMAL,
                    WindowsThreadPriority.HIGHEST,
                    new ANonBlockingTCPListeningThreadFactory()
                    );
            ( (AMessageDest) m_messageDest ).start();
            m_messageDestRmiStub = m_messageDest.getRmiStub();
            
            PerformanceOptimizationServer perfReportCollectorRMIStub = 
                (PerformanceOptimizationServer) UnicastRemoteObject.exportObject( (PerformanceOptimizationServer) this, 0 );
            
            Registry registry = LocateRegistry.getRegistry(
                    m_registryHost,
                    m_registryPort
                    );
            m_sessionRegistry = (SessionRegistry) registry.lookup( "SessionRegistry" );
            m_sessionRegistry.registerPerformanceOptimizationServer( perfReportCollectorRMIStub );
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerformanceOptimizationServer::init(): Exception occurred while registering with the session registry",
                    e
                    );
        }
    }
    
    public MessageDest getMessageDest() {
        return m_messageDestRmiStub;
    }
    
    public ClockSyncServer getClockSyncServer() {
        ClockSyncServer rmiStub = null;
        
        try {
            rmiStub = m_clockSyncServer.getRmiStub();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerformanceOptimizationServer::getClockSyncServer(): An exception occurred while trying to get the rmi stub from clock sync server",
                    e
                    );
        }
        
        return rmiStub;
    }
    
    public void begin() {
        try {
            m_clockSyncServer.start();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerformanceOptimizationServer::start(): Exception occurred while starting clock sync server",
                    e
                    );
        }
    }
    
    public void quit() {
        try {
            m_clockSyncServer.quit();
            m_networkLatencyCollectorServer.quit();
            
            estimateParameterValues();
            AComputerPerformanceData.savePerformanceParameterEstimates(
                    m_parameterEstimatesFileOut,
                    m_computerPerformanceDataCollection
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerformanceOptimizationServer::stop(): Exception occurred while stopping",
                    e
                    );
        }
    }
    
	public synchronized void registerUser(
			PerformanceOptimizationUserInfo performanceOptimizationUserInfo
			) {
	    
        int userIndex = performanceOptimizationUserInfo.getUserIndex(); 
	    ComputerInfo userComputerInfo = performanceOptimizationUserInfo.getComputerInfo();
	    
	    m_performanceOptimizationUserInfos.put(
	            userIndex,
	            performanceOptimizationUserInfo
	            );
	    
	    if ( m_computerPerformanceDataCollection.get( userComputerInfo ) == null ) {
	        m_computerPerformanceDataCollection.put(
	                userComputerInfo,
	                new AComputerPerformanceData( userComputerInfo )
                    );
	    }
	    
	    if ( m_thinkTimesForUsers.get( userIndex ) == null ) {
	        m_thinkTimesForUsers.put(
	                userIndex,
	                new LinkedList<Double>()
	                );
	    }
	    
	    m_networkLatencyCollectorServer.addUserHost(
	            performanceOptimizationUserInfo.getUserIndex(),
	            performanceOptimizationUserInfo.getNetworkLatencyCollectorClient(),
	            performanceOptimizationUserInfo.getHost()
	            );
	    
	    if ( performanceOptimizationUserInfo.getCollectPerformanceData() ) {
	        m_usersWhoWillReport.add( userIndex );
	    }
	    
	    optimizeIfPossible();
	}
	
	public synchronized void unregisterUser(
	        int userIndex
	        ) {
	    m_performanceOptimizationUserInfos.remove( userIndex );
	    m_networkLatencyCollectorServer.removeUserHost( userIndex );
	    m_thinkTimesForUsers.remove( userIndex );
	    m_usersWhoWillReport.removeElement( userIndex );
	    
	    optimizeIfPossible();
	}
	
	public void setCurrentSystemConfiguration(
            int systemConfigurationVersion,
            List<Integer> userIndices,
            List<Integer> masterUserIndices,
            Vector<Integer> inputtingUserIndices,
            Vector<Integer> mastersRunningUI,
            HashMap<Integer, Overlay> networkOverlays,
            HashMap<Integer, SchedulingPolicy> schedulingPolicies
            ) {
        SysConfig newSystemConfiguration = null;
        
        resetForNextOptimizationAttempt();
        
        SysConfig sysConfig = m_systemConfigurations.get( systemConfigurationVersion - 1 );
        if ( sysConfig != null ) {
            newSystemConfiguration = ASysConfig.deepCopy( sysConfig );
            
            newSystemConfiguration.setSystemConfigurationVersion( systemConfigurationVersion );
            
            if ( userIndices != null ) {
                newSystemConfiguration.setUserIndices( userIndices );
            }
            if ( masterUserIndices != null ) {
                newSystemConfiguration.setMasterUserIndices( masterUserIndices );
            }
            if ( inputtingUserIndices != null ) {
                newSystemConfiguration.setInputtingUserIndices( inputtingUserIndices );
            }
            if ( mastersRunningUI != null ) {
                newSystemConfiguration.setMastersRunningUI( mastersRunningUI );
            }
            if ( networkOverlays != null ) {
                newSystemConfiguration.setNetworkOverlays( networkOverlays );
            }
            if ( schedulingPolicies != null ) {
                newSystemConfiguration.setSchedulingPolicies( schedulingPolicies );
            }

        }
        else {
            newSystemConfiguration = new ASysConfig(
                    systemConfigurationVersion,
                    userIndices,
                    masterUserIndices,
                    inputtingUserIndices,
                    mastersRunningUI,
                    networkOverlays,
                    schedulingPolicies
                    );
        }
        
        m_systemConfigurations.put(
                systemConfigurationVersion,
                newSystemConfiguration
                );
        m_latestSystemConfigurationVersion = systemConfigurationVersion;
	}

    public synchronized void receiveMessage(
            Message msg
            ) {
        
        PerformanceOptimizationReportMessage perfReportMsg = (PerformanceOptimizationReportMessage) msg;
        APerformanceOptimizationReport performanceOptimizationReport = perfReportMsg.getPerformanceOptimizationReport();
        
        if ( performanceOptimizationReport.UserIndex == performanceOptimizationReport.CmdSourceUserIndex ) {
            if ( m_simulatingNetworkLatencies == false && m_receivedFirstReport == false ) {
                m_receivedFirstReport = true;
                ( (ASelfOptArchThread) m_networkLatencyCollectorServer ).start();
            }
        }
        
        if ( m_collectPerformanceData ) {
            processPerformanceOptimizationReportForThinkTime( performanceOptimizationReport );
            processPerformanceOptimizationReportForComputerPerformanceData( performanceOptimizationReport );
        }
        
        if ( m_numCommandsFullyProcessedSinceLastOptimization >= m_minNumReportsRequiredToEstimateCosts ) {
            estimateParameterValues();
            optimizeIfPossible();
            m_numCommandsFullyProcessedSinceLastOptimization = 0;
        }
    }
    
    private void processPerformanceOptimizationReportForThinkTime(
            APerformanceOptimizationReport performanceOptimizationReport
            ) {
        if ( performanceOptimizationReport.ThinkTime == 0 ) {
            return;
        }

        int userIndex = performanceOptimizationReport.UserIndex;
        m_thinkTimes.add( performanceOptimizationReport.ThinkTime );
        m_thinkTimesForUsers.get( userIndex ).add( performanceOptimizationReport.ThinkTime );
    }
    
    private void processPerformanceOptimizationReportForComputerPerformanceData(
            APerformanceOptimizationReport performanceOptimizationReport
            ) {
        
        int reportingUserIndex = performanceOptimizationReport.UserIndex;
        int reportSysConfigVersion = performanceOptimizationReport.SysConfigVersion;

        SchedulingPolicy reportSchedulingPolicy = 
            m_systemConfigurations.get( reportSysConfigVersion ).getSchedulingPolicies().get( reportingUserIndex );
        if ( reportSchedulingPolicy == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
            /*
             * TODO: Need to process performance report data when concurrent scheduling is used.
             * This will require a lot of estimation - basically we will use the analytical model.
             */
            return;
        }
        
        String uniqueReportForCommandId = 
            performanceOptimizationReport.CmdSourceUserIndex + "_" + performanceOptimizationReport.SeqId;
        APerformanceOptimizationReportsForCommandProcessor reportsForCommandProcessor = 
            m_reportsForCommandProcessors.get( uniqueReportForCommandId );
        if ( reportsForCommandProcessor == null ) {
            try {
                reportsForCommandProcessor = new APerformanceOptimizationReportsForCommandProcessor(
                        performanceOptimizationReport.CmdSourceUserIndex,
                        performanceOptimizationReport.SeqId,
                        m_systemConfigurations.get( reportSysConfigVersion ),
                        m_usersWhoWillReport,
                        m_clockSyncServer.getClockSkews()
                        );
                m_reportsForCommandProcessors.put(
                        uniqueReportForCommandId,
                        reportsForCommandProcessor
                        );
            }
            catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "APerformanceOptimizationServer::processPerformanceOptimizationReportForComputerPerformanceData: Error!",
                        e
                        );
            }
        }
        
        reportsForCommandProcessor.addPerformanceOptimizationReport( performanceOptimizationReport );
        if ( reportsForCommandProcessor.getHasBeenFullyProcessed() ) {
            m_numCommandsFullyProcessedSinceLastOptimization++;
            m_numCommandsFullyProcessedSinceLastSysConfigChange++;
            
            Map<Integer, Map<PerformanceParameterType, Double>> performanceData = reportsForCommandProcessor.getProcessedData();
            Iterator<Map.Entry<Integer, Map<PerformanceParameterType, Double>>> mainItr = performanceData.entrySet().iterator();
            while ( mainItr.hasNext() ) {
                Map.Entry<Integer, Map<PerformanceParameterType, Double>> mainEntry = mainItr.next();
                int userIndex = mainEntry.getKey();
                Map<PerformanceParameterType, Double> userPerformanceData = mainEntry.getValue();
                
                ComputerInfo userComputerInfo = m_performanceOptimizationUserInfos.get( userIndex ).getComputerInfo();
                ComputerPerformanceData userComputerPerformanceData = m_computerPerformanceDataCollection.get( userComputerInfo );
                
                Iterator<Map.Entry<PerformanceParameterType, Double>> subItr = userPerformanceData.entrySet().iterator();
                while ( subItr.hasNext() ) {
                    Map.Entry<PerformanceParameterType, Double> subEntry = subItr.next();
                    userComputerPerformanceData.addPerformanceParameterValue(
                            subEntry.getKey(),
                            subEntry.getValue()
                            );
                }
            }
        }

    }
    
    private void resetForNextOptimizationAttempt() {
        m_numCommandsFullyProcessedSinceLastOptimization = 0;
        m_numCommandsFullyProcessedSinceLastSysConfigChange = 0;
    }
    
    private void estimateParameterValues() {
        List<ComputerPerformanceData> computerPerformanceDataList = new LinkedList<ComputerPerformanceData>();
        Iterator<Map.Entry<ComputerInfo, ComputerPerformanceData>> itr = 
            m_computerPerformanceDataCollection.entrySet().iterator();
        while ( itr.hasNext() ) {
            computerPerformanceDataList.add( itr.next().getValue() );
        }
        itr = m_computerPerformanceDataCollection.entrySet().iterator();
        while ( itr.hasNext() ) {
            itr.next().getValue().estimateValuesOfAllPerformanceParameters(
                    computerPerformanceDataList 
                    );
        }
    }
    
    public synchronized void setSimulatedLatencies(
            double[][] simulatedLatencies
            ) {
        optimizeIfPossible();
    }
    
    private void optimizeIfPossible() {
    }
}
