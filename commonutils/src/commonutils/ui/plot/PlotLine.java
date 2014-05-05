package commonutils.ui.plot;

import java.awt.Color;
import java.util.*;

import commonutils.ui.shapes.*;

import util.models.AListenableVector;

public interface PlotLine {

	String getLineId();
	AListenableVector<Point> getPoints();
	AListenableVector<Line> getLines();
	
	void setColor( 
			Color newLineColor
			);
	Color getColor();
	
	void addNewValue(
			int newValue,
			int valueIndex
			);

	void removePointAtIndex(
			int pointIndex
			);
	
	void setVerticalScalingFactor(
			double newVerticalScalingFactor
			);
	double getVerticalScalingFactor();
	
	int getMinYValue();
	int getMaxYValue();
	
	void clear();
	
	void setHorizontalTickWidth(
			int tickWidth
			);
	int getHorizontalTickWidth();
	
	Vector<PlotLineEntry> getPlotLineEntries();
	
}
