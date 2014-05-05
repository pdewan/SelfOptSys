package selfoptsys.network.overlay;

import selfoptsys.network.*;

public class AUnicastOverlayBuilder extends 
	AnOverlayBuilder {

    public AUnicastOverlayBuilder(
    		NetworkInfo nwInfo
    		) {
    	
    	super( nwInfo );
    }
    
    public Overlay generateOverlay() {

        findOverlay();
        
        return getOverlayBuilderResult();
        
    }
    
    private void findOverlay() {
        
    	int numComputers = m_nwInfo.getNumComputers();
    	int sourceComputer = m_nwInfo.getSourceComputer();
        int numUsedVertices = 0;
        
        for ( int i = 0; i < numComputers; i++ ) {
        	m_parents[ i ] = sourceComputer;
        }
        m_parents[ sourceComputer ] = -1;
        
        m_addOrder[numUsedVertices] = sourceComputer;
        numUsedVertices++;
        for ( int i = 0; i < numComputers; i++ ) {
        	if ( i == sourceComputer ) {
        		continue;
        	}
        	m_addOrder[numUsedVertices] = i;
        	numUsedVertices++;
        }
        
        for ( int i = 0; i < numComputers; i++ ) {
        	m_numChildren[ i ] = 0;
        }
        m_numChildren[ sourceComputer ] = numComputers - 1;
        
    }
    
}
