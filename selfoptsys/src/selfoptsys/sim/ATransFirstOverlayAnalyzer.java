package selfoptsys.sim;

import selfoptsys.network.*;
import commonutils.basic.*;


public class ATransFirstOverlayAnalyzer 
	extends AnOverlayAnalyzer {

	private int[] m_numChildrenProcessed;
	
	public ATransFirstOverlayAnalyzer(
			NetworkInfo nwInfo
			) {
		super(nwInfo);
		
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
         * Calculating response times from the reception times requires that we deduce
         * when the processing task can actually begin and then adding the processing 
         * time to that time. The time at which the processing can begin depends on how
         * many destinations a thread running on the same core as the processing thread
         * transmits to.
         */
		for ( int i = 0; i < m_nwInfo.getNumComputers(); i++ ) {
		    
			double processingStartTime = 0;
			
            int numChildren = overlay.getNumChildren()[i];
		    
	        double transCost = m_nwInfo.getTransCostForCompAndArch( i );
	        processingStartTime = transCost * numChildren;

	        double transCostToFirstDest = m_nwInfo.getTransCostToFirstDestForCompAndArch( i );
	        processingStartTime += ( transCostToFirstDest - transCost );
			
			m_responseTimes[i] = 
				m_receptionTimes[i] +
				processingStartTime + 
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
		double totalTransTime = observedTransCost * (m_numChildrenProcessed[ curParent ] + 1);

        double observedTransCostToFirstDest = m_nwInfo.getObservedTransCostToFirstDestForCompAndArch( curParent );
        totalTransTime += ( observedTransCostToFirstDest - observedTransCost );

		delay = totalTransTime;
		
        delay = MathUtils.roundToFourDecPlaces( delay );
		return delay;
		
	}
	
}