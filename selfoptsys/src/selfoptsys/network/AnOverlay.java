package selfoptsys.network;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnOverlay 
	implements Overlay {

	private static final long serialVersionUID = -2255012837266946536L;
	int[] m_parents = null;
    int[] m_addOrder = null;
    int[] m_numChildren = null;
    Map<Integer, List<Integer>> m_childrenOf;
    
    public AnOverlay(
    		int[] parents,
    		int[] addOrder,
    		int[] numChildren,
    		Map<Integer, List<Integer>> childrenOf
    		) {
    	
    	m_parents = parents;
    	m_addOrder = addOrder;
    	m_numChildren = numChildren;
    	m_childrenOf = childrenOf;
    	
    }
    
    public int[] getAddOrder() {
        return m_addOrder;
    }
    public int[] getParents() {
        return m_parents;
    }
    public int[] getNumChildren() {
        return m_numChildren;
    }
    
    public List<Integer> getChildrenOf( int index ) {
        return m_childrenOf.get( index );
    }
    
    public String toString() {
    	String output = "";
    	
    	output += "Add Order: ";
    	for ( int i = 0; i < m_addOrder.length; i++ ) {
    		output += m_addOrder[i] + " ";
    	}
    	output += "\n";
    	
    	output += "Num Children: ";
    	for ( int i = 0; i < m_numChildren.length; i++ ) {
    		output += m_numChildren[i] + " ";
    	}
    	output += "\n";

    	output += "Parents: ";
    	for ( int i = 0; i < m_parents.length; i++ ) {
    		output += m_parents[i] + " ";
    	}
    	output += "\n";
    	
        output += "Children Of: ";
        Iterator<Map.Entry<Integer, List<Integer>>> itr = m_childrenOf.entrySet().iterator();
    	while ( itr.hasNext() ) {
    	    Map.Entry<Integer, List<Integer>> entry = itr.next();
    	    int parentIndex = entry.getKey();
    	    List<Integer> children = entry.getValue();
    	    if ( children.size() == 0 ) {
    	        continue;
    	    }
    	    
	        output += "p=" + parentIndex + ",c={";
	        for ( int i = 0; i < children.size(); i++ ) {
	            output += m_childrenOf.get( i ) + " ";
	        }
	        output += "};";
    	}
        output += "\n";

    	return output;
    }
    
    public boolean equals( Object other ) {
        
        if ( other instanceof Overlay == false ) {
            return false;
        }
        Overlay otherOverlay = (Overlay) other;
        
        if ( m_addOrder.length != otherOverlay.getAddOrder().length ) {
            return false;
        }
        
        for ( int i = 0; i < m_parents.length; i++ ) {
            if ( m_parents[ i ] != otherOverlay.getParents()[ i ] ) {
                return false;
            }
        }
        
        return true;
    }
    
}
