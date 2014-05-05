package selfoptsys.perf;

import java.io.Serializable;
import java.net.InetAddress;

import selfoptsys.*;

public class APerformanceOptimizationUserInfo 
    implements PerformanceOptimizationUserInfo, Serializable {
    
    private static final long serialVersionUID = 9149413685599931170L;
    
    protected int m_userIndex;
    protected Logger m_logger;
    protected InetAddress m_host;
    protected NetworkLatencyCollectorClient m_networkLatencyCollectorClient;
    protected boolean m_hasAFakeLoggable;
    protected boolean m_autoReportProcCosts;
    protected boolean m_autoReportTransCosts;
    protected int m_simulatedLatencyIndex;
    protected boolean m_collectPerformanceStats;
    protected int m_coreUsedByProcessingThread;
    protected int m_coreUsedByTransmissionThread;
    
    protected ComputerInfo m_computerInfo;
    
    public APerformanceOptimizationUserInfo(
            int userIndex,
            Logger logger,
            boolean hasAFakeLoggable,
            boolean autoReportProcCosts,
            boolean autoReportTransCosts,
            int simulatedLatencyIndex,
            boolean collectPerformanceStats,
            int coreUsedByProcessingThread,
            int coreUsedByTransmissionThread
            ) {
        m_userIndex = userIndex;
        m_logger = logger;
        m_hasAFakeLoggable = hasAFakeLoggable;
        m_autoReportProcCosts = autoReportProcCosts;
        m_autoReportTransCosts = autoReportTransCosts;
        m_simulatedLatencyIndex = simulatedLatencyIndex;
        m_collectPerformanceStats = collectPerformanceStats;
        m_coreUsedByProcessingThread = coreUsedByProcessingThread;
        m_coreUsedByTransmissionThread = coreUsedByTransmissionThread;
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    public Logger getLogger() {
        return m_logger;
    }
    public boolean getHasAFakeLoggable() {
        return m_hasAFakeLoggable;
    }
    public boolean getAutoReportProcCosts() {
        return m_autoReportProcCosts;
    }
    public boolean getAutoReportTransCosts() {
        return m_autoReportTransCosts;
    }
    public int getSimulatedLatencyIndex() {
        return m_simulatedLatencyIndex;
    }
    public boolean getCollectPerformanceData() {
        return m_collectPerformanceStats;
    }
    public int getCoreUsedByProcessingThread() {
        return m_coreUsedByProcessingThread;
    }
    public int getCoreUsedByTransmissionThread() {
        return m_coreUsedByTransmissionThread;
    }
    
    public void setComputerInfo(
            ComputerInfo computerInfo
            ) {
        m_computerInfo = computerInfo;
    }
    public ComputerInfo getComputerInfo() {
        return m_computerInfo;
    }

    public void setHost(
            InetAddress host
            ) {
        m_host = host;
    }
    public InetAddress getHost() {
        return m_host;
    }
    
    public void setNetworkLatencyCollectorClient(
            NetworkLatencyCollectorClient networkLatencyCollectorClient
            ) {
        m_networkLatencyCollectorClient = networkLatencyCollectorClient;
    }
    public NetworkLatencyCollectorClient getNetworkLatencyCollectorClient() {
        return m_networkLatencyCollectorClient;
    }
}
