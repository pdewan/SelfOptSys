package commonutils.ui.shapes;

import java.beans.*;
import java.util.*;

public class APoint implements Point {
	
	int m_x;
	int m_y;
	
	Vector<PropertyChangeListener> m_listeners;
	
	public APoint(
			int theX, 
			int theY
			) {
		m_x = theX;
		m_y = theY;
		
		m_listeners = new Vector<PropertyChangeListener>();
	}
	public void setX( 
			int newX 
			) {
		int oldX = m_x;
		m_x = newX;
		notifyAllListeners( new PropertyChangeEvent(
				this, "x", oldX, newX ) );
	}
	public int getX() {
		return m_x;
	}
	
	public void setY( 
			int newY 
			) {
		int oldY = m_y;
		m_y = newY;
		notifyAllListeners( new PropertyChangeEvent(
				this, "y", oldY, newY ) );
	}
	public int getY() {
		return m_y;
	} 
	
	public double getAngle() {
		return Math.atan( (double) m_y / m_x );
	}
	
	public double getRadius() {
		return  Math.sqrt( m_x * m_x + m_y * m_y );
	}
	
	public void addPropertyChangeListener(
			PropertyChangeListener l
			) {
		m_listeners.add( l );
	}
	
	public 	void removePropertyChangeListener(
			PropertyChangeListener l ) {
		m_listeners.remove( l );
	}

	
	void notifyAllListeners(
			PropertyChangeEvent e
			) {
		for (int i = 0; i < m_listeners.size(); i++) {
			m_listeners.elementAt( i ).propertyChange( e );
		}
	}
	
}