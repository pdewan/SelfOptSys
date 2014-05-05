package selfoptsys.sim;

import commonutils.config.*;
import selfoptsys.network.*;

public abstract class AnOverlayAnalyzer
	implements OverlayAnalyzer {

	protected final double ZERO = 0.000001;
	
	protected NetworkInfo m_nwInfo;
	
	protected double[] m_responseTimes;
	protected double[] m_receptionTimes;
	
	public AnOverlayAnalyzer(
			NetworkInfo nwInfo
			) {
		m_nwInfo = nwInfo;
		m_responseTimes = new double[ m_nwInfo.getNumComputers() ];
		m_receptionTimes = new double[ m_nwInfo.getNumComputers() ];
	}
	
	public abstract double[] analyzeOverlay(
			Overlay overlay
			);
	
	protected void calculateReceptionTimeForSourceUser(
	        Overlay overlay
	        ) {

        /*
         * If the centralized processing architecture is used, then we "offset" the received time
         * calculations by the amount of time the master computer requires to process the input
         * command. The reason is that the master computer must first compute the output (by
         * processing the input) before it can even begin transmitting or processing the output.
         * Later, when we calculate response times, this also makes sure that the input
         * processing time on the master is included in the calculation.
         */
	    
	    if ( m_nwInfo.getProcessingArchitecture() == ProcessingArchitectureType.CENTRALIZED ) {
	        int masterUserIndex = m_nwInfo.getSourceComputer();
	        m_receptionTimes[ masterUserIndex ] = m_nwInfo.getInputProcCostForComp( masterUserIndex );
	    }
	    
	}

}
