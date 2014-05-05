package selfoptsys.network.overlay;

import selfoptsys.network.*;
import selfoptsys.network.misc.*;
import commonutils.config.*;

public class AnHmdmMulticastOverlayBuilder extends 
	AnOverlayBuilder {

    private double[] m_pvVals = null;
    private double[][] m_updatedEdgeCosts = null;
    
    private int[][] m_shortestPathPredecessors = null;
    private double[][] m_shortestPathLengths = null;

    public AnHmdmMulticastOverlayBuilder(
    		NetworkInfo nwInfo
    		) {
    	
    	super( nwInfo );
    }
    
    public Overlay generateOverlay() {

        getPVals();
        
        updateCommCosts();
        
        computeShortestPathBetweenAllVertices();
        
        findOverlay();
        
        m_addOrder = ATransmissionOrderOptimizer.findOptimalOrdering(
        		m_nwInfo,
        		m_children
        		);
        
        return getOverlayBuilderResult();
        
    }
    
    private int getNumUsedVerticesOnPath( 
    		int start, 
    		int end, 
    		boolean[] usedVertices 
    		) {
    	
        int numUsed = 0;
        if ( usedVertices[start] == true ) {
            numUsed = 1;
        }
        
        while ( end != start ) {
            if ( usedVertices[end] == true ) {
                numUsed++;
            }
            
            end = m_shortestPathPredecessors[start][end];
        }
        
        return numUsed;
        
    }
    
    //
    // This algorithm is the same as for HMDM. However, the way that the
    // edge costs are updated is different here. In particular, NOT ONLY
    // is the transmission cost (of the source host) added to the cost 
    // of each edge, BUT ALSO the processing cost (of the source host).
    //
    private void findOverlay() {
        
    	int numComputers = m_nwInfo.getNumComputers();
    	int sourceComputer = m_nwInfo.getSourceComputer();
        int numUsedVertices = 0;

        boolean[] usedVertices = new boolean[ numComputers ];
        for ( int i = 0; i < numComputers; i++ ) {
            usedVertices[i] = false;
        }
        
        m_parents[ sourceComputer ] = -1;
        m_addOrder[numUsedVertices] = sourceComputer;
        m_feedthruTimes[ sourceComputer ] = 0;
        usedVertices[ sourceComputer ] = true;
        numUsedVertices++;
        
        while ( numUsedVertices < numComputers ) {
            int[] m = new int[ numComputers ];
            double[] mCosts = new double[ numComputers ];
            for ( int i = 0; i < numComputers; i++ ) {
                m[i] = Integer.MAX_VALUE;
                mCosts[i] = Double.MAX_VALUE;
            }
            
            for ( int u = 0; u < numComputers; u++ ) {
                if ( !usedVertices[u] ) {
                    for ( int v = 0; v < numComputers; v++ ) {
                        if ( usedVertices[v] ) {
                            
                            int numUsedOnPath = getNumUsedVerticesOnPath( v, u, usedVertices );
                            if ( numUsedOnPath > 1 ) {
                                continue;
                            }
                            
                            double newVal = m_shortestPathLengths[v][u];;
                            if ( m_feedthruTimes[v] != Double.MAX_VALUE ) {
                                newVal += m_feedthruTimes[v];
                            }
                            if ( mCosts[u] > newVal ) {
                                mCosts[u] = newVal;
                                m[u] = v;
                            }
                        }
                    }
                }
            }
            
            int v = -1;
            double curMax = 0;
            for ( int u = 0; u < numComputers; u++ ) {
                if ( m[u] == Integer.MAX_VALUE ) {
                    continue;
                }
                if ( !usedVertices[u] ) {
                    double newMax = m_feedthruTimes[m[u]] + m_shortestPathLengths[m[u]][u];
                    /*
                     * NOTE: We **have** to find some newMax greater than 0. The reason is
                     * that m_shortestPathLengths is always greater than 0 since transmission
                     * cost for each computer is greater than 0.
                     */
                    if ( curMax < newMax ) {
                        curMax = newMax;
                        v = u;
                    }
                }
            }
            
            int w = v;
            int numNewVertices = 0;
            while ( !usedVertices[w] ) {
                numNewVertices++;
                w = m_shortestPathPredecessors[m[v]][w];
            }
            int addOrderPos = numUsedVertices + numNewVertices - 1;
            m[v] = w;
            w = v;
            while ( w != m[v] ) {
                boolean exitLoop = false;
                int predecessor = m_shortestPathPredecessors[m[v]][w];

                m_parents[w] = predecessor;
                addComputerAsChildOf( m_parents[w], w );
                m_numChildren[predecessor]++;
                m_addOrder[addOrderPos] = w;
                addOrderPos--;
                
                m_feedthruTimes[w] = m_feedthruTimes[m[v]] + m_pvVals[w] + m_shortestPathLengths[m[v]][w];
                usedVertices[w] = true;
                numUsedVertices++;
                
                w = predecessor;
                if ( exitLoop ) {
                    break;
                }
            }
            
            m_feedthruTimes[w] = m_feedthruTimes[w] + m_pvVals[w];
            m_feedthruTimes[v] = m_feedthruTimes[v] - m_pvVals[v];
            usedVertices[v] = true;
            
        }
    }
    
    private void getPVals() {
        if ( m_nwInfo.getProcessingArchitecture() == ProcessingArchitectureType.CENTRALIZED ) {
            m_pvVals = m_nwInfo.getObservedOutputTransCosts();
        } else if ( m_nwInfo.getProcessingArchitecture() == ProcessingArchitectureType.REPLICATED ) {
            m_pvVals = m_nwInfo.getObservedInputTransCosts();
        }
    }
    
    private void updateCommCosts() {
    	
    	int numComputers = m_nwInfo.getNumComputers();
    	
        m_updatedEdgeCosts = new double[ numComputers ][ numComputers ];
        double[][] commCosts = m_nwInfo.getNetworkLatencies();
        
        for ( int i = 0; i < numComputers; i++ ) {
            for ( int j = 0; j < numComputers; j++ ) {
                if ( i == j ) {
                    continue;
                }
                
                m_updatedEdgeCosts[i][j] = commCosts[i][j] + m_pvVals[i];
            }
        }
    }
    
    private void computeShortestPathBetweenAllVertices() {
        
    	int numComputers = m_nwInfo.getNumComputers();
    	int sourceComputer = m_nwInfo.getSourceComputer();
    	
        boolean[] vertexHasSlack = new boolean[ numComputers ];
        vertexHasSlack[ sourceComputer ] = true;
        
        FloydWarshallShortestPathBetweenAllPairs floydWarshall = new FloydWarshallShortestPathBetweenAllPairs();
        floydWarshall.setEdgeCosts( m_updatedEdgeCosts );
        floydWarshall.findShortestPaths();
        
        m_shortestPathLengths = floydWarshall.getShortestPathLengths();
        m_shortestPathPredecessors = floydWarshall.getShortestPathPredecessors();

    }
}
