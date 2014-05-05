package commonutils.ui.plot;

import commonutils.ui.shapes.*;

public interface PlotLineEntry {

	int getEntryIndex();
	int getEntryValue();
	
	void setPoint(
			Point newPoint
			);
	Point getPoint();
	
	void setLine(
			Line newLine
			);
	Line getLine();
	
}
