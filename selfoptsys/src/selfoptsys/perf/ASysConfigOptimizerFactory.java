package selfoptsys.perf;

import java.util.List;


public class ASysConfigOptimizerFactory 
    implements SysConfigOptimizerFactory {

    public SysConfigOptimizer getSysConfigOptimizer(
            double schedulingQuantum,
            double lazyDelay,
            boolean supportProcessingArchitectureChanges,
            boolean supportReplicatedArchitectures,
            boolean supportAllCentralizedArchitectures,
            List<Integer> supportedCentralizedArchitectureMasters,
            boolean supportMulticastCommunication,
            boolean supportSchedulingChanges
            ) {
        return new ASysConfigOptimizer(
                schedulingQuantum,
                lazyDelay,
                supportProcessingArchitectureChanges,
                supportReplicatedArchitectures,
                supportAllCentralizedArchitectures,
                supportedCentralizedArchitectureMasters,
                supportMulticastCommunication,
                supportSchedulingChanges
                );
    }
    
}
