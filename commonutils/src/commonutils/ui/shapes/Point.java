package commonutils.ui.shapes;

import java.beans.*;

public interface Point {
	
	public void setX( 
			int newX 
			);
	public int getX(); 
	
	public void setY( 
			int newY 
			);
	public int getY(); 	
	
	public double getAngle(); 
	
	public double getRadius();
	
	void addPropertyChangeListener(
			PropertyChangeListener l
			);
	
	void removePropertyChangeListener(
			PropertyChangeListener l
			);
	
}
