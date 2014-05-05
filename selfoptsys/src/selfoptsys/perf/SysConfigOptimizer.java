package selfoptsys.perf;

import java.util.*;


public interface SysConfigOptimizer {

    SysConfig optimizeCurrentSysConfig(
            SysConfig curSysConfig,
            Map<Integer, PerfCollectorUserInfo> userInfos,
            Map<ComputerInfo, ComputerStats> computerStatsCollection,
            Map<Integer,Map<Integer,Double>> latencies,
            double thinkTimeEstimate,
            double significantDiffInLocalRespTimes,
            double significantDiffInRemoteRespTimes
            );
    
}
