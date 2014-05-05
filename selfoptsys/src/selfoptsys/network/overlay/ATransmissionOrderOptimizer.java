package selfoptsys.network.overlay;

import java.util.*;

import selfoptsys.network.*;

import commonutils.basic.*;



/*
 * This is my implementation of the algorithm for finding the optimal 
 * ordering within a tree as presented in Eli Brosh's Master's Thesis 
 * in "Section 3.2 - The Optimal Recursive Computation."
 */
public class ATransmissionOrderOptimizer {

    static double[] m_mVals;
    static double[] m_MVals;
    
    public static int[] findOptimalOrdering(
    		NetworkInfo nwInfo,
    		Map<Integer, List<Integer>> childrenLists
    		) {
    	int numComputers = nwInfo.getNumComputers();
    	int sourceComputer = nwInfo.getSourceComputer();
    	
    	int[] addOrder = new int[ numComputers ];
    	
        m_mVals = new double[ numComputers ];
        m_MVals = new double[ numComputers ];
        
        /*
         * First calculate M vals which dictate child ordering
         */
        double[] possibleMVals = getCandidateMVals( 
        		nwInfo,
        		childrenLists,
        		sourceComputer
        		);
        Arrays.sort( possibleMVals );
        m_MVals[ sourceComputer ] = possibleMVals[possibleMVals.length - 1];
        
        /*
         * Rearrange the m_addOrder to have optimal ordering
         * The original tree must be preserved.
         */
        int numComputersUsed = 0;
        List<Integer> children; 
        Vector<Integer> reachableComputers = new Vector<Integer>();
        
        reachableComputers.add( sourceComputer );
        
        while ( reachableComputers.isEmpty() == false ) {
            int curComputer = reachableComputers.elementAt(0);
            reachableComputers.removeElementAt(0);
            
            addOrder[ numComputersUsed ] = curComputer;
            children = childrenLists.get( curComputer );
            int[] sortedChildren = sortChildrenAccordingToMVal( children );
            
            for ( int i = 0; i < children.size(); i++ ) {
                reachableComputers.add( sortedChildren[i] );
            }
            
            numComputersUsed++;
        }
        
        return addOrder;
    }

    static int[] sortChildrenAccordingToMVal( 
    		List<Integer> children 
    		) {
    	
        int[] sortedChildren = new int[ children.size() ];
        double[] childmVals = new double[ children.size() ];
        
        for ( int i = 0; i < children.size(); i++ ) {
            sortedChildren[i] = children.get(i);
            childmVals[i] = m_mVals[children.get(i)];
        }
        
        DualArraySorter.sortOnDoubleArray( childmVals, sortedChildren );

        return sortedChildren;
    }
    
    static double[] getCandidateMVals( 
    		NetworkInfo nwInfo,
    		Map<Integer, List<Integer>> childrenLists,
    		int subtreeRoot 
    		) {
        
        List<Integer> children = childrenLists.get( subtreeRoot );
    	int numChildren = children.size();

        double[] vals = new double[ numChildren ];
        for ( int i = 0; i < numChildren; i++ ) {
            int curChild = children.get(i);
            List<Integer> childsChildren = childrenLists.get( curChild );
            
            /*
             * Recursively calculate m and M vals
             */
            if ( childsChildren.size() > 0 ) {
                double[] possiblemVals = getCandidateMVals(
                		nwInfo,
                		childrenLists,
                		curChild 
                		);
                Arrays.sort(possiblemVals);
                m_MVals[curChild] = possiblemVals[possiblemVals.length - 1];
                m_mVals[curChild] = m_MVals[curChild] + nwInfo.getNetworkLatencies()[ subtreeRoot ][ curChild ];
            }
            else {
                m_mVals[curChild] = m_MVals[curChild] + nwInfo.getNetworkLatencies()[ subtreeRoot ][ curChild ];
            }
            
            vals[i] = m_mVals[curChild];
        }
        
        Arrays.sort( vals );
        
        for ( int i = 0; i < numChildren; i++ ) {
        	vals[i] += ( (numChildren - i - 1) + 1) * nwInfo.getTransCostForCompAndArch( subtreeRoot );
        }
        
        return vals;
    }

	
}
