package selfoptsys.sim;

import selfoptsys.network.*;
import commonutils.basic.*;


public class AProcFirstOverlayAnalyzer 
	extends AnOverlayAnalyzer {

	private int[] m_numChildrenProcessed;
	
	public AProcFirstOverlayAnalyzer(
			NetworkInfo nwInfo
			) {
		super( nwInfo );
		
		m_numChildrenProcessed = new int[ m_nwInfo.getNumComputers() ];
	}
	
	public double[] analyzeOverlay(
			Overlay overlay
			) {
		
	    /*
	     * First, we calculate the time at which each computer receives the input (output) command
	     * if the replicated (centralized) processing architecture is used.
	     */
	    calculateReceptionTimeForSourceUser(
	            overlay
	            );
		for ( int i = 0; i < m_nwInfo.getNumComputers(); i++ ) {
			int curComp = overlay.getAddOrder()[ i ];
			
			if ( curComp == m_nwInfo.getSourceComputer() ) {
				continue;
			}
			
			int curParent = overlay.getParents()[ curComp ];
			double parentDelay = calculateDelayOnParentForComp(
					overlay,
					curParent
					);
			
			m_receptionTimes[ curComp ] = 
				m_receptionTimes[ curParent ] + 
				parentDelay +
				m_nwInfo.getNetworkLatencies()[ curParent ][ curComp ];
			m_receptionTimes[ curComp ] = MathUtils.roundToFourDecPlaces( m_receptionTimes[ curComp ] );
			
			m_numChildrenProcessed[ curParent ]++;
		}
		
		/*
		 * Calculating response times from the reception times is straightforward
		 * when process first scheduling is used. It is equal to the reception time
		 * plus the processing time.
		 */
		for ( int i = 0; i < m_nwInfo.getNumComputers(); i++ ) {
			m_responseTimes[i] = 
				m_receptionTimes[i] +
				m_nwInfo.getProcCostForCompAndArch( i );
			m_responseTimes[i] = MathUtils.roundToFourDecPlaces( m_responseTimes[i] );
		}
		
		return m_responseTimes;
		
	}
	
	private double calculateDelayOnParentForComp(
			Overlay overlay,
			int curParent
			) {
		
	    double delay = 0;
		
        double observedTransCost = m_nwInfo.getObservedTransCostForCompAndArch( curParent );
		double totalObservedTransTime = observedTransCost * ( m_numChildrenProcessed[ curParent ] + 1 );

		double observedTransCostToFirstDest = m_nwInfo.getObservedTransCostToFirstDestForCompAndArch( curParent );
		totalObservedTransTime += ( observedTransCostToFirstDest - observedTransCost );
		
		delay = totalObservedTransTime;
		
	    double procTime = m_nwInfo.getProcCostForCompAndArch( curParent );
	    delay = procTime + totalObservedTransTime;
		
		delay = MathUtils.roundToFourDecPlaces( delay );
		return delay;
		
	}
	
}