package selfoptsys.perf;

import selfoptsys.comm.*;

public interface PerformanceOptimizationClient {

    void teardown();
    
    void registerWithPerformanceOptimizationServer(
    		PerformanceOptimizationServer performanceServer
    		);
    void unregisterFromPerformanceOptimizationServer();
    
    void sendPerformanceReport(
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
            );
    
}
