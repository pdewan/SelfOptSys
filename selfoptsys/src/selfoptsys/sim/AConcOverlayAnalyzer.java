package selfoptsys.sim;

import selfoptsys.network.*;
import commonutils.basic.*;


public class AConcOverlayAnalyzer 
	extends AnOverlayAnalyzer {

	private int[] m_numChildrenProcessed;
	private double m_quantum;
	
	public AConcOverlayAnalyzer(
			NetworkInfo nwInfo,
			double quantum
			) {
		super( nwInfo );
		
		m_numChildrenProcessed = new int[ m_nwInfo.getNumComputers() ];
		m_quantum = quantum;
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
		
		for ( int i = 0; i < m_nwInfo.getNumComputers(); i++ ) {
			
			double delayToLocalUser = calculateDelayToLocalUser(
			        overlay,
			        i
			        );
			
			m_responseTimes[i] = 
				m_receptionTimes[i] +
				delayToLocalUser;
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
        double observedTransCostToFirstDest = m_nwInfo.getObservedTransCostToFirstDestForCompAndArch( curParent );

        delay = observedTransCost * ( m_numChildrenProcessed[ curParent ] + 1 ) +
            ( observedTransCostToFirstDest - observedTransCost );
        
        delay = MathUtils.roundToFourDecPlaces( delay );
        return delay;
        
	}
	
	private double calculateDelayToLocalUser(
            Overlay overlay,
			int curParent
			) {
		
        double delay = 0;
        
        double procCost = m_nwInfo.getProcCostForCompAndArch( curParent );
        boolean procAndTransThreadsShareCores = m_nwInfo.getProcAndTransThreadShareCores().get( curParent );
        int numChildren = overlay.getNumChildren()[ curParent ];
        
        if ( procAndTransThreadsShareCores == false ) {
            delay = procCost;
        }
        else {
            int numFullProcQuanta = (int) ( procCost / m_quantum );
            int numProcQuanta = numFullProcQuanta;
            long durationOfProcInLastQuantum = 0;
            if ( numFullProcQuanta * m_quantum < procCost ) {
                numProcQuanta++;
                durationOfProcInLastQuantum =
                    (long) ( ( procCost - numFullProcQuanta * m_quantum ) * (1000000) );
            }
            
            double cpuTransCost = m_nwInfo.getTransCostForCompAndArch( curParent );
            double cpuTransCostToFirstDest = m_nwInfo.getTransCostToFirstDestForCompAndArch( curParent );
            double totalCpuTransCost = numChildren == 0 ? 0 : numChildren * cpuTransCost + ( cpuTransCostToFirstDest - cpuTransCost );
            int numFullProcQuantaRequiredForTrans = (int) ( totalCpuTransCost / m_quantum ); 
            int numProcQuantaRequiredForTrans = numFullProcQuantaRequiredForTrans;
            long durationOfTransInLastQuantum = 0;
            if ( numFullProcQuantaRequiredForTrans * m_quantum < totalCpuTransCost ) {
                numProcQuantaRequiredForTrans++;
                durationOfTransInLastQuantum = 
                    (long) ( ( totalCpuTransCost - ( numFullProcQuantaRequiredForTrans * m_quantum ) ) * 1000000 );
            }
            
            long longDelay = SimulationUtils.getDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
                    numFullProcQuanta,
                    numFullProcQuantaRequiredForTrans,
                    numProcQuanta,
                    numProcQuantaRequiredForTrans,
                    durationOfProcInLastQuantum,
                    durationOfTransInLastQuantum,
                    (long) ( m_quantum * 1000000 )
                    );
            delay = MathUtils.round( longDelay / (double) 1000000, 4 );
        }
		
		delay = MathUtils.roundToFourDecPlaces( delay );
		return delay;
		
	}
	
}