package selfoptsys.perf.tradeofffunction;

import java.util.*;
import commonutils.config.*;

public class AThesisChapter3Exp2RespTimeTradeoffPolicy 
	implements RespTimeTradeoffPolicy {

	public int compareResponseTimes(
            int sourceUserIndex,
            int firstMasterUserIndex,
            int secondMasterUserIndex,
            double minRequiredDiffInLocalResponseTimes,
            double minRequiredDiffInRemoteResponseTimes,
			double[] firstRemoteResponseTimes,
			double[] secondRemoteResponseTimes,
            SchedulingPolicy firstSchedulingPolicy,
            SchedulingPolicy secondSchedulingPolicy
			) {
	    
	    double[] firstRemoteResponseTimesWeCareAbout = getResponseTimesWeCareAbout(
                sourceUserIndex,
                firstMasterUserIndex,
                firstRemoteResponseTimes
                );
        double[] secondRemoteResponseTimesWeCareAbout = getResponseTimesWeCareAbout(
                sourceUserIndex,
                secondMasterUserIndex,
                secondRemoteResponseTimes
                );
        
	    double[] tempFirstRemoteResponseTimes = new double[ firstRemoteResponseTimesWeCareAbout.length ];
	    double[] tempSecondRemoteResponseTimes = new double[ secondRemoteResponseTimesWeCareAbout.length ];
	    for ( int i = 0; i < tempFirstRemoteResponseTimes.length; i++ ) {
	        tempFirstRemoteResponseTimes[ i ] = firstRemoteResponseTimesWeCareAbout[ i ];
	        tempSecondRemoteResponseTimes[ i ] = secondRemoteResponseTimesWeCareAbout[ i ];
	    }
	    
		Arrays.sort( tempFirstRemoteResponseTimes );
		Arrays.sort( tempSecondRemoteResponseTimes );
		
		int numResponseTimesImprovedBySecond = 0;
		
		for ( int i = 0; i < tempFirstRemoteResponseTimes.length; i++ ) {
			double diff = tempFirstRemoteResponseTimes[i] - tempSecondRemoteResponseTimes[ i ];
			if ( diff < 0 ) {
				numResponseTimesImprovedBySecond--;
			}
			else if ( diff > 0 ) {
				numResponseTimesImprovedBySecond++;
			}
		}
		
		return numResponseTimesImprovedBySecond;
	}
	
    protected double[] getResponseTimesWeCareAbout(
            int sourceUserIndex,
            int firstMasterUserIndex,
            double[] remoteResponseTimes
            ) {
        
        double[] newRespTimes = new double[1];
        newRespTimes[0] = remoteResponseTimes[1];
        return newRespTimes;
        
    }
    	
}
