package selfoptsys.network.misc;

public class FloydWarshallShortestPathBetweenAllPairs {

    private double[][] m_edgeCosts = null;
    
    private int m_numVertices = -1;
    private double[][] m_shortestPathLengths = null;
    private int[][] m_shortestPathPredecessors = null;
    
    public void setEdgeCosts( 
    		double[][] edgeCosts
    		) {
        m_numVertices = edgeCosts.length;

        m_edgeCosts = new double[m_numVertices][m_numVertices];
        for ( int i = 0; i < m_numVertices; i++ ) {
            for ( int j = 0; j < m_numVertices; j++ ) {
                m_edgeCosts[i][j] = edgeCosts[i][j];
            }
        }
        
    }
    
    public void findShortestPaths() {
        
        m_shortestPathLengths = new double[m_numVertices][m_numVertices];
        m_shortestPathPredecessors = new int[m_numVertices][m_numVertices];
        
        for ( int i = 0; i < m_numVertices; i++ ) {
            for ( int j = 0; j < m_numVertices; j++ ) {
                if ( m_edgeCosts[i][j] != Double.MAX_VALUE ) {
                    m_shortestPathLengths[i][j] = m_edgeCosts[i][j];
                    m_shortestPathPredecessors[i][j] = i;
                }
                else {
                    m_shortestPathLengths[i][j] = Double.MAX_VALUE;
                    m_shortestPathPredecessors[i][j] = -1;
                }
                
                if ( i == j ) {
                    m_shortestPathLengths[i][j] = 0;
                    m_shortestPathPredecessors[i][j] = -1;
                }
            }
        }

        double curPathLength = 0;
        for ( int k = 0; k < m_numVertices; k++ ) {
            for ( int i = 0; i < m_numVertices; i++ ) {

                for ( int j = 0; j < m_numVertices; j++ ) {
                    curPathLength = m_shortestPathLengths[i][k] + m_shortestPathLengths[k][j];
                    if ( m_shortestPathLengths[i][j] > curPathLength ) {                        
                        m_shortestPathLengths[i][j] = curPathLength;
                        m_shortestPathPredecessors[i][j] = m_shortestPathPredecessors[k][j];
                    }
                }
            }
        }
        
    }
    
    public double[][] getShortestPathLengths() {
        return m_shortestPathLengths;
    }
    
    public int[][] getShortestPathPredecessors() {
        return m_shortestPathPredecessors;
    }
    
}
