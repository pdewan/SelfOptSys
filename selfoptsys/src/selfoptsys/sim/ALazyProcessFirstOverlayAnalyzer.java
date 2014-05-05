package selfoptsys.sim;

import selfoptsys.network.*;
import commonutils.basic.*;


public class ALazyProcessFirstOverlayAnalyzer 
	extends AnOverlayAnalyzer {

	private double[] m_availLpfDelays;
	private double[] m_delaySoFar;
	private int[] m_numChildrenProcessed;
	private double m_sourceLpfDelay;
	private double m_destLpfDelay;
	
	public ALazyProcessFirstOverlayAnalyzer(
			NetworkInfo nwInfo,
			double sourceLpfDelay,
			double destLpfDelay
			) {
		super(nwInfo);
		
		m_availLpfDelays = new double[ m_nwInfo.getNumComputers() ];
		m_numChildrenProcessed = new int[ m_nwInfo.getNumComputers() ];
		m_delaySoFar = new double[ m_nwInfo.getNumComputers() ];
		
		m_sourceLpfDelay = sourceLpfDelay;
		m_destLpfDelay = destLpfDelay;
	}
	
	public double[] analyzeOverlay(
			Overlay overlay
			) {
		
	    /*
	     * Setup the lazy processing first delays
	     */
		for ( int i = 0; i < m_nwInfo.getNumComputers(); i++ ) {
			if ( i == m_nwInfo.getSourceComputer() ) {
				m_availLpfDelays[i] = m_sourceLpfDelay;
			}
			else {
				m_availLpfDelays[i] = m_destLpfDelay;
			}
		}
		
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
			
			m_delaySoFar[ curComp ] = 
				parentDelay +
				m_delaySoFar[ curParent ];
			
			m_numChildrenProcessed[ curParent ]++;
			
			m_availLpfDelays[ curComp ] -= m_delaySoFar[ curComp ];
			m_availLpfDelays[ curComp ] = Math.max( 0, m_availLpfDelays[ curComp ] );
			m_availLpfDelays[ curComp ] = MathUtils.roundToFourDecPlaces( m_availLpfDelays[ curComp ] );
		}
		
		
        /*
         * Calculating response times from the reception times requires that we deduce
         * when the processing task can actually begin and then adding the processing 
         * time to that time. The time at which the processing can begin depends on how
         * many destinations a thread running on the same core as the processing thread
         * transmits to before either it uses up the lpf delay or runs out of dests to
         * transmit to.
         */
		for ( int i = 0; i < m_nwInfo.getNumComputers(); i++ ) {
            double processingDelayTime = 0;
            
            int numChildren = overlay.getNumChildren()[i];
            
            double transCost = m_nwInfo.getTransCostForCompAndArch( i );
            double transCostToFirstDest = m_nwInfo.getTransCostToFirstDestForCompAndArch( i );
            
            processingDelayTime = 0;
            if ( numChildren > 0 ) {
                double totalCpuTransTime = transCost * numChildren + ( transCostToFirstDest - transCost );
                if ( totalCpuTransTime > m_availLpfDelays[ i ] ) {
                    int numDestsTransmittedToBeforeProcessing = (int) (
                            ( m_availLpfDelays[ i ] - ( transCostToFirstDest - transCost ) ) / transCost
                            );
                    double timeLeftInDelay = 0;
                    if ( numDestsTransmittedToBeforeProcessing > 0 ) {
                        timeLeftInDelay = 
                            m_availLpfDelays[ i ] - 
                            ( transCost * numDestsTransmittedToBeforeProcessing + ( transCostToFirstDest - transCost ) );
                    }
                    totalCpuTransTime = m_availLpfDelays[ i ] - timeLeftInDelay;
                }
                processingDelayTime = totalCpuTransTime;
            }
            
            m_responseTimes[i] = 
                m_receptionTimes[i] + 
                processingDelayTime + 
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
        totalObservedTransTime += observedTransCostToFirstDest - observedTransCost;
        
        delay = totalObservedTransTime;

        double transCpuCost = m_nwInfo.getTransCostForCompAndArch( curParent );
        double totalCpuTransTime = transCpuCost * ( m_numChildrenProcessed[ curParent ] + 1 );
        
        double transCpuCostToFirstDest = m_nwInfo.getTransCostToFirstDestForCompAndArch( curParent );
        totalCpuTransTime += transCpuCostToFirstDest - transCpuCost;
        
        if ( totalCpuTransTime > m_availLpfDelays[ curParent ] ) {
            
            double procCost = m_nwInfo.getProcCostForCompAndArch( curParent );
            int numDestsTransmittedToBeforeProcessing = (int) (
                    ( m_availLpfDelays[ curParent ] - ( transCpuCostToFirstDest - transCpuCost ) ) / transCpuCost
                    );
            double timeForNwCardToTransmit = 
                numDestsTransmittedToBeforeProcessing * observedTransCost +
                ( observedTransCostToFirstDest - observedTransCost );
                
            if ( numDestsTransmittedToBeforeProcessing > 0 && timeForNwCardToTransmit < m_availLpfDelays[ curParent ] + procCost ) {
                delay = ( numDestsTransmittedToBeforeProcessing * transCpuCost ) + ( transCpuCostToFirstDest - transCpuCost ) +
                    procCost +
                    observedTransCost * ( m_numChildrenProcessed[ curParent ] + 1 - numDestsTransmittedToBeforeProcessing );
            }
        }
		
		delay = MathUtils.roundToFourDecPlaces( delay );
		return delay;
		
	}
	
}
