package selfoptsys.perf;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import selfoptsys.network.*;

import commonutils.basic.*;
import commonutils.config.*;

public class APerformanceOptimizationReportsForCommandProcessor {

    protected int m_sourceUserIndex;
    protected int m_seqId;
    protected SysConfig m_sysConfig;
    protected List<Integer> m_usersWhoWillReport;
    protected Map<Integer, Long> m_clockSkews;
    protected String m_uniqueReportsForCommandId;
    
    protected List<Integer> m_remainingUsersWhoWillReport;
    
    protected ProcessingArchitectureType m_processingArchitecture;
    protected SchedulingPolicy m_schedulingPolicy;
    
    protected Map<Integer, APerformanceOptimizationReport> m_receivedReports;
    
    protected Map<Integer, Map<PerformanceParameterType, Double>> m_processedData;
    
    protected PerformanceParameterType[] m_replicatedCpuPerformancePerformanceParameterTypes = {
            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC,
            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS,
            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST,
            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC,
            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC,
            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS,
            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST,
            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC
    };
    protected PerformanceParameterType[] m_centralizedCpuPerformancePerformanceParameterTypes = {
            PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC,
            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC,
            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS,
            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST,
            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS,
            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST,
            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC,
            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS,
            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST
    };
    protected PerformanceParameterType[] m_replicatedNetworkCardPerformanceParameterTypes = {
            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME
    };
    protected PerformanceParameterType[] m_centralizedNetworkCardPerformanceParameterTypes = {
            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME,
            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME
    };
    
    public APerformanceOptimizationReportsForCommandProcessor(
            int sourceUserIndex,
            int seqId,
            SysConfig sysConfig,
            List<Integer> usersWhoWillReport,
            Map<Integer, Long> clockSkews
            ) {
        m_sourceUserIndex = sourceUserIndex;
        m_seqId = seqId;
        m_sysConfig = sysConfig;
        m_usersWhoWillReport = usersWhoWillReport;
        m_clockSkews = clockSkews;
        
        m_uniqueReportsForCommandId = m_sourceUserIndex + "_" + m_seqId;
        
        m_remainingUsersWhoWillReport = new LinkedList<Integer>();
        for ( Integer userIndex : m_usersWhoWillReport ) {
            m_remainingUsersWhoWillReport.add( userIndex );
        }
        m_receivedReports = new Hashtable<Integer, APerformanceOptimizationReport>();
        m_processedData = new Hashtable<Integer, Map<PerformanceParameterType,Double>>();
        
        m_processingArchitecture = m_sysConfig.getMasterUserIndices().size() == 1 ? 
                ProcessingArchitectureType.CENTRALIZED : ProcessingArchitectureType.REPLICATED;
        m_schedulingPolicy = m_sysConfig.getSchedulingPolicies().get( m_sourceUserIndex );
    }
    
    public String getUniqueReportsForCommandId() {
        return m_uniqueReportsForCommandId;
    }
    
    public boolean getHasBeenFullyProcessed() {
        return m_remainingUsersWhoWillReport.size() == 0;
    }
    
    public Map<Integer, Map<PerformanceParameterType, Double>> getProcessedData() {
        return m_processedData;
    }
    
    public void addPerformanceOptimizationReport(
            APerformanceOptimizationReport performanceOptimizationReport
            ) {
        int reportingUserIndex = performanceOptimizationReport.UserIndex;
        if ( m_remainingUsersWhoWillReport.contains( reportingUserIndex ) == false ) {
            ErrorHandlingUtils.logSevereMessageAndContinue(
                    "APerformanceOptimizationReportsForCommandProcessor::addPerformanceOptimizationReport: Received report from unexpected user"
                    );
        }
        
        adjustPerfReportMsgTimesForClockSkew( performanceOptimizationReport );
        
        m_remainingUsersWhoWillReport.remove( new Integer( reportingUserIndex ) );
        m_receivedReports.put(
                reportingUserIndex,
                performanceOptimizationReport
                );
        
        if ( m_remainingUsersWhoWillReport.size() == 0 ) {
            processData();
        }
        
    }
    
    private void processData() {

        Iterator<Map.Entry<Integer, APerformanceOptimizationReport>> itr = m_receivedReports.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<Integer, APerformanceOptimizationReport> entry = itr.next();
            int reportingUserIndex = entry.getKey();
            APerformanceOptimizationReport report = entry.getValue();
            
            Map<PerformanceParameterType, Double> userData = null;
            if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
                return;
            }
            else if ( m_schedulingPolicy == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
                userData = processDataWithLazyPolicy( report );
            }
            else {
                userData = processDataWithSequentialOrParallelPolicy( report );
            }

