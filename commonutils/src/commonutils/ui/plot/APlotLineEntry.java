package commonutils.ui.plot;

import commonutils.ui.shapes.*;

public class APlotLineEntry 
	implements PlotLineEntry {
	
	private int m_entryIndex;
	private int m_entryValue;
	private Point m_point;
	private Line m_line;
	
	public APlotLineEntry(
			int entryIndex,
			int entryValue,
			Point point,
			Line line
			) {
		m_entryIndex = entryIndex;
		m_entryValue = entryValue;
		m_point = point;
		m_line = line;
	}
	
	public int getEntryIndex() {
		return m_entryIndex;
	}
	
	public int getEntryValue() {
		return m_entryValue;
	}
	
	public void setPoint(
			Point newPoint
			) {
		m_point = newPoint;
	}
	public Point getPoint() {
		return m_point;
	}
	
	public void setLine(
			Line newLine
			) {
		m_line = newLine;
	}
	public Line getLine() {
		return m_line;
	}
			
}

