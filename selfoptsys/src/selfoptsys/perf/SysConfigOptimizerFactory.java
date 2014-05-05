package selfoptsys.perf;

import java.util.*;


public interface SysConfigOptimizerFactory {

    SysConfigOptimizer getSysConfigOptimizer(
            double schedulingQuantum,
            double lazyDelay,
            boolean supportProcessingArchitectureChanges,
            boolean supportReplicatedArchitectures,
            boolean supportAllCentralizedArchitectures,
            List<Integer> supportedCentralizedArchitectureMasters,
            boolean supportMulticastCommunication,
            boolean supportSchedulingChanges
            );
    
}
