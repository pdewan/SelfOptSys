package commonutils.ui.shapes;

import java.awt.Color;
import java.awt.Stroke;
import java.beans.*;

public interface Shape {

	public Point getLocation();
	public void setLocation( Point newLocation );
	
	public int getWidth();
	public void setWidth( int newWidth );
	
	public int getHeight();
	public void setHeight( int newHeight );
	
	public Color getColor();
	public void setColor( Color newColor );
	
	Stroke getStroke();
	void setStroke(
			Stroke stroke
			);
	
	public boolean isFilled();
	public void isFilled( boolean newFilled );
	
	void addPropertyChangeListener(
			PropertyChangeListener l
			);
	
	void removePropertyChangeListener(
			PropertyChangeListener l
			);
	
}
