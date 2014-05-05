package selfoptsys.ui;

import java.awt.Color;
import java.awt.Stroke;

import commonutils.ui.shapes.*;



public interface Connector {

	public final int SHOW_MIDDLE_LINE_THRESHOLD = 30;
	public final int MIN_VERTICAL_LINE_HEIGHT = 20;
	public final int MIN_SEPARATION_BETWEEN_HORIZONTAL_LINES = 5;
	
	public final Color INPUT_CONNECTOR_COLOR = Color.RED;
	public final Color OUTPUT_CONNECTOR_COLOR = Color.BLUE;
	
	Point getStartLocation();
	void setStartLocation( Point newStartLocation );
	
	Point getEndLocation();
	void setEndLocation( Point newEndLocation );
	
	Line getStartLine();
	Line getMiddleLine();
	Line getEndLine();
	
	void setStroke(
			Stroke stroke
			);
}
