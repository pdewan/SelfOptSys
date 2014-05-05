package commonutils.ui.plot;

import java.awt.Color;

import commonutils.ui.shapes.*;

import util.models.AListenableVector;

public interface PlotLegend {

	final int LABEL_WIDTH = 150;
	final int LABEL_HEIGHT = 20;
	
	final int LINE_WIDTH = 20;
	
	final int VERTICAL_ENTRY_SEPARATION = 5;
	final int MARGIN = 5;
	
	void addEntry(
			String text,
			Color color
			);
	
	void removeEntry(
			String text
			);
	
	Rectangle getBorder();
	AListenableVector<Line> getLines();
	AListenableVector<Label> getLabels();
	
//	void setIsVisible( boolean newIsVisible );
//	boolean getIsVisible();
	
}
