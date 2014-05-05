package commonutils.ui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.beans.*;
import java.util.*;

public abstract class AShape
	implements Shape {

	Point m_location;
	int m_width;
	int m_height;
	Color m_color = Color.BLACK;
	boolean m_filled = false;
	Stroke m_stroke = new BasicStroke();
	
	Vector<PropertyChangeListener> m_listeners;
	
	public AShape(
			int initX, 
			int initY, 
			int initWidth, 
			int initHeight
			) {
		m_location = new APoint(
				initX, 
				initY
				);
		m_width = initWidth;
		m_height = initHeight;
		
		m_listeners = new Vector<PropertyChangeListener>();
	}
	
	public Point getLocation() {
		return m_location;
	}
	public void setLocation( 
			Point newLocation 
			) {
		Point oldLocation = m_location;
		m_location = newLocation;
		notifyAllListeners( new PropertyChangeEvent(
				this, "location", oldLocation, newLocation ) );
	}
	
	public int getWidth() {
		return m_width;
	}
	public void setWidth( 
			int newWidth 
			) {
		int oldWidth = m_width;
		m_width = newWidth;
		notifyAllListeners( new PropertyChangeEvent(
				this, "width", oldWidth, newWidth ) );
	}
	
	public int getHeight() {
		return m_height;
	}
	public void setHeight( 
			int newHeight 
			) {
		int oldHeight = m_height;
		m_height = newHeight;
		notifyAllListeners( new PropertyChangeEvent(
				this, "height", oldHeight, newHeight ) );
	}
	
	public Color getColor() {
		return m_color;
	}
	public void setColor( 
			Color newColor
			) {
		Color oldColor = m_color;
		m_color = newColor;
		notifyAllListeners( new PropertyChangeEvent(
				this, "color", oldColor, newColor ) );
	}
	
	public Stroke getStroke() {
		return m_stroke;
	}
	public void setStroke(
			Stroke stroke
			) {
		m_stroke = stroke;
	}
	
	public boolean isFilled() {
		return m_filled;
	}
	public void isFilled( 
			boolean newFilled 
			) {
		boolean oldFilled = m_filled;
		m_filled = newFilled;
		notifyAllListeners( new PropertyChangeEvent(
				this, "filled", oldFilled, newFilled ) );
	}
	
	public void addPropertyChangeListener(
			PropertyChangeListener l
			) {
		m_listeners.add( l );
	}
	
	public void removePropertyChangeListener(
			PropertyChangeListener l
			) {
		m_listeners.remove( l );
	}
	
	void notifyAllListeners(
			PropertyChangeEvent evt
			) {
		for ( int i = 0; i < m_listeners.size(); i++ ) {
			m_listeners.elementAt( i ).propertyChange( evt );
		}
	}

}
