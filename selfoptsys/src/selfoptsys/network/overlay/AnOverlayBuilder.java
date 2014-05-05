package selfoptsys.network.overlay;

import java.util.*;

import selfoptsys.network.*;


public abstract class AnOverlayBuilder 
	implements OverlayBuilder {

    protected NetworkInfo m_nwInfo;

    protected int[] m_parents;
    protected int[] m_addOrder;
    protected int[] m_numChildren;
    protected Map<Integer, List<Integer>> m_children;
    
    protected double[] m_feedthruTimes;
    
    public AnOverlayBuilder(
    		NetworkInfo nwInfo
    		) {
    	
    	m_nwInfo = nwInfo;
    	m_children = new Hashtable<Integer, List<Integer>>();
    	
    	reset();
    	
    }
    
    public abstract Overlay generateOverlay();
    
    private void reset() {
    	
        m_addOrder = null;
        m_parents = null;
        m_numChildren = null;
        m_children = null;
        m_feedthruTimes = null;
        
        if ( m_nwInfo == null ) {
        	return;
        }
        
        int numComputers = m_nwInfo.getNumComputers();
        m_addOrder = new int[ numComputers ];
        m_parents = new int[ numComputers ];
        m_numChildren = new int[ numComputers ];
        m_feedthruTimes = new double[ numComputers ];

        for ( int i = 0; i < numComputers; i++ ) {
        	m_feedthruTimes[i] = Double.MAX_VALUE;
        }
        
    }
    
    protected void addComputerAsChildOf(
    		int parentIndex,
    		int computerIndex
    		) {
    	
        List<Integer> children = m_children.get( parentIndex );
        if ( children == null ) { 
            children = new LinkedList<Integer>();
            m_children.put(
                    parentIndex,
                    children
                    );
        }
    	children.add( computerIndex );
    	
    }
    
    protected List<Integer> getChildrentList(
    		int computerIndex
    		) {
    	return m_children.get( computerIndex );
    }
        
    public Overlay getOverlayBuilderResult() {

    	Overlay overlay = new AnOverlay(
    			m_parents,
    			m_addOrder,
    			m_numChildren,
    			m_children
    			);
        return overlay;
    }

}
