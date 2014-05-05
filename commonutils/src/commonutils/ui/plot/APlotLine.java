package commonutils.ui.plot;

import java.awt.Color;
import java.util.*;

import commonutils.ui.shapes.*;

import util.models.AListenableVector;

public class APlotLine
	implements PlotLine {

	private Plot m_parentPlot;
	private int m_originX;
	private int m_originY;
	private String m_lineId;
	
	private Color m_color = Color.BLACK;
	private double m_verticalScalingFactor = 1;
	private int m_horizontalTickWidth = 20;
	
	private Vector<PlotLineEntry> m_entries;
	private AListenableVector<Point> m_pointsToShow;
	private AListenableVector<Line> m_linesToShow;
	
	private int m_maxYValue = Integer.MIN_VALUE;
	private int m_minYValue = Integer.MAX_VALUE;
	
	public APlotLine(
			Plot parentPlot,
			int originX,
			int originY,
			String lineId
			) {
		m_parentPlot = parentPlot;
		m_originX = originX;
		m_originY = originY;
		m_lineId = lineId;
		
		m_entries = new Vector<PlotLineEntry>();
		m_pointsToShow = new AListenableVector<Point>();
		m_linesToShow = new AListenableVector<Line>();
	}
	
	public boolean preGetLineId() {
		return false;
	}
	public String getLineId() {
		return m_lineId;
	}
	
	public boolean preGetMaxYValue() {
		return false;
	}
	public int getMaxYValue() {
		return m_maxYValue;
	}
	
	public boolean preGetMinYValue() {
		return false;
	}
	public int getMinYValue() {
		return m_minYValue;
	}
	
	public void setHorizontalTickWidth(
			int horizontalTickWidth
			) {
		if ( m_horizontalTickWidth == horizontalTickWidth ) {
			return;
		}
		m_horizontalTickWidth = horizontalTickWidth;
		updateHorizontalOffsets();
	}
	public int getHorizontalTickWidth() {
		return m_horizontalTickWidth;
	}
	
	public void setColor(
			Color newLineColor
			) {
		m_color = newLineColor;
		updateColors();
	}
	public boolean preGetColor() {
		return false;
	}
	public Color getColor() {
		return m_color;
	}
	
	public void setVerticalScalingFactor( 
			double newVerticalScalingFactor 
			) {
		m_verticalScalingFactor = newVerticalScalingFactor;
		scaleLine();
	}
	
	public boolean preGetVerticalScalingFactor() {
		return false;
	}
	public double getVerticalScalingFactor() {
		return m_verticalScalingFactor;
	}
	
	public AListenableVector<Point> getPoints() {
		return m_pointsToShow;
	}
	
	public AListenableVector<Line> getLines() {
		return m_linesToShow;
	}
	
	public Vector<PlotLineEntry> getPlotLineEntries() {
		return m_entries;
	}
	
	public void addNewValue(
			int newValue,
			int valueIndex
			) {
		
		m_maxYValue = Math.max( m_maxYValue, newValue );
		m_minYValue = Math.min( m_minYValue, newValue );
		
		PlotLineEntry newEntry = null;
		for ( int i = 0; i < m_entries.size(); i++ ) {
			PlotLineEntry entry = m_entries.elementAt( i );
			if ( entry.getEntryIndex() == valueIndex ) {
				newEntry = entry;
				break;
			}
			else if ( entry.getEntryIndex() > valueIndex ) {
				newEntry = new APlotLineEntry(
						valueIndex,
						newValue,
						null,
						null
						);
				m_entries.insertElementAt( newEntry, i );
				break;
			}
		}
		
		if ( newEntry == null ) {
			newEntry = new APlotLineEntry(
					valueIndex,
					newValue,
					null,
					null
					);
			m_entries.add( newEntry );
		}
		
		int indexOfNewEntry = m_entries.indexOf( newEntry );
		
		int absXVal = m_originX + valueIndex * m_horizontalTickWidth;
		int absYVal = m_originY - (int)( newValue * m_verticalScalingFactor );
		Point newPoint = new APoint(
				absXVal,
				absYVal
				);
		newEntry.setPoint( newPoint );
		m_pointsToShow.add( newPoint );
		
		PlotLineEntry nextEntry = null;
		PlotLineEntry prevEntry = null;
		if ( m_entries.size() > indexOfNewEntry + 1 ) {
			nextEntry = m_entries.elementAt( indexOfNewEntry + 1 );
		}
		if ( indexOfNewEntry > 0 ) {
			prevEntry = m_entries.elementAt( indexOfNewEntry - 1 );
		}
		
		if ( prevEntry != null && nextEntry != null ) {
			m_linesToShow.removeElement( prevEntry.getLine() );
			prevEntry.setLine( null );
		}
		
		if ( prevEntry != null ) {
			Line line = createLine(
					prevEntry.getPoint(),
					newEntry.getPoint()
					);
			prevEntry.setLine( line );
			m_linesToShow.add( line );
		}
		
		if ( nextEntry != null ) {
			Line line = createLine(
					newEntry.getPoint(),
					nextEntry.getPoint()
					);
			newEntry.setLine( line );
			m_linesToShow.add( line );
		}
		
		if ( m_parentPlot != null ) {
			m_parentPlot.plotLineValuesChanged(
					m_lineId,
					newValue
					);
		}
	}

	public void removePointAtIndex(
			int pointIndex
			) {

		if ( m_entries.size() == 0 ) {
			return;
		}
		
		int indexOfEntryToRemove = -1;
		for ( int i = 0; i < m_entries.size(); i++ ) {
			if ( m_entries.elementAt( i ).getEntryIndex() == pointIndex ) {
				indexOfEntryToRemove = +i;
				break;
			}
		}
		
		if ( indexOfEntryToRemove == -1 ) {
			return;
		}
		
		PlotLineEntry entryToRemove = m_entries.elementAt( indexOfEntryToRemove );
		
		PlotLineEntry nextEntry = null;
		PlotLineEntry prevEntry = null;
		if ( m_entries.size() > indexOfEntryToRemove + 1 ) {
			nextEntry = m_entries.elementAt( indexOfEntryToRemove + 1 );
		}
		if ( indexOfEntryToRemove > 0 ) {
			prevEntry = m_entries.elementAt( indexOfEntryToRemove - 1 );
		}
		
		if ( prevEntry != null ) {
			m_linesToShow.remove( prevEntry.getLine() );
			prevEntry.setLine( null );
		}
		
		if ( prevEntry != null && nextEntry != null ) {
			Line line = createLine(
					prevEntry.getPoint(),
					nextEntry.getPoint()
					);
			prevEntry.setLine( line );
			m_linesToShow.add( line );
		}
		
		m_entries.removeElement( entryToRemove );
		m_linesToShow.removeElement( entryToRemove.getLine() );
		m_pointsToShow.removeElement( entryToRemove.getPoint() );
	}
	
	private Line createLine(
			Point startPoint,
			Point endPoint
			) {
		Line line = new ALine(
				startPoint.getX(),
				startPoint.getY(),
				endPoint.getX() - startPoint.getX(),
				endPoint.getY() - startPoint.getY()
				);
		line.setColor( m_color );
		
		return line;
	}
	
	private void updateHorizontalOffsets() {
		for ( int i = 0; i < m_entries.size(); i++ ) {
			PlotLineEntry entry = m_entries.elementAt( i );
			
			int newXValue = m_originX + entry.getEntryIndex() * m_horizontalTickWidth;
			entry.getPoint().setX( newXValue );
			entry.getLine().setLocation( entry.getPoint() );
			if ( i > 0 ) {
				PlotLineEntry prevEntry = m_entries.elementAt( i - 1 );
				prevEntry.getLine().setWidth(
						entry.getPoint().getX() - prevEntry.getPoint().getX()
						);
			}
		}
	}
	
	private void updateColors() {
		for ( int i = 0; i < m_entries.size(); i++ ) {
			m_entries.elementAt( i ).getLine().setColor( m_color );
		}
	}
	
	private void scaleLine() {
		for ( int i = 0; i < m_entries.size(); i++ ) {
			PlotLineEntry entry = m_entries.elementAt( i );
			int newYValue = 
				m_originY - (int)( entry.getEntryValue() * m_verticalScalingFactor );
			entry.getPoint().setY( newYValue );
			
			entry.getLine().setLocation( entry.getPoint() );
			if ( i > 0 ) {
				PlotLineEntry prevEntry = m_entries.elementAt( i - 1 );
				prevEntry.getLine().setWidth(
						entry.getPoint().getX() - prevEntry.getPoint().getX()
						);
				prevEntry.getLine().setHeight(
						entry.getPoint().getY() - prevEntry.getPoint().getY()
						);
			}
		}
	}
	
	public void clear() {
		m_entries.clear();
		while ( m_linesToShow.size() > 0 ) {
			m_linesToShow.remove( 0 );
		}
		while ( m_pointsToShow.size() > 0 ) {
			m_pointsToShow.remove( 0 );
		}
	}
	
}
