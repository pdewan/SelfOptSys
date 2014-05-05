package selfoptsys.perf;

import java.io.*;
import java.net.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import commonutils.config.*;
import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;
import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;
import selfoptsys.network.*;
import selfoptsys.sim.*;

public class APerfReportCollector
    extends ASelfOptArchThread
    implements PerfReportCollector, LocalMessageDest {
    
    protected ConfigParamProcessor m_mainCpp;
    
    protected SessionRegistry m_sessionRegistry;
    protected String m_registryHost;
    protected int m_registryPort;
    
    protected MessageDest m_messageDest;
    protected MessageDest m_messageDestRmiStub;
    
    protected Map<ComputerInfo, ComputerStats> m_computerStatsCollection;
    
    protected double m_curThinkTimeEstimate;
    protected Map<Integer, List<Double>> m_thinkTimesForUsers;
    protected List<Double> m_thinkTimes;
    
    protected int m_latestSystemConfigurationVersion;
    protected Map<Integer, SysConfig> m_systemConfigurations;
    protected SysConfig m_curSystemConfig;
    
    protected ANetworkLatencyCollector m_networkLatencyCollector;
    protected boolean m_simulatingLatencies;
    protected SimulatedLatencyManager m_latencyManager;
    
    protected boolean m_receivedAtLeastOneReport = false;
    
    protected AtomicBoolean m_haveAnOutstandingSystemConfigurationRequest = new AtomicBoolean();
    protected SysConfig m_requestedSystemConfiguration;
    
    protected double m_lazyDelay;
    protected double m_schedulingQuantum;
    protected double m_significantDiffInLocalRespTimes;
    protected double m_significantDiffInRemoteRespTimes;
    
    protected boolean m_supportProcessingArchitectureChanges;
    protected boolean m_supportReplicatedArchitecture;
    protected boolean m_supportAllCentralizedArchitectures;
    protected List<Integer> m_supportedCentralizedArchitectureMasters;
    protected boolean m_supportMulticastCommunication;
    protected boolean m_supportSchedulingChanges;
    
    protected int m_minNumReportsRequiredToEstimateCosts = -1;
    protected int m_maxNumReportsUsedToEstimateCosts = -1;
    
    protected Map<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>> m_inputTransTimeReportsBasedOnObservedTransTimes;
    protected Map<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>> m_outputTransTimeReportsBasedOnObservedTransTimes;
    
    protected Map<Integer, PerfCollectorUserInfo> m_userInfos;
    
    protected Map<Integer, Long> m_clockSkews;
    protected List<ClockSkewMsg> m_clockSkewMsgs;
    
    protected int m_numInputEnteredEventsSinceLastSysConfigChange = 0;
    protected boolean m_gatherNewCosts = true;
    
    public APerfReportCollector(
            String registryHost,
            int registryPort
            ) {
        
        m_mainCpp = AMainConfigParamProcessor.getInstance();
        
        m_registryHost = registryHost;
        m_registryPort = registryPort;
        
        m_systemConfigurations = new Hashtable<Integer, SysConfig>();
        
        m_computerStatsCollection = new Hashtable<ComputerInfo, ComputerStats>();

        m_thinkTimesForUsers = new Hashtable<Integer, List<Double>>();
        m_thinkTimes = new LinkedList<Double>();
        
        m_schedulingQuantum = m_mainCpp.getIntParam( Parameters.SCHEDULING_QUANTUM );
        m_lazyDelay = m_mainCpp.getIntParam( Parameters.LAZY_DELAY );
        m_significantDiffInLocalRespTimes = m_mainCpp.getIntParam( Parameters.SIGNIFICANT_DIFFERENCE_IN_LOCAL_RESPONSE_TIMES );
        m_significantDiffInRemoteRespTimes = m_mainCpp.getIntParam( Parameters.SIGNIFICANT_DIFFERENCE_IN_REMOTE_RESPONSE_TIMES );
        
        m_minNumReportsRequiredToEstimateCosts = m_mainCpp.getIntParam( Parameters.MIN_NUM_REPORTS_REQUIRED_TO_ESTIMATE_COSTS );
        m_maxNumReportsUsedToEstimateCosts = m_mainCpp.getIntParam( Parameters.MAX_NUM_REPORTS_USED_TO_ESTIMATE_COSTS );

        m_messageDest = new ATCPMessageDest(
                this,
                WindowsThreadPriority.NORMAL,
                WindowsThreadPriority.HIGHEST,
                new ASerializedObjectTCPListeningThreadFactory()
                );
        ( (AMessageDest) m_messageDest ).start();
        
        int networkLatencyCollectorTimeout = AMainConfigParamProcessor.getInstance().getIntParam( Parameters.NETWORK_LATENCY_COLLECTING_TIMEOUT );
        m_simulatingLatencies = AMainConfigParamProcessor.getInstance().getBooleanParam( Parameters.SIMULATING_NETWORK_LATENCIES );
        m_networkLatencyCollector = new ANetworkLatencyCollector(
                m_simulatingLatencies,
                networkLatencyCollectorTimeout
                );
        if ( m_simulatingLatencies ) {
            ( (ASelfOptArchThread) m_networkLatencyCollector ).start();
        }
        
        m_supportProcessingArchitectureChanges = m_mainCpp.getBooleanParam( Parameters.SUPPORT_CHANGING_PROCESSING_ARCHITECTURE );
        m_supportReplicatedArchitecture = m_mainCpp.getBooleanParam( Parameters.SUPPORT_REPLICATED_ARCHITECTURE );
        m_supportAllCentralizedArchitectures = m_mainCpp.getBooleanParam( Parameters.SUPPORT_ALL_CENTRALIZED_ARCHITECTURES );
        int[] supportedCentralizedArchitectureMasters = m_mainCpp.getIntArrayParam( Parameters.SUPPORTED_CENTRALIZED_ARCHITECTURE_MASTERS );
        m_supportedCentralizedArchitectureMasters = new Vector<Integer>();
        for ( int i = 0; i < supportedCentralizedArchitectureMasters.length; i++ ) {
            m_supportedCentralizedArchitectureMasters.add( supportedCentralizedArchitectureMasters[ i ] );
        }
        m_supportMulticastCommunication = m_mainCpp.getBooleanParam( Parameters.SUPPORT_MULTICAST_COMMUNICATION );
        m_supportSchedulingChanges = m_mainCpp.getBooleanParam( Parameters.SUPPORT_SCHEDULING_POLICY_CHANGES );
        
        m_inputTransTimeReportsBasedOnObservedTransTimes = new Hashtable<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>>();
        m_outputTransTimeReportsBasedOnObservedTransTimes = new Hashtable<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>>();
        
        m_userInfos = new Hashtable<Integer, PerfCollectorUserInfo>();

        m_clockSkews = new Hashtable<Integer, Long>();
        m_clockSkewMsgs = new LinkedList<ClockSkewMsg>();
        
//        m_gatherNewCosts = m_mainCpp.getBooleanParam( Parameters.COLLECT_PARAMETER_VALUES );
    }
    
    public void setSimulatedLatencies(
            double[][] simulatedLatencies
            ) {
        m_networkLatencyCollector.setSimulatedLatencies( simulatedLatencies );
    }
    
    public void init() {
        
        try {
            m_messageDestRmiStub = (MessageDest) UnicastRemoteObject.exportObject( m_messageDest, 0 );
            
            PerfReportCollector perfReportCollectorRMIStub = 
                (PerfReportCollector) UnicastRemoteObject.exportObject( (PerfReportCollector) this, 0 );
            
            Registry registry = LocateRegistry.getRegistry(
                    m_registryHost,
                    m_registryPort
                    );
            m_sessionRegistry = (SessionRegistry) registry.lookup( "SessionRegistry" );
//            m_sessionRegistry.registerPerfReportCollector( perfReportCollectorRMIStub );
            
            loadComputerStats();
            loadThinkTimes();
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerfReportCollector: Error in init",
                    e
                    );
        }
        
    }
    
    public void begin() {
        ( (ASelfOptArchThread) this ).start();
    }
    
    public void run() {}
    
    public MessageDest getMessageDest() {
        return m_messageDest;
    }
    
    public synchronized void registerUser(
            int userIndex,
            NetworkLatencyCollectorAgent userAgent,
            InetAddress userHost,
            String processorName,
            String processorIdentifier,
            long processorSpeed,
            int latencyIndex,
            boolean collectPerfStatsForUser,
            int coreUsedByProcessingThread,
            int coreUsedByTransmissionThread,
            int quantumSize
            ) {
        
        ComputerInfo computerInfo = new AComputerInfo( 
                processorName,
                processorIdentifier,
                processorSpeed
                );
        
        PerfCollectorUserInfo userInfo = new APerfCollectorUserInfo( userIndex );
        m_userInfos.put(
                userIndex,
                userInfo
                );
        
        userInfo.setCollectStatsForUser( collectPerfStatsForUser );
        userInfo.setComputerInfo( computerInfo );
        userInfo.setCoreUsedByProcessingThread( coreUsedByProcessingThread );
        userInfo.setCoreUsedByTransmissionThread( coreUsedByTransmissionThread );
        userInfo.setQuantumSize( quantumSize );
        boolean procAndTransThreadsShareCores = coreUsedByProcessingThread == coreUsedByTransmissionThread;
        userInfo.setProcAndTransThreadsShareCores( procAndTransThreadsShareCores );
        
        if ( m_computerStatsCollection.get( computerInfo ) == null ) {
            ComputerStats stats = new AComputerStats( computerInfo );
            m_computerStatsCollection.put(
                    computerInfo,
                    stats
                    );
        }
        
        m_thinkTimesForUsers.put(
                userIndex,
                new LinkedList<Double>()
                );
        
        m_networkLatencyCollector.addUserHost(
                userIndex,
                userAgent,
                userHost
                );
        
        if ( m_simulatingLatencies ) {
            m_networkLatencyCollector.setLatencyIndexForUser(
                    userIndex,
                    latencyIndex
                    );
        }
        
    }
    
    public synchronized void unregisterUser(
            int userIndex
            ) {
    }
    
    private void handleClockSkewMsg(
            ClockSkewMsg msg
            ) {
        m_clockSkewMsgs.add( msg );
        if ( m_clockSkewMsgs.size() < 30 ) {
            return;
        }
        
        List<Long> skews = new LinkedList<Long>();
        for ( int i = 10; i < m_clockSkewMsgs.size() - 10; i++ ) {
            ClockSkewMsg curMsg = m_clockSkewMsgs.get( i );
            ClockSkewMsg nextMsg = m_clockSkewMsgs.get( i + 1 );
            long skew = 
                curMsg.getReceivedTime() -
                curMsg.getSendTimeAtSender() -
                nextMsg.getPrevMsgDelayTime();
            skews.add( skew );
        }
        
        m_clockSkewMsgs.clear();
        long avgSkew = 0;
        for ( int i = 0; i < skews.size(); i++ ) {
            avgSkew += skews.get( i );
        }
        avgSkew /= skews.size();
        m_clockSkews.put(
                msg.getUserIndex(),
                avgSkew
                );
    }
    
    public synchronized void receiveMessage(
            Message msg
            ) {
        
        if ( msg.getMessageType() == MessageType.CLOCK_SKEW_MSG ) {
            handleClockSkewMsg( (ClockSkewMsg) msg );
            return;
        }

        PerfReportMessage perfReportMsg = (PerfReportMessage) msg;
        
        if ( perfReportMsg.getUserIndex() == perfReportMsg.getCmdSourceUserIndex() ) {
            if ( m_simulatingLatencies == false && m_receivedAtLeastOneReport == false ) {
                m_receivedAtLeastOneReport = true;
                ( (ANetworkLatencyCollector) m_networkLatencyCollector ).start();
            }
            processThinkTimeReport( perfReportMsg );
            m_numInputEnteredEventsSinceLastSysConfigChange++;
            
            requestNewSystemConfigurationIfNecessary();
        }
        
        if ( m_gatherNewCosts ) {
            processPerfReport( perfReportMsg );
        }
        
    }
    
    private void processPerfReport(
            PerfReportMessage perfReportMsg
            ) {
        int reportingUserIndex = perfReportMsg.getUserIndex();

        ComputerInfo computerInfo = m_userInfos.get( reportingUserIndex ).getComputerInfo();
        
        ComputerStats compStats = m_computerStatsCollection.get( computerInfo );
        if ( compStats == null ) {
            compStats = new AComputerStats(
                    computerInfo
                    );
            m_computerStatsCollection.put(
                    computerInfo,
                    compStats
                    );
        }
        
        /*
         * Get info about message based on sys config version
         */
        SchedulingPolicy schedPol = 
            m_systemConfigurations.get( perfReportMsg.getSysConfigVersion() ).getSchedulingPolicies().get( perfReportMsg.getCmdSourceUserIndex() );

        
        int sysConfigVersionOfMsg = perfReportMsg.getSysConfigVersion();
        SysConfig sysConfigForMsg = m_systemConfigurations.get( sysConfigVersionOfMsg );
        Overlay overlayForMessage = sysConfigForMsg.getNetworkOverlays().get( perfReportMsg.getCmdSourceUserIndex() );
        int coreUsedByProcessingThread = m_userInfos.get( reportingUserIndex ).getCoreUsedByProcessingThread();
        int coreUsedByTransmissionThread = m_userInfos.get( reportingUserIndex ).getCoreUsedByTransmissionThread();
                
        int senderUserIndex = overlayForMessage.getParents()[ reportingUserIndex ];
        if ( senderUserIndex == -1 ) {
            senderUserIndex = reportingUserIndex;
        }
        perfReportMsg.setSenderUserIndex( senderUserIndex );
        
        ProcessingArchitectureType procArch = sysConfigForMsg.getMasterUserIndices().size() == 1 ? 
                ProcessingArchitectureType.CENTRALIZED : ProcessingArchitectureType.REPLICATED;
        
        /*
         * Done getting info about message based on sys config version
         */
        
        double value = 0;

        if ( procArch == ProcessingArchitectureType.CENTRALIZED ) {
            if ( reportingUserIndex == sysConfigForMsg.getMasterUserIndices().get( 0 ) ) {
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC,
                            value
                            );
                }
                
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC,
                            value
                            );
                }

                value = getCostValueFromPerfReport(
                        PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS,
                            value
                            );
                }
            }
            else {
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC,
                            value
                            );
                }
                
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS,
                            value
                            );
                }

                value = getCostValueFromPerfReport(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS,
                            value
                            );
                }
            }
        }
        else {
            if ( reportingUserIndex == perfReportMsg.getCmdSourceUserIndex() ) {
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC,
                            value
                            );
                }
                
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC,
                            value
                            );
                }
                
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS,
                            value
                            );
                }
            }
            else {
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC,
                            value
                            );
                }
                
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC,
                            value
                            );
                }
                
                value = getCostValueFromPerfReport(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS,
                        perfReportMsg,
                        schedPol,
                        coreUsedByProcessingThread,
                        coreUsedByTransmissionThread
                        );
                if ( value > 0 ) {
                    compStats.appendCostToCurrentSession(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS,
                            value
                            );
                }
            }
        }
        
        updateObservedTransTimeData( perfReportMsg );
        
    }
    
    private void processThinkTimeReport(
            PerfReportMessage perfReportMsg
            ) {
        if ( perfReportMsg.getThinkTime() == 0 ) {
            return;
        }

        int userIndex = perfReportMsg.getUserIndex();
        m_thinkTimes.add( perfReportMsg.getThinkTime() );
        m_thinkTimesForUsers.get( userIndex ).add( perfReportMsg.getThinkTime() );
    }
    
    public synchronized void setCurrentSystemConfiguration(
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
        m_curSystemConfig = newSystemConfiguration;
    }
    
    public void quit() {

        try {
            ErrorHandlingUtils.logInfoMessageAndContinue( "APerfReportCollector quitting ..." );
            m_messageDest.resetMsgDest();
            saveComputerStats();
            saveThinkTimes();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerfReportCollector: Error while quitting",
                    e
                    );
        }
        
    }
    
    public synchronized void reset() {
        loadComputerStats();
        loadThinkTimes();
    }
    
    private void loadComputerStats() {
        
        String costEstimatesFile = m_mainCpp.getStringParam( Parameters.COST_ESTIMATES_FILE_IN );
        File file = new File( costEstimatesFile );
        if ( file.exists() == false ) {
            ErrorHandlingUtils.logWarningMessageAndContinue( "Cost estimates file does not exist." );
            return;
        }
        
        m_computerStatsCollection = AComputerStats.loadObjects( costEstimatesFile );

    }
    
    private void saveComputerStats() {
        
        calculateNewEstimates();
        
        String outputDirectory = m_mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY );
        String costEstimatesFile = m_mainCpp.getStringParam( Parameters.COST_ESTIMATES_FILE_OUT );
        AComputerStats.saveObjects(
        		outputDirectory + "\\" + costEstimatesFile,
                m_computerStatsCollection
                );
        
    }
    
    private void loadThinkTimes() {
        
        try {
            String thinkTimeEstimateFile = m_mainCpp.getStringParam( Parameters.THINK_TIME_ESTIMATE_FILE_IN );
            File file = new File( thinkTimeEstimateFile );
            if ( file.exists() == false ) {
                ErrorHandlingUtils.logWarningMessageAndContinue( "Think time estimates file does not exist." );
                return;
            }
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList statsObjectsNodeList = doc.getElementsByTagName("ThinkTime");
                
            Node statsObjectsFirstNode = statsObjectsNodeList.item( 0 );
            if ( statsObjectsFirstNode.getNodeType() == Node.ELEMENT_NODE ) {
                 Element statObjectsFirstNodeListElement = (Element) statsObjectsFirstNode;
                 NodeList statsObjectNodeList = statObjectsFirstNodeListElement.getElementsByTagName( "parameter" );
                 Node statsObjectFirstNode = statsObjectNodeList.item( 0 );
                 if ( statsObjectFirstNode.getNodeType() == Node.ELEMENT_NODE ) {
                     Element firstElement = (Element) statsObjectFirstNode;

                     NodeList paramValueNodeList = firstElement.getElementsByTagName("value");
                     Element paramValueElement = (Element) paramValueNodeList.item(0);
                     NodeList paramValueElementChildNodes = paramValueElement.getChildNodes();
                     Object parametertValue = ((Node) paramValueElementChildNodes.item(0)).getNodeValue();
                     
                     m_curThinkTimeEstimate = Double.parseDouble( (String ) parametertValue );
                 }
            }

        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerfReportCollector: Could not load think time estimates file",
                    e
                    );
        }
        
    }
    
    private void saveThinkTimes() {
        
        try {
            calculateThinkTimeEstimate();

            String outputDirectory = m_mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY );
            String thinkTimeEstimateFile = m_mainCpp.getStringParam( Parameters.THINK_TIME_ESTIMATE_FILE_OUT );
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement( "estimates" );
            document.appendChild( rootElement );
            
            Element thinkTimeNode = document.createElement( "ThinkTime" );
            Element emParameterNode = document.createElement( "parameter" );
            Element emParameterNodeChild = document.createElement( "name" );
            emParameterNodeChild.setTextContent( "ThinkTime" );
            emParameterNode.appendChild( emParameterNodeChild );
            emParameterNodeChild = document.createElement( "value" );
            emParameterNodeChild.setTextContent( Double.toString( m_curThinkTimeEstimate ) );
            emParameterNode.appendChild( emParameterNodeChild );
            emParameterNode.appendChild( emParameterNodeChild );
            thinkTimeNode.appendChild( emParameterNode );
            
            rootElement.appendChild( thinkTimeNode );

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); 
            transformer.setOutputProperty( "{http://xml.apache.org/xalan}indent-amount", "4" );
            DOMSource source = new DOMSource( document );
            StreamResult result =  new StreamResult( outputDirectory + "\\" + thinkTimeEstimateFile );
            transformer.transform( source, result );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerfReportCollector: Could not save think time estimates file",
                    e
                    );
        }

    }
    
    private void calculateThinkTimeEstimate() {
        
        double newThinkTimeEstimate = 0.0;
        if ( m_thinkTimes.size() > 0 ) {
            for ( int i = 0; i < m_thinkTimes.size(); i++ ) {
                newThinkTimeEstimate += m_thinkTimes.get( i );
            }
            newThinkTimeEstimate /= m_thinkTimes.size();
        }
        
        if ( m_curThinkTimeEstimate == 0 ) {
            m_curThinkTimeEstimate = newThinkTimeEstimate;
        }
        else if ( newThinkTimeEstimate > 0 ) {
            m_curThinkTimeEstimate = m_curThinkTimeEstimate * 0.3 + newThinkTimeEstimate * 0.7;
        }
        
        m_curThinkTimeEstimate = MathUtils.round( m_curThinkTimeEstimate, 4 );
        
    }
    
    private synchronized boolean requestNewSystemConfigurationIfNecessary() {
        
        boolean status = false;
        
        try {
            if ( haveEnoughReportsForEstimatingCosts() == false && 
                    m_numInputEnteredEventsSinceLastSysConfigChange < m_minNumReportsRequiredToEstimateCosts ) {
                return true;
            }
            
            calculateNewEstimates();
            
            Map<Integer,Map<Integer,Double>> latencies = m_networkLatencyCollector.getCopyOfLatencies();
            
            SysConfigOptimizer optimizer = ASysConfigOptimizerFactorySelector.getFactory().getSysConfigOptimizer(
                    m_schedulingQuantum,
                    m_lazyDelay,
                    m_supportProcessingArchitectureChanges,
                    m_supportReplicatedArchitecture,
                    m_supportAllCentralizedArchitectures,
                    m_supportedCentralizedArchitectureMasters,
                    m_supportMulticastCommunication,
                    m_supportSchedulingChanges
                    );
            SysConfig optimizedSysConfig = optimizer.optimizeCurrentSysConfig(
                    m_curSystemConfig,
                    m_userInfos,
                    m_computerStatsCollection,
                    latencies,
                    m_curThinkTimeEstimate,
                    m_significantDiffInLocalRespTimes,
                    m_significantDiffInRemoteRespTimes
                    );
            if ( optimizedSysConfig == null || 
                    optimizedSysConfig == m_curSystemConfig ) {
                resetForNextOptimizationAttempt();
                return true;
            }
            
            HashMap<Integer, HashMap<PerformanceParameterType, Double>> observedTransCosts = new HashMap<Integer, HashMap<PerformanceParameterType,Double>>();
            Iterator<Map.Entry<Integer, PerfCollectorUserInfo>> computerInfoItr = m_userInfos.entrySet().iterator();
            while ( computerInfoItr.hasNext() ) {
                Map.Entry<Integer, PerfCollectorUserInfo> entry = computerInfoItr.next();
                int userIndex = entry.getKey();
                ComputerInfo compInfo = entry.getValue().getComputerInfo();
                
                HashMap<PerformanceParameterType, Double> observedCosts = new HashMap<PerformanceParameterType, Double>();
                List<PerformanceParameterType> observedTransCostCostTypes = new LinkedList<PerformanceParameterType>();
                observedTransCostCostTypes.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                observedTransCostCostTypes.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                
                for ( int i = 0; i < observedTransCostCostTypes.size(); i++ ) {
                    observedCosts.put(
                            observedTransCostCostTypes.get( i ),
                            m_computerStatsCollection.get( compInfo ).getCostEstimate(
                                    observedTransCostCostTypes.get( i ),
                                    true,
                                    false,
                                    m_computerStatsCollection
                                    )
                            );
                }
                
                observedTransCosts.put(
                        userIndex,
                        observedCosts
                        );
            }
            
            status = m_sessionRegistry.setupNewSysConfig(
                    optimizedSysConfig.getSystemConfigurationVersion(),
                    optimizedSysConfig.getMasterUserIndices(),
                    optimizedSysConfig.getNetworkOverlays(),
                    optimizedSysConfig.getSchedulingPolicies(),
                    observedTransCosts
                    );
            
            resetForNextOptimizationAttempt();
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerfReportCollector: Error while requesting (if necessary) new sys config configuration",
                    e
                    );
        }
        
        return status;
    }
    
    private void resetForNextOptimizationAttempt() {
        m_numInputEnteredEventsSinceLastSysConfigChange = 0;
        Iterator<ComputerStats> itr = m_computerStatsCollection.values().iterator();
        while ( itr.hasNext() ) {
            itr.next().resetNumCostsCounters();
        }
    }
    
    private void calculateNewEstimates() {
        
        calculateTransCostsBasedOnObservedTransTimes();
        
        Iterator<ComputerStats> compStatsItr = m_computerStatsCollection.values().iterator();
        while ( compStatsItr.hasNext() ) {
            compStatsItr.next().calculateEstimatesBasedOnValuesFromCurrentSession( m_maxNumReportsUsedToEstimateCosts );
        }
        
        calculateThinkTimeEstimate();
        
    }
    
    protected boolean haveEnoughReportsForEstimatingCosts() {
        
        boolean haveEnoughReports = false;
        if ( m_computerStatsCollection.size() > 0 ) {
            haveEnoughReports = true;
        }
        
        Iterator<Map.Entry<ComputerInfo, ComputerStats>> itr = 
            m_computerStatsCollection.entrySet().iterator();
        while ( itr.hasNext() ) {
            int maxNumReportsForThisComputer = 0;
            
            Map.Entry<ComputerInfo, ComputerStats> entry = itr.next();
            
            if ( entry.getKey().getProcessorName().equals( Constants.FAKE_LOGGABLE_PROCESSOR_NAME ) && 
                    entry.getKey().getProcessorIdentifier().equals( Constants.FAKE_LOGGABLE_PROCESSOR_IDENTIFIER ) && 
                    entry.getKey().getProcessorSpeed() == Constants.FAKE_LOGGABLE_PROCESSOR_SPEED ) {
                continue;
            }
            
            Iterator<Map.Entry<PerformanceParameterType, Integer>> itr2 = 
                entry.getValue().getNumCostsCounters().entrySet().iterator();
            while ( itr2.hasNext() ) {
                maxNumReportsForThisComputer = Math.max(
                        maxNumReportsForThisComputer, 
                        itr2.next().getValue()
                        );
            }
            
            if ( maxNumReportsForThisComputer < m_minNumReportsRequiredToEstimateCosts ) {
                haveEnoughReports = false;
                break;
            }
        }
        
        return haveEnoughReports;
    } 
    
    private void calculateTransCostsBasedOnObservedTransTimes() {
        
        Iterator<Map.Entry<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>>> inputReportItr =
            m_inputTransTimeReportsBasedOnObservedTransTimes.entrySet().iterator();
        while ( inputReportItr.hasNext() ) {
            Map.Entry<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>> entry = inputReportItr.next();
            Iterator<Map.Entry<Integer, TransTimeReportBasedOnObservedTransTime>> itr2 = 
                entry.getValue().entrySet().iterator();
            while ( itr2.hasNext() ) {
                TransTimeReportBasedOnObservedTransTime transReport = itr2.next().getValue();
                if ( transReport.getSendersPerfReportMsg() == null ||
                        transReport.getMsgReceiveTimeOfLastDestTransmittedTo() <= 0 ) {
                    continue;
                }
                
                /*
                 * TODO: 
                 * For now, we do not estimate observed trans costs when concurrent or lazy
                 * scheduling policies are used
                 */
                
                SchedulingPolicy sendersSchedPol = m_systemConfigurations.get( transReport.getSysConfigVersion() ).getSchedulingPolicies().get(
                        transReport.getSenderUserIndex()
                        );
                if ( sendersSchedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT || 
                        sendersSchedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
                    continue;
                }
                
                long sendersSendStartTime = Long.MAX_VALUE;
                int numDestsTransmittedTo = 0;
                sendersSendStartTime = Math.min(
                        sendersSendStartTime,
                        transReport.getSendersPerfReportMsg().getInputTransStartTime()
                        );
                numDestsTransmittedTo += transReport.getSendersPerfReportMsg().getInputNumDestsForwardedTo();
                
                long observedTransTimeOfFirstUserIndexTransmittedTo =
                    transReport.getMsgReceiveTimeOfFirstDestTransmittedTo() - sendersSendStartTime;
                long observedTransTimeOfLastUserIndexTransmittedTo =
                    transReport.getMsgReceiveTimeOfLastDestTransmittedTo() - sendersSendStartTime;
                
                double observedTransCostToFirstDest = MathUtils.round(
                        observedTransTimeOfFirstUserIndexTransmittedTo / (double) 1000000,
                        2
                        );
                double observedTransCostToLastDest = -1;
                if ( observedTransTimeOfLastUserIndexTransmittedTo != -1 ) {
                    observedTransCostToLastDest = MathUtils.round(
                            observedTransTimeOfLastUserIndexTransmittedTo / numDestsTransmittedTo / (double) 1000000,
                            2
                            );
                }
                
                int userIndex = entry.getKey();
                ComputerInfo compInfo = m_userInfos.get( userIndex ).getComputerInfo();
                ComputerStats compStats = m_computerStatsCollection.get( compInfo );
                boolean isMaster = m_curSystemConfig.getMasterUserIndices().contains( userIndex );
                boolean inputtingUser = userIndex == transReport.getCmdSourceUserIndex() ? true : false;
                ProcessingArchitectureType procArchType =
                    m_curSystemConfig.getMasterUserIndices().size() == 1 ? 
                            ProcessingArchitectureType.CENTRALIZED : ProcessingArchitectureType.REPLICATED;
                  
                PerformanceParameterType costType = null;
                PerformanceParameterType costTypeToFirstDest = null;
                if ( procArchType == ProcessingArchitectureType.REPLICATED ) {
                    if ( inputtingUser ) {
                        costType = PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                        costTypeToFirstDest = PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                    }
                    else {
                        costType = PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                        costTypeToFirstDest = PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                    }
                }
                else {
                    if ( isMaster ) {
                        costType = PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME;
                        costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                    }
                    else {
                        if ( inputtingUser ) {
                            costType = PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                            costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                        }
                        else {
                            costType = PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME;
                            costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                        }
                    }
                }
                
                compStats.appendCostToCurrentSession(
                        costTypeToFirstDest,
                        observedTransCostToFirstDest
                        );
                if ( observedTransCostToLastDest != -1 ) {
                    compStats.appendCostToCurrentSession(
                            costType,
                            observedTransCostToLastDest
                            );
                }

            }
        }
        
        Iterator<Map.Entry<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>>> outputReportItr =
            m_outputTransTimeReportsBasedOnObservedTransTimes.entrySet().iterator();
        while ( outputReportItr.hasNext() ) {
            Map.Entry<Integer, Map<Integer, TransTimeReportBasedOnObservedTransTime>> entry = outputReportItr.next();
            Iterator<Map.Entry<Integer, TransTimeReportBasedOnObservedTransTime>> itr2 = 
                entry.getValue().entrySet().iterator();
            while ( itr2.hasNext() ) {
                TransTimeReportBasedOnObservedTransTime transReport = itr2.next().getValue();
                if ( transReport.getSendersPerfReportMsg() == null ||
                        transReport.getMsgReceiveTimeOfLastDestTransmittedTo() <= 0 ) {
                    continue;
                }
                
                /*
                 * TODO: 
                 * For now, we do not estimate observed trans costs when concurrent or lazy
                 * scheduling policies are used
                 */
                
                SchedulingPolicy sendersSchedPol = m_systemConfigurations.get( transReport.getSysConfigVersion() ).getSchedulingPolicies().get(
                        transReport.getSenderUserIndex()
                        );
                if ( sendersSchedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT || 
                        sendersSchedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
                    continue;
                }

                long sendersSendStartTime = Long.MAX_VALUE;
                int numDestsTransmittedTo = 0;
                sendersSendStartTime = Math.min(
                        sendersSendStartTime,
                        transReport.getSendersPerfReportMsg().getOutputTransStartTime()
                        );
                numDestsTransmittedTo += transReport.getSendersPerfReportMsg().getOutputNumDestsForwardedTo();
                
                long observedTransTimeOfFirstUserIndexTransmittedTo =
                    transReport.getMsgReceiveTimeOfFirstDestTransmittedTo() - sendersSendStartTime;
                long observedTransTimeOfLastUserIndexTransmittedTo =
                    transReport.getMsgReceiveTimeOfLastDestTransmittedTo() - sendersSendStartTime;
                
                double observedTransCostToFirstDest = MathUtils.round(
                        observedTransTimeOfFirstUserIndexTransmittedTo / (double) 1000000,
                        2
                        );
                double observedTransCostToLastDest = -1;
                if ( observedTransTimeOfLastUserIndexTransmittedTo != -1 ) {
                    observedTransCostToLastDest = MathUtils.round(
                            observedTransTimeOfLastUserIndexTransmittedTo / numDestsTransmittedTo / (double) 1000000,
                            2
                            );
                }
                
                int userIndex = entry.getKey();
                ComputerInfo compInfo = m_userInfos.get( userIndex ).getComputerInfo();
                ComputerStats compStats = m_computerStatsCollection.get( compInfo );
                boolean isMaster = m_curSystemConfig.getMasterUserIndices().contains( userIndex );
                boolean inputtingUser = userIndex == transReport.getCmdSourceUserIndex() ? true : false;
                ProcessingArchitectureType procArchType =
                    m_curSystemConfig.getMasterUserIndices().size() == 1 ? 
                            ProcessingArchitectureType.CENTRALIZED : ProcessingArchitectureType.REPLICATED;
                  
                PerformanceParameterType costType = null;
                PerformanceParameterType costTypeToFirstDest = null;
                if ( procArchType == ProcessingArchitectureType.REPLICATED ) {
                    if ( inputtingUser ) {
                        costType = PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                        costTypeToFirstDest = PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                    }
                    else {
                        costType = PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                        costTypeToFirstDest = PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                    }
                }
                else {
                    if ( isMaster ) {
                        costType = PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME;
                        costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                    }
                    else {
                        if ( inputtingUser ) {
                            costType = PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME;
                            costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                        }
                        else {
                            costType = PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME;
                            costTypeToFirstDest = PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME;
                        }
                    }
                }
                
                compStats.appendCostToCurrentSession(
                        costTypeToFirstDest,
                        observedTransCostToFirstDest
                        );
                if ( observedTransCostToLastDest != -1 ) {
                    compStats.appendCostToCurrentSession(
                            costType,
                            observedTransCostToLastDest
                            );
                }

            }
        }

    }
    
    private double getCostValueFromPerfReport(
            PerformanceParameterType costType,
            PerfReportMessage perfReportMsg,
            SchedulingPolicy schedPol,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread
            ) {
        double value = 0;
        
        if ( schedPol == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST || 
                schedPol == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ||
                schedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
            value = getCostValueFromPerfReportWithSequentialSchedPol(
                    costType,
                    perfReportMsg,
                    coreToUseForProcessingThread,
                    coreToUseForTransmissionThread
                    );
        }
        else if ( schedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
            int quantumSize = m_userInfos.get( perfReportMsg.getUserIndex() ).getQuantumSize();
            value = getCostValueFromPerfReportWithConcurrentSchedPol(
                    costType,
                    perfReportMsg,
                    coreToUseForProcessingThread,
                    coreToUseForTransmissionThread,
                    ( quantumSize * (long) 1000000 )
                    );
        }
        
        return value;
    }
    
    private double getCostValueFromPerfReportWithSequentialSchedPol(
            PerformanceParameterType costType,
            PerfReportMessage perfReportMsg,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread
            ) {
        double value = 0;
        
        if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ) {
            value = getMillisFromNanos( perfReportMsg.getInputProcTime() );
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC ) {
            value = getMillisFromNanos( perfReportMsg.getOutputProcTime() );
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ||
                costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ||
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ) {
            
            value = getMillisFromNanos(
                    perfReportMsg.getInputTransTime() / perfReportMsg.getInputNumDestsForwardedTo()
                    );
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS ) {
            
            value = getMillisFromNanos(
                    perfReportMsg.getOutputTransTime() / perfReportMsg.getOutputNumDestsForwardedTo()
                    );
        }
        
        return value;
    }
    
    private double getCostValueFromPerfReportWithConcurrentSchedPol(
            PerformanceParameterType costType,
            PerfReportMessage perfReportMsg,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread,
            long quantumSize
            ) {
        double value = 0;
        
        boolean procAndInputTransThreadsShareACore = perfReportMsg.getInputTransCore() == coreToUseForTransmissionThread;
        boolean procAndOutputTransThreadsShareACore = perfReportMsg.getOutputTransCore() == coreToUseForTransmissionThread;

        if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ) {
            
            if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ) {
                value = getMillisFromNanos( perfReportMsg.getInputProcTime() );
            } 
            else if ( procAndInputTransThreadsShareACore == false ) {
                value = getMillisFromNanos( perfReportMsg.getInputProcTime() );
            }
            else {
                long startTimeOfTask = perfReportMsg.getInputProcStartTime();
                long startTimeOfConcurrentTask = perfReportMsg.getInputTransStartTime();
                long durationOfTask = perfReportMsg.getInputProcTime();
                long durationOfConcurrentTask = perfReportMsg.getInputTransTime();
                
                value = SimulationUtils.getActualDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
                        startTimeOfTask,
                        startTimeOfConcurrentTask,
                        durationOfTask,
                        durationOfConcurrentTask,
                        quantumSize
                        );
            }
            
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ||
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC ) {
            
            if ( costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ||
                    costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC ) {
                value = getMillisFromNanos( perfReportMsg.getInputProcTime() );
            } 
            else if ( procAndOutputTransThreadsShareACore == false ) {
                value = getMillisFromNanos( perfReportMsg.getOutputProcTime() );
            }
            else {
                long startTimeOfTask = perfReportMsg.getOutputProcStartTime();
                long startTimeOfConcurrentTask = perfReportMsg.getOutputTransStartTime();
                long durationOfTask = perfReportMsg.getOutputProcTime();
                long durationOfConcurrentTask = perfReportMsg.getOutputTransTime();
                
                value = SimulationUtils.getActualDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
                        startTimeOfTask,
                        startTimeOfConcurrentTask,
                        durationOfTask,
                        durationOfConcurrentTask,
                        quantumSize
                        );
            }

        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ||
                costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ||
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ) {
            
            if ( costType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ) {
                value = getMillisFromNanos( perfReportMsg.getInputTransTime() );
            }
            else if ( procAndInputTransThreadsShareACore == false ) {
                value = getMillisFromNanos( perfReportMsg.getInputTransTime() );
            }
            else {
                long startTimeOfTask = perfReportMsg.getInputTransStartTime();
                long startTimeOfConcurrentTask = perfReportMsg.getInputProcStartTime();
                long durationOfTask = perfReportMsg.getInputTransTime();
                long durationOfConcurrentTask = perfReportMsg.getInputProcTime();
                
                value = SimulationUtils.getActualDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
                        startTimeOfTask,
                        startTimeOfConcurrentTask,
                        durationOfTask,
                        durationOfConcurrentTask,
                        quantumSize
                        );
            }
            
            value = MathUtils.round( value / perfReportMsg.getInputNumDestsForwardedTo(), 4 );
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS ) {
            
            if ( procAndOutputTransThreadsShareACore == false ) {
                value = getMillisFromNanos( perfReportMsg.getOutputTransTime() );
            }
            else {
                long startTimeOfTask = perfReportMsg.getOutputTransStartTime();
                long startTimeOfConcurrentTask = perfReportMsg.getOutputProcStartTime();
                long durationOfTask = perfReportMsg.getOutputTransTime();
                long durationOfConcurrentTask = perfReportMsg.getOutputProcTime();
                
                value = SimulationUtils.getActualDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
                        startTimeOfTask,
                        startTimeOfConcurrentTask,
                        durationOfTask,
                        durationOfConcurrentTask,
                        quantumSize
                        );
            }
            
            value = MathUtils.round( value / perfReportMsg.getInputNumDestsForwardedTo(), 4 );
        }
        
        return value;
    }
    
    private double getMillisFromNanos(
            long nanos
            ) {
        return MathUtils.round(
                nanos / (double) 1000000,
                4
                );
    }
    
    private void updateObservedTransTimeData(
            PerfReportMessage perfReportMsg
            ) {
        
        PerfReportMessage copyOfPerfReportMsg = APerfReportMessage.copy( perfReportMsg );
        adjustPerfReportMsgTimesForClockSkew( copyOfPerfReportMsg );
        copyOfPerfReportMsg.setSenderUserIndex( perfReportMsg.getSenderUserIndex() );
        
        int reportingUserIndex = copyOfPerfReportMsg.getUserIndex();
        int senderUserIndex = copyOfPerfReportMsg.getSenderUserIndex();
        
        if ( senderUserIndex != reportingUserIndex ) {
            if ( copyOfPerfReportMsg.getInputOriginalReceiveTime() > 0 - m_clockSkews.get( reportingUserIndex ) ) {
                Map<Integer, TransTimeReportBasedOnObservedTransTime> reportsForSender = m_inputTransTimeReportsBasedOnObservedTransTimes.get( senderUserIndex );
                if ( reportsForSender == null ) {
                    reportsForSender = new Hashtable<Integer, TransTimeReportBasedOnObservedTransTime>();
                    m_inputTransTimeReportsBasedOnObservedTransTimes.put(
                            senderUserIndex,
                            reportsForSender
                            );
                }
                
                TransTimeReportBasedOnObservedTransTime reportForSendersSeqId = reportsForSender.get( copyOfPerfReportMsg.getSeqId() );
                if ( reportForSendersSeqId == null ) {
                    reportForSendersSeqId = new ATransTimeReportBasedOnObservedTransTime(
                            senderUserIndex,
                            copyOfPerfReportMsg.getSysConfigVersion(),
                            copyOfPerfReportMsg.getCmdSourceUserIndex(),
                            copyOfPerfReportMsg.getSeqId()
                            );
                    reportsForSender.put(
                            copyOfPerfReportMsg.getSeqId(),
                            reportForSendersSeqId
                            );
                }
                
                if ( reportForSendersSeqId.getMsgReceiveTimeOfFirstDestTransmittedTo() != -1 ) {
                    if ( copyOfPerfReportMsg.getInputOriginalReceiveTime() <= reportForSendersSeqId.getMsgReceiveTimeOfFirstDestTransmittedTo() ) {
                        reportForSendersSeqId.setMsgReceivedTimeOfLastDestTransmittedTo( 
                                reportForSendersSeqId.getMsgReceiveTimeOfFirstDestTransmittedTo()
                                );
                        reportForSendersSeqId.setMsgReceivedTimeOfFirstDestTransmittedTo(
                                copyOfPerfReportMsg.getInputOriginalReceiveTime()
                                );
                    }
                    else {
                        reportForSendersSeqId.setMsgReceivedTimeOfLastDestTransmittedTo( 
                                copyOfPerfReportMsg.getInputOriginalReceiveTime()
                                );
                    }
                }
                reportForSendersSeqId.setMsgReceivedTimeOfFirstDestTransmittedTo( 
                        copyOfPerfReportMsg.getInputOriginalReceiveTime()
                        );
            }
            
            if ( copyOfPerfReportMsg.getOutputOriginalReceiveTime() > 0 - m_clockSkews.get( reportingUserIndex ) ) {
                Map<Integer, TransTimeReportBasedOnObservedTransTime> reportsForSender = m_outputTransTimeReportsBasedOnObservedTransTimes.get( senderUserIndex );
                if ( reportsForSender == null ) {
                    reportsForSender = new Hashtable<Integer, TransTimeReportBasedOnObservedTransTime>();
                    m_outputTransTimeReportsBasedOnObservedTransTimes.put(
                            senderUserIndex,
                            reportsForSender
                            );
                }
                
                TransTimeReportBasedOnObservedTransTime reportForSendersSeqId = reportsForSender.get( copyOfPerfReportMsg.getSeqId() );
                if ( reportForSendersSeqId == null ) {
                    reportForSendersSeqId = new ATransTimeReportBasedOnObservedTransTime(
                            senderUserIndex,
                            copyOfPerfReportMsg.getSysConfigVersion(),
                            copyOfPerfReportMsg.getCmdSourceUserIndex(),
                            copyOfPerfReportMsg.getSeqId()
                            );
                    reportsForSender.put(
                            copyOfPerfReportMsg.getSeqId(),
                            reportForSendersSeqId
                            );
                }
                
                if ( reportForSendersSeqId.getMsgReceiveTimeOfFirstDestTransmittedTo() != -1 ) {
                    if ( copyOfPerfReportMsg.getOutputOriginalReceiveTime() <= reportForSendersSeqId.getMsgReceiveTimeOfFirstDestTransmittedTo() ) {
                        reportForSendersSeqId.setMsgReceivedTimeOfLastDestTransmittedTo( 
                                reportForSendersSeqId.getMsgReceiveTimeOfFirstDestTransmittedTo()
                                );
                        reportForSendersSeqId.setMsgReceivedTimeOfFirstDestTransmittedTo(
                                copyOfPerfReportMsg.getOutputOriginalReceiveTime()
                                );
                    }
                    else {
                        reportForSendersSeqId.setMsgReceivedTimeOfLastDestTransmittedTo( 
                                copyOfPerfReportMsg.getOutputOriginalReceiveTime()
                                );
                    }
                }
                reportForSendersSeqId.setMsgReceivedTimeOfFirstDestTransmittedTo( 
                        copyOfPerfReportMsg.getOutputOriginalReceiveTime()
                        );
            }
        }
        else if ( senderUserIndex == reportingUserIndex ) {
            
            if ( copyOfPerfReportMsg.getInputNumDestsForwardedTo() > 0 ) {
                Map<Integer, TransTimeReportBasedOnObservedTransTime> reportsForSender = m_inputTransTimeReportsBasedOnObservedTransTimes.get( senderUserIndex );
                if ( reportsForSender == null ) {
                    reportsForSender = new Hashtable<Integer, TransTimeReportBasedOnObservedTransTime>();
                    m_inputTransTimeReportsBasedOnObservedTransTimes.put(
                            senderUserIndex,
                            reportsForSender
                            );
                }
                
                TransTimeReportBasedOnObservedTransTime reportForSendersSeqId = reportsForSender.get( copyOfPerfReportMsg.getSeqId() );
                if ( reportForSendersSeqId == null ) {
                    reportForSendersSeqId = new ATransTimeReportBasedOnObservedTransTime(
                            senderUserIndex,
                            copyOfPerfReportMsg.getSysConfigVersion(),
                            copyOfPerfReportMsg.getCmdSourceUserIndex(),
                            copyOfPerfReportMsg.getSeqId()
                            );
                    reportsForSender.put(
                            copyOfPerfReportMsg.getSeqId(),
                            reportForSendersSeqId
                            );
                }
                
                reportForSendersSeqId.setSendersPerfReportMsg(
                        copyOfPerfReportMsg
                        );
            }

            if ( copyOfPerfReportMsg.getOutputNumDestsForwardedTo() > 0 ) {
                Map<Integer, TransTimeReportBasedOnObservedTransTime> reportsForSender = m_outputTransTimeReportsBasedOnObservedTransTimes.get( senderUserIndex );
                if ( reportsForSender == null ) {
                    reportsForSender = new Hashtable<Integer, TransTimeReportBasedOnObservedTransTime>();
                    m_outputTransTimeReportsBasedOnObservedTransTimes.put(
                            senderUserIndex,
                            reportsForSender
                            );
                }
                
                TransTimeReportBasedOnObservedTransTime reportForSendersSeqId = reportsForSender.get( copyOfPerfReportMsg.getSeqId() );
                if ( reportForSendersSeqId == null ) {
                    reportForSendersSeqId = new ATransTimeReportBasedOnObservedTransTime(
                            senderUserIndex,
                            copyOfPerfReportMsg.getSysConfigVersion(),
                            copyOfPerfReportMsg.getCmdSourceUserIndex(),
                            copyOfPerfReportMsg.getSeqId()
                            );
                    reportsForSender.put(
                            copyOfPerfReportMsg.getSeqId(),
                            reportForSendersSeqId
                            );
                }
                
                reportForSendersSeqId.setSendersPerfReportMsg(
                        copyOfPerfReportMsg
                        );
            }
        }

    }
    
    private void adjustPerfReportMsgTimesForClockSkew(
            PerfReportMessage msg
            ) {
        long clockSkew = m_clockSkews.get( msg.getUserIndex() );
        
        msg.setInputProcStartTime( 
                msg.getInputProcStartTime() + clockSkew
                );
        msg.setOutputProcStartTime(
                msg.getOutputProcStartTime() + clockSkew
                );
        
        msg.setInputTransStartTime( msg.getInputTransStartTime() + clockSkew );
        
        msg.setOutputTransStartTime( msg.getOutputTransStartTime() + clockSkew );

        msg.setInputOriginalReceiveTime( 
                msg.getInputOriginalReceiveTime() + clockSkew
                );
        msg.setOutputOriginalReceiveTime( 
                msg.getOutputOriginalReceiveTime() + clockSkew
                );
        
    }
    
}

