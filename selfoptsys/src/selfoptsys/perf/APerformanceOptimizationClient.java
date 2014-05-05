package selfoptsys.perf;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.config.*;

import commonutils.basic.*;
import commonutils.scheduling.*;
import commonutils.threadpool.*;

import winsyslib.*;

public class APerformanceOptimizationClient
    implements PerformanceOptimizationClient {

    protected int m_userIndex;
    protected PerformanceOptimizationUserInfo m_performanceOptimizationUserInfo;
    
    protected PerformanceOptimizationServer m_performanceOptimizationServer;

    protected NetworkLatencyCollectorClient m_networkLatencyCollectorClient;
    
    protected MessageForwarder m_performanceReportForwarder;
    protected List<APerformanceOptimizationReport> m_incompletePerformanceOptimizationReports;
    protected boolean m_quittingFlag = false; 
    protected boolean m_receivedOutputTransReport = false;

    public APerformanceOptimizationClient(
            int userIndex,
            PerformanceOptimizationUserInfo performanceOptimizationUserInfo
            ) {
        m_userIndex = userIndex;
        m_performanceOptimizationUserInfo = performanceOptimizationUserInfo;

        m_incompletePerformanceOptimizationReports = new LinkedList<APerformanceOptimizationReport>();
        
        m_networkLatencyCollectorClient = new ANetworkLatencyCollectorClient();
    }
    
    public void registerWithPerformanceOptimizationServer(
            PerformanceOptimizationServer performanceOptimizationServer
            ) {
        m_performanceOptimizationServer = performanceOptimizationServer;
        
        /*
         * Gather local computer information
         */

        String processorName = "";
        String processorIdentifier = "";
        long processorSpeed = 0;
        if ( m_performanceOptimizationUserInfo.getHasAFakeLoggable() == false ) {
            processorName = WinSysLibUtilities.getProcessorName();
            processorIdentifier = WinSysLibUtilities.getProcessorIdentifier();
            processorSpeed = WinSysLibUtilities.getProcessorSpeed();
        }
        else {
            processorName = "Intel(R) Core(TM)2 Duo CPU     E4400  @ 2.00GHz";
            processorIdentifier = "x86 Family 6 Model 15 Stepping 13";
            processorSpeed = 2000;
        }
        ComputerInfo computerInfo = new AComputerInfo(
                processorIdentifier,
                processorName,
                processorSpeed
                );
        m_performanceOptimizationUserInfo.setComputerInfo( computerInfo );
        
        /*
         * Register user with performance optimization server
         */

        try {
            m_performanceOptimizationUserInfo.setHost( 
                    InetAddress.getLocalHost()
                    );
            m_performanceOptimizationUserInfo.setNetworkLatencyCollectorClient(
                    m_networkLatencyCollectorClient.getRmiStub() 
                    );
            m_performanceOptimizationServer.registerUser( 
                    m_performanceOptimizationUserInfo
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::registerPerformanceServer: Failed to register user with performance optimization server",
                    e
                    );
        }
        
        /*
         * Perform clock synchronization between local computer and performance optimization server
         */
        try {
            if ( m_performanceOptimizationUserInfo.getCollectPerformanceData() ) {
                ClockSyncClient clockSyncClient = new AClockSyncClient(
                        m_userIndex,
                        performanceOptimizationServer.getClockSyncServer()
                        );
                clockSyncClient.sync();
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::registerPerformanceServer: Failed to synchronize clocks with performance optimization server",
                    e
                    );
        }
        
        /*
         * Setup reporting channel to performance optimization server
         */
        
        try {
            m_performanceReportForwarder = ( new ANonBlockingTCPMessageForwarderFactory() ).createMessageForwarder(
                    m_userIndex
                    );
            MessageDest performanceOptimizationServerMessageDest = m_performanceOptimizationServer.getMessageDest();
            m_performanceReportForwarder.addDest(
                    Constants.PERFORMANCE_OPTIMIZATION_SERVER_USER_INDEX,
                    performanceOptimizationServerMessageDest
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALogger::registerPerformanceServer(): Exception occurred while setting up performance optimization report sender",
                    e
                    );
        }
        
    }
    
    public void unregisterFromPerformanceOptimizationServer() {
        try {
            m_performanceOptimizationServer.unregisterUser(
                    m_userIndex
                    );
            m_performanceReportForwarder.removeDest(
                    Constants.PERFORMANCE_OPTIMIZATION_SERVER_USER_INDEX
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerformanceOptimizationClient::unregisterFromPerformanceOptimizationServer(): Exception occured while unregistering from performance optimization server",
                    e
                    );
        }        
    }
    
    public void teardown() {
        unregisterFromPerformanceOptimizationServer();
    }
    
    public void sendPerformanceReport(
            MessageType msgType,
            int sysConfigVersion,
            int cmdSourceUserIndex,
            int seqId,
            long procTime,
            long transTime,
            long transTimeToFirstDest,
            int numDestsTransmittedTo,
            int lastDestTransmittedTo,
            double thinkTime,
            int coreNum,
            boolean reportProcCost,
            boolean reportTransCost
            ) {
        
        if ( m_performanceOptimizationUserInfo.getCollectPerformanceData() == false ) {
            return;
        }
        
        if ( msgType == MessageType.PERF_INPUT_ENTERED || 
                msgType == MessageType.PERF_INPUT_PROC_TIME ||
                msgType == MessageType.PERF_OUTPUT_PROC_TIME ) {
            if ( m_performanceOptimizationUserInfo.getAutoReportProcCosts() == false &&  
                    reportProcCost == false ) {
                return;
            }
        }
        
        if ( msgType == MessageType.PERF_INPUT_RECEIVED_TIME ||
                msgType == MessageType.PERF_INPUT_TRANS_TIME ||
                msgType == MessageType.PERF_OUTPUT_RECEIVED_TIME ||
                msgType == MessageType.PERF_OUTPUT_TRANS_TIME ) {
            if ( m_performanceOptimizationUserInfo.getAutoReportTransCosts() == false &&  
                    reportTransCost == false ) {
                return;
            }
        }
            
        try {
            APerformanceOptimizationReport report = null;
            boolean newReport = true;
            for ( int i = 0; i < m_incompletePerformanceOptimizationReports.size(); i++ ) {
                if ( m_incompletePerformanceOptimizationReports.get( i ).CmdSourceUserIndex == cmdSourceUserIndex &&
                        m_incompletePerformanceOptimizationReports.get( i ).SeqId == seqId ) {
                    report = m_incompletePerformanceOptimizationReports.get( i );
                    newReport = false;
                    break;
                }
            }
            if ( report == null ) {
                report = new APerformanceOptimizationReport();
                report.UserIndex = m_userIndex;
                report.SysConfigVersion = sysConfigVersion;
                report.CmdSourceUserIndex = cmdSourceUserIndex;
                report.SeqId = seqId;
                newReport = true;
            } 

            if ( msgType == MessageType.PERF_INPUT_ENTERED ) {
                report.ThinkTime = thinkTime;
            }
            else if ( msgType == MessageType.PERF_INPUT_PROC_TIME ) {
                report.InputProcTime = procTime;
                report.InputProcStartTime = System.nanoTime() - procTime;
            }
            else if ( msgType == MessageType.PERF_OUTPUT_PROC_TIME ) {
                report.OutputProcTime = procTime;
                report.OutputProcStartTime = System.nanoTime() - procTime;
                report.ReceivedOutputProcCost = true;
            }
            else if ( (msgType == MessageType.PERF_INPUT_TRANS_TIME ) ) {
                if ( numDestsTransmittedTo > 0 ) {
                    report.InputTransTime = transTime;
                    report.InputTransTimeToFirstDest = transTimeToFirstDest;
                    report.InputTransStartTime = System.nanoTime() - transTime;
                }
            }
            else if ( (msgType == MessageType.PERF_OUTPUT_TRANS_TIME ) ) {
                report.ReceivedOutputTransCost = true;
                if ( numDestsTransmittedTo > 0 ) {
                    report.OutputTransTime = transTime;
                    report.OutputTransTimeToFirstDest = transTimeToFirstDest;
                    report.OutputTransStartTime = System.nanoTime() - transTime;
                }
            }
            else if ( msgType == MessageType.PERF_INPUT_RECEIVED_TIME ) {
                report.InputOriginalReceiveTime = System.nanoTime();
            }
            else if ( msgType == MessageType.PERF_OUTPUT_RECEIVED_TIME ) {
                report.OutputOriginalReceiveTime = System.nanoTime();
            }
            
            if ( report.ReceivedOutputProcCost && report.ReceivedOutputTransCost ) {
                final List<APerformanceOptimizationReport> reportsToSend = new LinkedList<APerformanceOptimizationReport>();
                reportsToSend.add( report );
                Runnable r = new Runnable() {
                    public void run() {
                        sendPerformanceOptimizationReports( reportsToSend );
                    }
                };
                ASharedThreadPool.getInstance().execute(
                        r,
                        0,
                        WindowsThreadPriority.BELOW_NORMAL
                        );
            }
            else if ( newReport ) {
                m_incompletePerformanceOptimizationReports.add( report );
            }
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APerfReportCollectorForwarder: Error in run",
                    e
                    );
        }        
    }
    
    private void sendPerformanceOptimizationReports(
            List<APerformanceOptimizationReport> reportsToSend
            ) {
        for ( int i = 0; i < reportsToSend.size(); i++ ) {
            PerformanceOptimizationReportMessage msg = new APerformanceOptimizationReportMessage(
                    reportsToSend.get( i )
                    );
            m_performanceReportForwarder.sendMsg( msg );
        }
    }
}