            m_processedData.put(
                    reportingUserIndex,
                    userData
                    );
        }
        
    }
    
    private Map<PerformanceParameterType, Double> processDataWithSequentialOrParallelPolicy(
            APerformanceOptimizationReport report
            ) {
        Map<PerformanceParameterType, Double> performanceData = new Hashtable<PerformanceParameterType, Double>();
        
        int reportingUserIndex = report.UserIndex;
        boolean reportingUserIsSource = report.CmdSourceUserIndex == reportingUserIndex;
        boolean reportingUserIsMaster = m_sysConfig.getMasterUserIndices().contains( reportingUserIndex );
        ProcessingArchitectureType processingArchitecture = m_sysConfig.getMasterUserIndices().size() > 1 ? 
                ProcessingArchitectureType.REPLICATED : ProcessingArchitectureType.CENTRALIZED;
        Overlay overlayRootedAtSourceUser = m_sysConfig.getNetworkOverlays().get( report.CmdSourceUserIndex );
        int numDestsReportingUserTransmitsTo = overlayRootedAtSourceUser.getChildrenOf( reportingUserIndex ).size();
        
        if ( processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            if ( reportingUserIsSource ) {
                performanceData.put(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC,
                        convertNanosToMillis( report.InputProcTime )
                        );
                performanceData.put(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC,
                        convertNanosToMillis( report.OutputProcTime )
                        );
                if ( numDestsReportingUserTransmitsTo > 0 ) {
                    performanceData.put(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS,
                            MathUtils.round( convertNanosToMillis( report.InputTransTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST,
                            convertNanosToMillis( report.InputTransTimeToFirstDest )
                            );
                    
                    List<Integer> children = overlayRootedAtSourceUser.getChildrenOf( reportingUserIndex );
                    int firstDestUserIndex = children.get( 0 );
                    int lastDestUserIndex = children.get( children.size() - 1 );
                    
                    APerformanceOptimizationReport firstDestReport = m_receivedReports.get( firstDestUserIndex );
                    APerformanceOptimizationReport lastDestReport = m_receivedReports.get( lastDestUserIndex );
                    
                    long transTimeToFirstDestBasedOnObservedTime = firstDestReport.InputOriginalReceiveTime - report.InputTransStartTime;
                    long transTimeToLastDestBasedOnObservedTime = lastDestReport.InputOriginalReceiveTime - report.InputTransStartTime;

                    performanceData.put(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
                            MathUtils.round( convertNanosToMillis( transTimeToLastDestBasedOnObservedTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                            convertNanosToMillis( transTimeToFirstDestBasedOnObservedTime )
                            );
                }
            }
            else {
                performanceData.put(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC,
                        convertNanosToMillis( report.InputProcTime )
                        );
                performanceData.put(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC,
                        convertNanosToMillis( report.OutputProcTime )
                        );
                if ( numDestsReportingUserTransmitsTo > 0 ) {
                    performanceData.put(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS,
                            MathUtils.round( convertNanosToMillis( report.InputTransTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST,
                            convertNanosToMillis( report.InputTransTimeToFirstDest )
                            );

                    List<Integer> children = overlayRootedAtSourceUser.getChildrenOf( reportingUserIndex );
                    int firstDestUserIndex = children.get( 0 );
                    int lastDestUserIndex = children.get( children.size() - 1 );
                    
                    APerformanceOptimizationReport firstDestReport = m_receivedReports.get( firstDestUserIndex );
                    APerformanceOptimizationReport lastDestReport = m_receivedReports.get( lastDestUserIndex );
                    
                    long transTimeToFirstDestBasedOnObservedTime = firstDestReport.InputOriginalReceiveTime - report.InputTransStartTime;
                    long transTimeToLastDestBasedOnObservedTime = lastDestReport.InputOriginalReceiveTime - report.InputTransStartTime;

                    performanceData.put(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
                            MathUtils.round( convertNanosToMillis( transTimeToLastDestBasedOnObservedTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                            convertNanosToMillis( transTimeToFirstDestBasedOnObservedTime )
                            );
                }
            }
        }
        else if ( processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            if ( reportingUserIsMaster ) {
                performanceData.put(
                        PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC,
                        convertNanosToMillis( report.InputProcTime )
                        );
                performanceData.put(
                        PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC,
                        convertNanosToMillis( report.OutputProcTime )
                        );
                if ( numDestsReportingUserTransmitsTo > 0 ) {
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS,
                            MathUtils.round( convertNanosToMillis( report.OutputTransTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST,
                            convertNanosToMillis( report.OutputTransTimeToFirstDest )
                            );
                    
                    List<Integer> children = overlayRootedAtSourceUser.getChildrenOf( reportingUserIndex );
                    int firstDestUserIndex = children.get( 0 );
                    int lastDestUserIndex = children.get( children.size() - 1 );
                    
                    APerformanceOptimizationReport firstDestReport = m_receivedReports.get( firstDestUserIndex );
                    APerformanceOptimizationReport lastDestReport = m_receivedReports.get( lastDestUserIndex );
                    
                    long transTimeToFirstDestBasedOnObservedTime = firstDestReport.OutputOriginalReceiveTime - report.OutputTransStartTime;
                    long transTimeToLastDestBasedOnObservedTime = lastDestReport.OutputOriginalReceiveTime - report.OutputTransStartTime;

                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME,
                            MathUtils.round( convertNanosToMillis( transTimeToLastDestBasedOnObservedTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                            convertNanosToMillis( transTimeToFirstDestBasedOnObservedTime )
                            );
                }
            }
            else {
                performanceData.put(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC,
                        convertNanosToMillis( report.OutputProcTime )
                        );
                if ( numDestsReportingUserTransmitsTo > 0 ) {
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST,
                            MathUtils.round( convertNanosToMillis( report.OutputTransTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST,
                            convertNanosToMillis( report.OutputTransTimeToFirstDest )
                            );
                    
                    List<Integer> children = overlayRootedAtSourceUser.getChildrenOf( reportingUserIndex );
                    int firstDestUserIndex = children.get( 0 );
                    int lastDestUserIndex = children.get( children.size() - 1 );
                    
                    APerformanceOptimizationReport firstDestReport = m_receivedReports.get( firstDestUserIndex );
                    APerformanceOptimizationReport lastDestReport = m_receivedReports.get( lastDestUserIndex );
                    
                    long transTimeToFirstDestBasedOnObservedTime = firstDestReport.OutputOriginalReceiveTime - report.OutputTransStartTime;
                    long transTimeToLastDestBasedOnObservedTime = lastDestReport.OutputOriginalReceiveTime - report.OutputTransStartTime;

                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                            MathUtils.round( convertNanosToMillis( transTimeToLastDestBasedOnObservedTime ) / numDestsReportingUserTransmitsTo, 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                            convertNanosToMillis( transTimeToFirstDestBasedOnObservedTime )
                            );
                }
                if ( reportingUserIsSource ) {
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS,
                            MathUtils.round( convertNanosToMillis( report.InputTransStartTime ), 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST,
                            MathUtils.round( convertNanosToMillis( report.InputTransTime ), 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST,
                            convertNanosToMillis( report.InputTransTimeToFirstDest )
                            );
                    
                    List<Integer> children = overlayRootedAtSourceUser.getChildrenOf( reportingUserIndex );
                    int firstDestUserIndex = children.get( 0 );
                    
                    APerformanceOptimizationReport firstDestReport = m_receivedReports.get( firstDestUserIndex );
                    
                    long transTimeToFirstDestBasedOnObservedTime = firstDestReport.OutputOriginalReceiveTime - report.OutputTransStartTime;

                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME,
                            MathUtils.round( convertNanosToMillis( transTimeToFirstDestBasedOnObservedTime ), 3 )
                            );
                    performanceData.put(
                            PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME,
                            convertNanosToMillis( transTimeToFirstDestBasedOnObservedTime )
                            );
                }
            }
        }
        
        return performanceData;
    }
    
    private Map<PerformanceParameterType, Double> processDataWithLazyPolicy(
            APerformanceOptimizationReport report
            ) {
        Map<PerformanceParameterType, Double> performanceData = new Hashtable<PerformanceParameterType, Double>();
        
        return performanceData;
    }
    
    private void adjustPerfReportMsgTimesForClockSkew(
            APerformanceOptimizationReport performanceOptimizationReport
            ) {
        long clockSkew = m_clockSkews.get( performanceOptimizationReport.UserIndex );
        
        if ( performanceOptimizationReport.InputProcStartTime != 0 ) {
            performanceOptimizationReport.InputProcStartTime += clockSkew;
        }
        if ( performanceOptimizationReport.OutputProcStartTime != 0 ) {
            performanceOptimizationReport.OutputProcStartTime += clockSkew;
        }
        if ( performanceOptimizationReport.InputTransStartTime != 0 ) {
            performanceOptimizationReport.InputTransStartTime += clockSkew;
        }
        if ( performanceOptimizationReport.OutputTransStartTime != 0 ) {
            performanceOptimizationReport.OutputTransStartTime += clockSkew;
        }
        if ( performanceOptimizationReport.InputOriginalReceiveTime != 0 ) {
            performanceOptimizationReport.InputOriginalReceiveTime += clockSkew;
        }
        if ( performanceOptimizationReport.OutputOriginalReceiveTime != 0 ) {
            performanceOptimizationReport.OutputOriginalReceiveTime += clockSkew;
        }
    }

    
    private double convertNanosToMillis(
            long value
            ) {
        return MathUtils.round( (double) ( value / (double) 1000000 ), 3 );
    }
    
    public boolean equals(
            Object other
            ) {
        return ( (APerformanceOptimizationReportsForCommandProcessor) other ).getUniqueReportsForCommandId().equals( m_uniqueReportsForCommandId );
    }
    
    public int hashCode() {
        return m_uniqueReportsForCommandId.hashCode();
    }
    

}
