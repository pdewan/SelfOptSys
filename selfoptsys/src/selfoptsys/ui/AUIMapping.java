package selfoptsys.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class AUIMapping 
	implements UIMapping {

	int m_sourceUserIndex;
	int m_destUserIndex;
	MappedCommand m_mappedCommand;
	MappedDirection m_mappedDirection;
	boolean m_showTextualInfoInOE = true;
	Vector<Integer> m_overlaysInWhichIAmIn;
	protected int m_delay = 0;
	
	Vector<PropertyChangeListener> m_observers;
	
	public AUIMapping(
			int sourceUserIndex,
			int destUserIndex,
			int rootUserIndex,
			MappedCommand mappingType,
			MappedDirection mappedDirection,
			int delay
			) {
		m_sourceUserIndex = sourceUserIndex;
		m_destUserIndex = destUserIndex;
		m_mappedCommand = mappingType;
		m_mappedDirection = mappedDirection;
		m_delay = delay;
		
		m_overlaysInWhichIAmIn = new Vector<Integer>();
		m_overlaysInWhichIAmIn.add( rootUserIndex );
		
		m_observers = new Vector<PropertyChangeListener>();
	}
	
	public Vector<Integer> getOverlaysInWhichIAmIn() {
		return m_overlaysInWhichIAmIn;
	}
	
	public boolean preGetSourceUserIndex() {
		return m_showTextualInfoInOE;
	}
	public int getSourceUserIndex() {
		return m_sourceUserIndex;
	}
	
	public boolean preGetDestUserIndex() {
		return m_showTextualInfoInOE;
	}
	public int getDestUserIndex() {
		return m_destUserIndex;
	}
	
	public boolean preGetMappedCommand() {
		return m_showTextualInfoInOE;
	}
	public MappedCommand getMappedCommand() {
		return m_mappedCommand;
	}
	
	public boolean preGetMappedDirection() {
		return m_showTextualInfoInOE;
	}
	public MappedDirection getMappedDirection() {
		return m_mappedDirection;
	}
	
	public boolean equals( Object other ) {
        if (!( other instanceof UIMapping )) {
            return false;
        }
        
        return (m_sourceUserIndex == ( (UIMapping) other ).getSourceUserIndex() &&
        		m_destUserIndex == ( (UIMapping) other ).getDestUserIndex() &&
        		m_mappedCommand == ( (UIMapping) other ).getMappedCommand() );
    }
	
	public void addOverlayInWhichIAmIn(
			int overlayInWhichIAmIn
		) {
		m_overlaysInWhichIAmIn.add( overlayInWhichIAmIn );
	}
	public void removeOverlayInWhichIAmIn(
			int overlayInWhichIAmIn
		) {
		m_overlaysInWhichIAmIn.removeElement( overlayInWhichIAmIn );
	}
	public int getCount() {
		return m_overlaysInWhichIAmIn.size();
	}
	
	public void setShowTextualInfoInOE(
			boolean showTextualInfoInOE
			) {
		m_showTextualInfoInOE = showTextualInfoInOE;
	}
	
	public void setDelay(
			int delay
			) {
		int oldDelay = m_delay;
		m_delay = delay;
		notifyAllObservers(
				new PropertyChangeEvent( this, "delay", oldDelay, m_delay )
				);
	}
	public int getDelay() {
		return m_delay;
	}
	
	public void addPropertyChangeListener(
			PropertyChangeListener l
			) {
		m_observers.add( l );
	}
	
	private void notifyAllObservers(
			PropertyChangeEvent e
			) {
		for ( int i = 0; i < m_observers.size(); i++ ) {
			m_observers.elementAt( i ).propertyChange( e );
		}
	}
}
