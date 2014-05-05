package selfoptsys.perf;

import java.util.*;
import commonutils.config.*;
import commonutils.basic.*;
import commonutils.basic2.*;
import selfoptsys.config.*;
import selfoptsys.network.*;
import selfoptsys.network.overlay.*;
import selfoptsys.perf.tradeofffunction.*;
import selfoptsys.sim.*;


public class ASysConfigOptimizer 
    implements SysConfigOptimizer {

    protected double m_schedulingQuantum;
    protected double m_lazyDelay;
    
    protected SysConfig m_curSysConfig;
    protected Map<Integer, PerfCollectorUserInfo> m_userInfos;
    protected Map<ComputerInfo, ComputerStats> m_computerStatsCollection;
    protected double m_thinkTimeEstimate;
    protected double m_significantDiffInLocalRespTimes;
    protected double m_significantDiffInRemoteRespTimes;
    protected Map<Integer,Map<Integer,Double>> m_latencies;
    
    protected RespTimeTradeoffPolicy m_respTimeTradeoffPolicy;
    
    protected Map<Integer, List<ASysConfigOptimizationInfoDeltaListEntry>> m_perUserSysConfigOptimizerInfoDeltaList;
    
    protected boolean m_supportProcessingArchitectureChanges;
    protected boolean m_supportReplicatedArchitecture;
    protected boolean m_supportAllCentralizedArchitectures;
    protected List<Integer> m_supportedCentralizedArchitectureMasters;
    protected boolean m_supportMulticastCommunication;
    protected boolean m_supportSchedulingChanges;
    
    public ASysConfigOptimizer(
            double schedulingQuantum,
            double lazyDelay,
            boolean supportProcessingArchitectureChanges,
            boolean supportReplicatedArchitectures,
            boolean supportAllCentralizedArchitectures,
            List<Integer> supportedCentralizedArchitectureMasters,
            boolean supportMulticastCommunication,
            boolean supportSchedulingChanges
            ) {
        m_schedulingQuantum = schedulingQuantum;
        m_lazyDelay = lazyDelay;
        
        m_supportProcessingArchitectureChanges = supportProcessingArchitectureChanges;
        m_supportReplicatedArchitecture = supportReplicatedArchitectures;
        m_supportAllCentralizedArchitectures = supportAllCentralizedArchitectures;
        m_supportedCentralizedArchitectureMasters = supportedCentralizedArchitectureMasters;
        m_supportMulticastCommunication = supportMulticastCommunication;
        m_supportSchedulingChanges = supportSchedulingChanges;
        
        String tradeoffPolicyFactoryClassName = AMainConfigParamProcessor.getInstance().getStringParam(
                Parameters.RESP_TIME_TRADEOFF_POLICY_FACTORY_CLASS_NAME
                );
        if ( tradeoffPolicyFactoryClassName == null || tradeoffPolicyFactoryClassName.equals( "" ) ) {
            tradeoffPolicyFactoryClassName = "selfoptsys.perf.tradeoff.ABasicRespTimeTradeoffPolicy";
        }
        
        try {
            RespTimeTradeoffPolicyFactory factory = 
                (RespTimeTradeoffPolicyFactory) Class.forName( tradeoffPolicyFactoryClassName ).newInstance();
            ARespTimeTradeoffPolicyFactorySelector.setFactory( factory );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASessionRegsitry: Error while selecting perf report collector factory",
                    e
                    );
        }

        
        m_respTimeTradeoffPolicy = ARespTimeTradeoffPolicyFactorySelector.getFactory().getRemoteResponseTimeTradeoffSpec();
        m_perUserSysConfigOptimizerInfoDeltaList = new Hashtable<Integer, List<ASysConfigOptimizationInfoDeltaListEntry>>();
    }
    
    public SysConfig optimizeCurrentSysConfig(
            SysConfig curSysConfig,
            Map<Integer, PerfCollectorUserInfo> userInfos,
            Map<ComputerInfo, ComputerStats> computerStatsCollection,
            Map<Integer,Map<Integer,Double>> latencies,
            double thinkTimeEstimate,
            double significantDiffInLocalRespTimes,
            double significantDiffInRemoteRespTimes
            ) {
        
        m_curSysConfig = curSysConfig;
        m_userInfos = userInfos;
        m_computerStatsCollection = computerStatsCollection;
        m_thinkTimeEstimate = thinkTimeEstimate;
        m_significantDiffInLocalRespTimes = significantDiffInLocalRespTimes;
        m_significantDiffInRemoteRespTimes = significantDiffInRemoteRespTimes;
        m_latencies = latencies;
        
        List<Integer> inputtingUsers = m_curSysConfig.getInputtingUserIndices();
        for ( int i = 0; i < inputtingUsers.size(); i++ ) {
            List<ASysConfigOptimizationInfoDeltaListEntry> deltaList = getDeltaListOfSysConfigInfosForSourceUser(
                    inputtingUsers.get( i )
                    );
            m_perUserSysConfigOptimizerInfoDeltaList.put(
                    inputtingUsers.get( i ),
                    deltaList
                    );
        }
        
        SysConfig bestSysConfig = pickOverallBestSysConfigOrKeepCurrentSysConfig( m_perUserSysConfigOptimizerInfoDeltaList );
        
        return bestSysConfig;
    }
    
    protected NetworkInfo createNetworkInfo(
            ProcessingArchitectureType procArchType
            ) {
        
        int numUsers = m_curSysConfig.getUserIndices().size();
        NetworkInfo nwInfo = new ANetworkInfo();
        nwInfo.setNumComputers( numUsers );
        nwInfo.setProcessingArchitecture( procArchType );
        
        double[] inputProcCosts = new double[ numUsers ];
        double[] inputTransCosts = new double[ numUsers ];
        double[] inputTransCostsToFirstDest = new double[ numUsers ];
        double[] observedInputTransCosts = new double[ numUsers ];
        double[] observedInputTransCostsToFirstDest = new double[ numUsers ];
        double[] outputProcCosts = new double[ numUsers ];
        double[] outputTransCosts = new double[ numUsers ];
        double[] outputTransCostsToFirstDest = new double[ numUsers ];
        double[] observedOutputTransCosts = new double[ numUsers ];
        double[] observedOutputTransCostsToFirstDest = new double[ numUsers ];
        Map<Integer, Integer> coreUsedByProcessingThread = new Hashtable<Integer, Integer>();
        Map<Integer, Integer> coreUsedByTransmissionThread = new Hashtable<Integer, Integer>();
        Map<Integer, Boolean> procAndTransThreadsShareCores = new Hashtable<Integer, Boolean>();
        
        for ( int i = 0; i < numUsers; i++ ) {
            int userIndex = m_curSysConfig.getUserIndices().get( i );
            ComputerInfo computerInfo = m_userInfos.get( userIndex ).getComputerInfo();
            ComputerStats computerStats = m_computerStatsCollection.get( computerInfo );

            coreUsedByProcessingThread.put(
                    userIndex,
                    m_userInfos.get( userIndex ).getCoreUsedByProcessingThread()
                    );
            coreUsedByTransmissionThread.put(
                    userIndex,
                    m_userInfos.get( userIndex ).getCoreUsedByTransmissionThread()
                    );
            procAndTransThreadsShareCores.put(
                    userIndex,
                    m_userInfos.get( userIndex ).getProcAndTransThreadsShareCores()
                    );
            
            if ( procArchType == ProcessingArchitectureType.REPLICATED ) {
                /*
                 * Make sure to set the correct input proc costs for source computer later
                 */
                inputProcCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                inputTransCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                inputTransCostsToFirstDest[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                observedInputTransCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                observedInputTransCostsToFirstDest[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                outputProcCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC,
                        true,
                        false,
                        m_computerStatsCollection
                        );
            }
            else if ( procArchType == ProcessingArchitectureType.CENTRALIZED ) {
                /*
                 * Make sure to set the correct output proc cost for master computer later
                 */
                inputProcCosts[ userIndex ] = -1;
                inputTransCosts[ userIndex ] = -1;
                outputProcCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                outputTransCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                outputTransCostsToFirstDest[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                observedOutputTransCosts[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME,
                        true,
                        false,
                        m_computerStatsCollection
                        );
                observedOutputTransCostsToFirstDest[ userIndex ] = computerStats.getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                        true,
                        false,
                        m_computerStatsCollection
                        );
            }
        }
        nwInfo.setInputProcCosts( inputProcCosts );
        nwInfo.setInputTransCosts( inputTransCosts );
        nwInfo.setInputTransCostsToFirstDest( inputTransCostsToFirstDest );
        nwInfo.setOutputProcCosts( outputProcCosts );
        nwInfo.setOutputTransCosts( outputTransCosts );
        nwInfo.setOutputTransCostsToFirstDest( outputTransCostsToFirstDest );
        nwInfo.setObservedInputTransCosts( observedInputTransCosts );
        nwInfo.setObservedInputTransCostsToFirstDest( observedInputTransCostsToFirstDest );
        nwInfo.setObservedOutputTransCosts( observedOutputTransCosts );
        nwInfo.setObservedOutputTransCostsToFirstDest( observedOutputTransCostsToFirstDest );
        nwInfo.setCoreUsedByProcessingThread( coreUsedByProcessingThread );
        nwInfo.setCoreUsedByTransmissionThread( coreUsedByTransmissionThread );
        nwInfo.setProcAndTransThreadShareCores( procAndTransThreadsShareCores );

        double[][] nwLatencies = new double[ numUsers ][ numUsers ];
        Iterator<Map.Entry<Integer, Map<Integer, Double>>> itr = m_latencies.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<Integer, Map<Integer, Double>> entry = itr.next();
            int curLocalUserIndex = entry.getKey();
            Iterator<Map.Entry<Integer, Double>> itr2 = entry.getValue().entrySet().iterator();
            while ( itr2.hasNext() ) {
                Map.Entry<Integer, Double> entry2 = itr2.next();
                int curRemoteUserIndex = entry2.getKey();
                double latency = entry2.getValue();
                
                nwLatencies[ curLocalUserIndex ][ curRemoteUserIndex ] = latency;
            }
        }
        nwInfo.setNetworkLatencies( nwLatencies );

        return nwInfo;
    }

    protected List<ASysConfigOptimizationInfoDeltaListEntry> getDeltaListOfSysConfigInfosForSourceUser(
            int sourceUserIndex
            ) {
        
        List<Integer> userIndices = m_curSysConfig.getUserIndices();

        List<ASysConfigOptimizationInfoDeltaListEntry> deltaList = null;
        List<ASysConfigOptimizerInfo> optimizationInfos = new LinkedList<ASysConfigOptimizerInfo>();
        ASysConfigOptimizerInfo optimizationInfo = null;
        
        /*
         * Build the overlays for the replicated and all centralized architectures.
         */
        OverlayBuilder overlayBuilder = null;
        Overlay overlay = null;
        
        ProcessingArchitectureType curProcArch = ProcessingArchitectureType.CENTRALIZED;
        int curMasterUserIndex = -1;
        if ( m_curSysConfig.getMasterUserIndices().size() == 1 ) {
            curMasterUserIndex = m_curSysConfig.getMasterUserIndices().get( 0 );
        }
        else {
            curProcArch = ProcessingArchitectureType.REPLICATED;
        }
        
        /*
         * Centralized Architecture
         */
        NetworkInfo centralizedNwInfo = createNetworkInfo( ProcessingArchitectureType.CENTRALIZED );
        for ( int i = 0 ; i < userIndices.size(); i++ ) {
            int potentialNewMasterUserIndex = userIndices.get( i );
            
            /*
             * If current architecture is centralized and current master user is the
             * potentialNewMasterUserIndex, then we can go ahead regardless of what
             * processing architectures are supported.
             */
            if ( curMasterUserIndex != potentialNewMasterUserIndex ) {

                if ( !m_supportProcessingArchitectureChanges && 
                        curProcArch == ProcessingArchitectureType.REPLICATED ) {
                    continue;
                } else if ( !m_supportAllCentralizedArchitectures &&
                        !m_supportedCentralizedArchitectureMasters.contains( potentialNewMasterUserIndex ) ) {
                    continue;
                }
                
            }
            
            centralizedNwInfo.setSourceComputer( potentialNewMasterUserIndex );
            
            adjustCentralizedProcessingArchitectureCosts(
                    potentialNewMasterUserIndex,
                    ( i > 0 ) ? userIndices.get( i - 1 ) : -1,
                            centralizedNwInfo
                    );

            if ( m_supportMulticastCommunication ) {
                overlayBuilder = new AnHmdmMulticastOverlayBuilder( centralizedNwInfo );
            }
            else {
                overlayBuilder = new AUnicastOverlayBuilder( centralizedNwInfo );
            }
            overlay = overlayBuilder.generateOverlay();
            
            optimizationInfo = findBestSchedulingPolicyForOverlay(
                    overlay,
                    centralizedNwInfo,
                    sourceUserIndex,
                    potentialNewMasterUserIndex
                    );

            optimizationInfos.add( optimizationInfo );
        }
        
        /*
         * Replicated Architecture
         */
        if ( m_supportProcessingArchitectureChanges ||
                ( m_supportReplicatedArchitecture && curProcArch == ProcessingArchitectureType.REPLICATED ) ) {
            
            NetworkInfo replicatedNwInfo = createNetworkInfo( ProcessingArchitectureType.REPLICATED );
            
            replicatedNwInfo.setProcessingArchitecture( ProcessingArchitectureType.REPLICATED );
            replicatedNwInfo.setSourceComputer( sourceUserIndex );
            
            adjustReplicatedProcessingArchitectureCosts(
                    sourceUserIndex,
                    replicatedNwInfo
                    );
            
            if ( m_supportMulticastCommunication ) {
                overlayBuilder = new AnHmdmMulticastOverlayBuilder( replicatedNwInfo );
            }
            else {
                overlayBuilder = new AUnicastOverlayBuilder( replicatedNwInfo );
            }
            overlay = overlayBuilder.generateOverlay();
    
            optimizationInfo = findBestSchedulingPolicyForOverlay(
                    overlay,
                    replicatedNwInfo,
                    sourceUserIndex,
                    -1
                    );
    
            optimizationInfos.add( optimizationInfo );
        }
        
        deltaList = createDeltaListOfSysConfigInfosForSourceUser(            
                optimizationInfos,
                sourceUserIndex
                );
        
        return deltaList;
        
    }
    
    protected ASysConfigOptimizerInfo findBestSchedulingPolicyForOverlay(
            Overlay overlay,
            NetworkInfo nwInfo,
            int actualSourceUserIndex,
            int masterUserIndex
            ) {
        
        ASysConfigOptimizerInfo optimizerInfo = new ASysConfigOptimizerInfo();
        
        SchedulingPolicy schedulingPolicy = SchedulingPolicy.UNDEFINED;
        
        List<SchedulingPolicy> schedPols = getListOfSchedulingPoliciesToUse();
        if ( schedPols == null || schedPols.size() == 0 ) {
            return null;
        }
        if ( m_supportSchedulingChanges == false ) {
            SchedulingPolicy current = m_curSysConfig.getSchedulingPolicies().get( actualSourceUserIndex );
            schedPols = new LinkedList<SchedulingPolicy>();
            schedPols.add( current );
        }
        
        
        OverlayAnalyzer analyzer = null;
        
        List<double[]> schedPolsResponseTimes = new LinkedList<double[]>();
        for ( int i = 0; i < schedPols.size(); i++ ) {
            schedPolsResponseTimes.add( null );
        }
        
        /*
         * TODO:
         * Handle new policy where trans and proc costs are kept on separate cores 
         */
        
        if ( schedPols.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) &&
                ( m_supportSchedulingChanges || 
                        m_curSysConfig.getSchedulingPolicies().get( actualSourceUserIndex ) == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
            analyzer = new AProcFirstOverlayAnalyzer( nwInfo );
            double[] procFirstResponseTimes = analyzer.analyzeOverlay( overlay );
            procFirstResponseTimes = adjustResponseTimesForSlaveSourceUser(
                    procFirstResponseTimes,
                    actualSourceUserIndex,
                    nwInfo
                    );
            schedPolsResponseTimes.set( 
                    schedPols.indexOf( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ),
                    procFirstResponseTimes
                    );
        }
        
        if ( schedPols.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) &&
                ( m_supportSchedulingChanges || 
                        m_curSysConfig.getSchedulingPolicies().get( actualSourceUserIndex ) == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
            analyzer = new ATransFirstOverlayAnalyzer( nwInfo );
            double[] transFirstResponseTimes = analyzer.analyzeOverlay( overlay );
            transFirstResponseTimes = adjustResponseTimesForSlaveSourceUser(
                    transFirstResponseTimes,
                    actualSourceUserIndex,
                    nwInfo
                    );
            schedPolsResponseTimes.set( 
                    schedPols.indexOf( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ),
                    transFirstResponseTimes
                    );
        }
        
        if ( schedPols.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT  ) &&
                ( m_supportSchedulingChanges || 
                        m_curSysConfig.getSchedulingPolicies().get( actualSourceUserIndex ) == SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
            analyzer = new AConcOverlayAnalyzer( nwInfo, m_schedulingQuantum );
            double[] concResponseTimes = analyzer.analyzeOverlay( overlay );
            concResponseTimes = adjustResponseTimesForSlaveSourceUser(
                    concResponseTimes,
                    actualSourceUserIndex,
                    nwInfo
                    );
            schedPolsResponseTimes.set( 
                    schedPols.indexOf( SchedulingPolicy.MULTI_CORE_CONCURRENT ),
                    concResponseTimes
                    );
        }
        
        if ( schedPols.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) &&
                ( m_supportSchedulingChanges || 
                        m_curSysConfig.getSchedulingPolicies().get( actualSourceUserIndex ) == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
            analyzer = new ALazyProcessFirstOverlayAnalyzer( nwInfo, m_lazyDelay, m_lazyDelay );
            double[] lazyResponseTimes = analyzer.analyzeOverlay( overlay );
            lazyResponseTimes = adjustResponseTimesForSlaveSourceUser(
                    lazyResponseTimes,
                    actualSourceUserIndex,
                    nwInfo
                    );
            schedPolsResponseTimes.set( 
                    schedPols.indexOf( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ),
                    lazyResponseTimes
                    );
        }
        
        int compResult = 0;
        schedulingPolicy = schedPols.get( 0 );
        double[] bestResponseTimes = schedPolsResponseTimes.get( 0 );
        SchedulingPolicy bestSchedPol = schedPols.get( 0 );
        
        for ( int i = 1; i < schedPols.size(); i++ ) {
            compResult = m_respTimeTradeoffPolicy.compareResponseTimes( 
                    actualSourceUserIndex,
                    masterUserIndex,
                    masterUserIndex,
                    m_significantDiffInLocalRespTimes,
                    m_significantDiffInRemoteRespTimes,
                    bestResponseTimes,
                    schedPolsResponseTimes.get( i ),
                    bestSchedPol,
                    schedPols.get( i )
                    );
            if ( compResult > 0 ) {
                schedulingPolicy = schedPols.get( i );
                bestResponseTimes = schedPolsResponseTimes.get( i );
            }
        }

        optimizerInfo.Overlay = overlay;
        optimizerInfo.SchedulingPolicy = schedulingPolicy;
        optimizerInfo.ProcessingArchitectureType = nwInfo.getProcessingArchitecture();
        optimizerInfo.MasterUserIndex = nwInfo.getSourceComputer();
        optimizerInfo.ResponseTimes = bestResponseTimes;
        
        return optimizerInfo;
        
    }
    
    protected double[] adjustResponseTimesForSlaveSourceUser(
            double[] responseTimes,
            int sourceUserIndex,
            NetworkInfo nwInfo
            ) {
        
        if ( nwInfo.getProcessingArchitecture() == ProcessingArchitectureType.REPLICATED ) {
            return responseTimes;
        }
        else if ( nwInfo.getProcessingArchitecture() == ProcessingArchitectureType.CENTRALIZED &&
                nwInfo.getSourceComputer() == sourceUserIndex ) {
            return responseTimes;
        }
        
        double[] adjustedResponseTimes = new double[ responseTimes.length ];
        
        double adjustment = nwInfo.getInputTransCosts()[ sourceUserIndex ] +
            nwInfo.getNetworkLatencies()[ sourceUserIndex ][ nwInfo.getSourceComputer() ];
        for ( int i = 0; i < responseTimes.length; i++ ) {
            adjustedResponseTimes[i] = responseTimes[ i ] + adjustment;
        }
        
        return adjustedResponseTimes;
        
    }
    
    protected List<ASysConfigOptimizationInfoDeltaListEntry> createDeltaListOfSysConfigInfosForSourceUser(
            List<ASysConfigOptimizerInfo> optimizationInfos,
            int sourceUserIndex
            ) {
        
        List<ASysConfigOptimizationInfoDeltaListEntry> deltaList = 
            new LinkedList<ASysConfigOptimizationInfoDeltaListEntry>();
        
        for ( int i = 0; i < optimizationInfos.size(); i++ ) {
            ASysConfigOptimizationInfoDeltaListEntry deltaListEntry = null;
            
            deltaListEntry = new ASysConfigOptimizationInfoDeltaListEntry();
            if ( deltaList.size() == 0 ) {
                deltaListEntry.ASysConfigOptimizerInfo = optimizationInfos.get( i );
                deltaListEntry.NumRespTimesSignificantlyWorseThanBestEntry = 0;
                deltaList.add( deltaListEntry );
                continue;
            }
            
            int compResult = m_respTimeTradeoffPolicy.compareResponseTimes(
                    sourceUserIndex,
                    deltaList.get( 0 ).ASysConfigOptimizerInfo.MasterUserIndex,
                    optimizationInfos.get( i ).MasterUserIndex,
                    m_significantDiffInLocalRespTimes,
                    m_significantDiffInRemoteRespTimes,
                    deltaList.get( 0 ).ASysConfigOptimizerInfo.ResponseTimes,
                    optimizationInfos.get( i ).ResponseTimes,
                    deltaList.get( 0 ).ASysConfigOptimizerInfo.SchedulingPolicy,
                    optimizationInfos.get( i ).SchedulingPolicy
                    );
            deltaListEntry.NumRespTimesSignificantlyWorseThanBestEntry = compResult;
            deltaListEntry.ASysConfigOptimizerInfo = optimizationInfos.get( i );

            if ( deltaListEntry.NumRespTimesSignificantlyWorseThanBestEntry < 0 ) {
                for ( int j = 1; j < deltaList.size(); j++ ) {
                    deltaList.get( j ).NumRespTimesSignificantlyWorseThanBestEntry += deltaListEntry.NumRespTimesSignificantlyWorseThanBestEntry;
                }
                deltaListEntry.NumRespTimesSignificantlyWorseThanBestEntry = 0;
            }
            
            deltaList.add( deltaListEntry );
        }
        
        return deltaList;
        
    }
    
    protected SysConfig pickOverallBestSysConfigOrKeepCurrentSysConfig(
            Map<Integer, List<ASysConfigOptimizationInfoDeltaListEntry>> perUserSysConfigOptimizerInfoDeltaList
            ) {
        
        int numUsers = perUserSysConfigOptimizerInfoDeltaList.size();
        int numDeltas = perUserSysConfigOptimizerInfoDeltaList.values().iterator().next().size();
        int[][] deltas = new int[ numUsers ][ numDeltas ];
        
        int index = 0;
        Iterator<List<ASysConfigOptimizationInfoDeltaListEntry>> itr = 
            perUserSysConfigOptimizerInfoDeltaList.values().iterator();
        while ( itr.hasNext() ) {
            List<ASysConfigOptimizationInfoDeltaListEntry> deltaList = itr.next();
            for ( int i = 0; i < deltaList.size(); i++ ) {
                deltas[ index ][ i ] = deltaList.get( i ).NumRespTimesSignificantlyWorseThanBestEntry;
            }
            index++;
        }

        int bestCol = 0;
        double bestSum = 0;
        for ( int i = 0; i < numDeltas; i++ ) {
            double newBestSum = 0;
            for ( int j = 0; j < numUsers; j++ ) {
                newBestSum += deltas[ j ][ i ];
            }
            if ( newBestSum > bestSum ) {
                bestSum = newBestSum;
                bestCol = i;
            }
        }
        
        
        /*
         * Build best ASysConfig
         */
        HashMap<Integer, Overlay> overlays = new HashMap<Integer, Overlay>();
        HashMap<Integer, SchedulingPolicy> schedulingPolicies = new HashMap<Integer, SchedulingPolicy>();
        Vector<Integer> newInputtingUsersIndices = new Vector<Integer>();

        Iterator<Map.Entry<Integer, List<ASysConfigOptimizationInfoDeltaListEntry>>> itr2 = 
            perUserSysConfigOptimizerInfoDeltaList.entrySet().iterator();
        while ( itr2.hasNext() ) {
            Map.Entry<Integer, List<ASysConfigOptimizationInfoDeltaListEntry>> entry = itr2.next();
            overlays.put(
                    entry.getKey(),
                    entry.getValue().get( bestCol ).ASysConfigOptimizerInfo.Overlay
                    );
            schedulingPolicies.put(
                    entry.getKey(),
                    entry.getValue().get( bestCol ).ASysConfigOptimizerInfo.SchedulingPolicy
                    );
            newInputtingUsersIndices.add( entry.getKey() );
        }
        
        List<Integer> newUserIndices = new LinkedList<Integer>();
        List<Integer> newMasterUsersIndices = new LinkedList<Integer>();
        ASysConfigOptimizerInfo sampleBestOptimizationInfo =  
            perUserSysConfigOptimizerInfoDeltaList.values().iterator().next().get( bestCol ).ASysConfigOptimizerInfo;
        
        int[] userIndices = sampleBestOptimizationInfo.Overlay.getAddOrder();
        for ( int i = 0; i < userIndices.length; i++ ) {
            newUserIndices.add( userIndices[ i ] );
        }
        
        if ( sampleBestOptimizationInfo.ProcessingArchitectureType == ProcessingArchitectureType.REPLICATED ) {
            for ( int i = 0; i < userIndices.length; i++ ) {
                newMasterUsersIndices.add( userIndices[ i ] );
            }
        }
        else if ( sampleBestOptimizationInfo.ProcessingArchitectureType == ProcessingArchitectureType.CENTRALIZED ) {
            newMasterUsersIndices.add( sampleBestOptimizationInfo.MasterUserIndex );
        }
        
        SysConfig newBestSysConfig = new ASysConfig(
                m_curSysConfig.getSystemConfigurationVersion(),
                newUserIndices,
                newMasterUsersIndices,
                newInputtingUsersIndices,
                null,
                overlays,
                schedulingPolicies
                );
        
        if ( m_curSysConfig.equals( newBestSysConfig ) ) {
            return m_curSysConfig;
        }
            
        return newBestSysConfig;
    }
    
    protected List<SchedulingPolicy> getListOfSchedulingPoliciesToUse() {
        List<SchedulingPolicy> schedPolicies = new LinkedList<SchedulingPolicy>();

        schedPolicies.add( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST );
        schedPolicies.add( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST );
        schedPolicies.add( SchedulingPolicy.MULTI_CORE_CONCURRENT );
        schedPolicies.add( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST );
        
        return schedPolicies;
    }
    
    protected void adjustCentralizedProcessingArchitectureCosts(
            int newMasterUserIndex,
            int oldMasterUserIndex,
            NetworkInfo nwInfo
            ) {
        
        ComputerInfo computerInfo = m_userInfos.get( newMasterUserIndex ).getComputerInfo();
        ComputerStats computerStats = m_computerStatsCollection.get( computerInfo );
        nwInfo.getInputProcCosts()[ newMasterUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getInputTransCosts()[ newMasterUserIndex ] = 0;
        nwInfo.getObservedInputTransCosts()[ newMasterUserIndex ] = 0;
        nwInfo.getOutputProcCosts()[ newMasterUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getOutputTransCosts()[ newMasterUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getOutputTransCostsToFirstDest()[ newMasterUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getObservedOutputTransCosts()[ newMasterUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getObservedOutputTransCostsToFirstDest()[ newMasterUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                true,
                false,
                m_computerStatsCollection
                );
        if ( oldMasterUserIndex > 0 ) {
            computerInfo = m_userInfos.get( oldMasterUserIndex ).getComputerInfo();
            computerStats = m_computerStatsCollection.get( computerInfo );
            nwInfo.getInputProcCosts()[ oldMasterUserIndex ] = -1;
            nwInfo.getInputTransCosts()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getInputTransCostsToFirstDest()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getObservedInputTransCosts()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getObservedInputTransCostsToFirstDest()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getOutputProcCosts()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getOutputTransCosts()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getOutputTransCostsToFirstDest()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getObservedOutputTransCosts()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME,
                    true,
                    false,
                    m_computerStatsCollection
                    );
            nwInfo.getObservedOutputTransCostsToFirstDest()[ oldMasterUserIndex ] = computerStats.getCostEstimate(
                    PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                    true,
                    false,
                    m_computerStatsCollection
                    );
        }
        
    }
    
    protected void adjustReplicatedProcessingArchitectureCosts(
            int sourceUserIndex,
            NetworkInfo nwInfo
            ) {
        
        ComputerInfo computerInfo = m_userInfos.get( sourceUserIndex ).getComputerInfo();
        ComputerStats computerStats = m_computerStatsCollection.get( computerInfo );
        nwInfo.getInputProcCosts()[ sourceUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getInputTransCosts()[ sourceUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getInputTransCostsToFirstDest()[ sourceUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getObservedInputTransCosts()[ sourceUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getObservedInputTransCostsToFirstDest()[ sourceUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                true,
                false,
                m_computerStatsCollection
                );
        nwInfo.getOutputProcCosts()[ sourceUserIndex ] = computerStats.getCostEstimate(
                PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC,
                true,
                false,
                m_computerStatsCollection
                );
        
    }
    
    protected OverlayAnalyzer getOverlayAnalyzerForUser(
            SysConfig sysConfig,
            int userIndex,
            NetworkInfo nwInfo
            ) {
        
        OverlayAnalyzer analyzer = null;
        
        SchedulingPolicy schedPol = sysConfig.getSchedulingPolicies().get( userIndex );
        if ( schedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
            analyzer = new ALazyProcessFirstOverlayAnalyzer(
                    nwInfo,
                    m_lazyDelay,
                    m_lazyDelay
                    );
                    
        }
        else if ( schedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
            analyzer = new AConcOverlayAnalyzer(
                    nwInfo,
                    m_schedulingQuantum
                    );
        }
        else if ( schedPol == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
            analyzer = new AProcFirstOverlayAnalyzer( nwInfo );
        }
        else if ( schedPol == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
            analyzer = new ATransFirstOverlayAnalyzer( nwInfo );
        }
        
        return analyzer;
        
    }
}
