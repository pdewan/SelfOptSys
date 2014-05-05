package selfoptsys.perf;

import java.net.InetAddress;

import selfoptsys.*;

public interface PerformanceOptimizationUserInfo {

    int getUserIndex();
    Logger getLogger();
    boolean getHasAFakeLoggable();
    boolean getAutoReportProcCosts();
    boolean getAutoReportTransCosts();
    int getSimulatedLatencyIndex();
    boolean getCollectPerformanceData();
    int getCoreUsedByProcessingThread();
    int getCoreUsedByTransmissionThread();
    
    void setHost(
            InetAddress host
            );
    InetAddress getHost();
    
    void setNetworkLatencyCollectorClient(
            NetworkLatencyCollectorClient networkLatencyCollectorClient
            );
    NetworkLatencyCollectorClient getNetworkLatencyCollectorClient();
    
    void setComputerInfo(
            ComputerInfo computerInfo
            );
    ComputerInfo getComputerInfo();
    
}
