package commonutils.ui.plot;

import java.awt.Color;

import commonutils.ui.shapes.*;



import util.models.AListenableVector;

public class APlotLegend 
	implements PlotLegend {

	Rectangle m_border;
	AListenableVector<Line> m_lines;
	AListenableVector<Label> m_labels;
	
	int m_offsetX;
	int m_offsetY;
	
	public APlotLegend(
			int offsetX,
			int offsetY
			) {
		
		m_offsetX = offsetX;
		m_offsetY = offsetY;
		
		m_lines = new AListenableVector<Line>();
		m_labels = new AListenableVector<Label>();
		m_border = new ARectangle(
				m_offsetX,
				m_offsetY,
				MARGIN * 3 + LABEL_WIDTH + LINE_WIDTH,
				0
				);
	}
	
	public void addEntry(
			String text,
			Color color
			) {
		int entryX = MARGIN;
		int entryY = MARGIN + m_labels.size() * ( LABEL_HEIGHT + VERTICAL_ENTRY_SEPARATION );
		
		Line newLine = new ALine(
				getAdjustedX( entryX ),
				getAdjustedY( entryY + LABEL_HEIGHT / 2 ),
				LINE_WIDTH,
				0
				);
		newLine.setColor( color );
		m_lines.add( newLine );
		
		Label newLabel = new ALabel(
				getAdjustedX( entryX + LINE_WIDTH + MARGIN ),
				getAdjustedY( entryY ),
				LABEL_WIDTH,
				LABEL_HEIGHT,
				text,
				null
				);
		newLabel.setColor( color );
		m_labels.add( newLabel );
		
		m_border.setHeight( MARGIN * 2 + m_labels.size() * LABEL_HEIGHT );
	}
	
	public void removeEntry(
			String lineId
			) {
		int index = -1;
		for ( int i = 0; i < m_labels.size(); i++ ) {
			if ( m_labels.elementAt( i ).getText().equals( lineId ) ) {
				index = i;
				break;
			}
		}
		if ( index == -1 ) {
			return;
		}
		m_lines.removeElementAt( index );
		m_labels.removeElementAt( index );
	}
	
	public Rectangle getBorder() {
		return m_border;
	}
	
	public AListenableVector<Line> getLines() {
		return m_lines;
	}
	
	public AListenableVector<Label> getLabels() {
		return m_labels;
	}
	
	int getAdjustedX( int xValue ) {
		return xValue + m_offsetX;
	}
	
	int getAdjustedY( int yValue ) {
		return yValue + m_offsetY;
	}
}
