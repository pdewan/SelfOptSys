package selfoptsys.perf.tradeofffunction;

import commonutils.config.SchedulingPolicy;

public interface RespTimeTradeoffPolicy {
	
	int compareResponseTimes(
	        int sourceUserIndex,
            int firstMasterUserIndex,
            int secondMasterUserIndex,
            double minRequiredDiffInLocalResponseTimes,
            double minRequiredDiffInRemoteResponseTimes,
			double[] firstRemoteResponseTimes,
			double[] secondRemoteResponseTimes,
			SchedulingPolicy firstSchedulingPolicy,
			SchedulingPolicy secondSchedulingPolicy
			);
	
}
