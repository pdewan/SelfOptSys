package commonutils.ui.plot;

import java.awt.Color;

import commonutils.ui.shapes.*;

import util.models.AListenableVector;

public class APlot
	implements Plot {

	private AListenableVector<PlotLine> m_plotLines;
	
	private int m_originX;
	private int m_originY;
//	private int m_xAxisLength;
	private int m_yAxisLength;
	
	private Label m_title;
	
	private Line m_yAxis;
	private Line m_xAxis;
	
	private AListenableVector<PlotTickMark> m_yAxisTickMarks;
	private AListenableVector<PlotTickMark> m_xAxisTickMarks;
	private int m_horizontalTickWidth = 20;
	
	private boolean m_isAutoScale = false;
	private int m_prevMaxYValue = Integer.MIN_VALUE;
	
//	private boolean m_showLegend = false;
	private PlotLegend m_legend;
	
	private final int NUM_Y_AXIS_TICKS = 4;
	private final int NUM_X_AXIS_TICKS = 20;
	
	public APlot(
			String title,
			int originX,
			int originY,
			int xAxisLength,
			int yAxisLength
			) {
		m_plotLines = new AListenableVector<PlotLine>();
		
		m_originX = originX;
		m_originY = originY;
//		m_xAxisLength = xAxisLength;
		m_yAxisLength = yAxisLength;
		
		m_xAxis = new ALine(
				m_originX,
				m_originY,
				m_originX + xAxisLength,
				0
				);

		m_yAxis = new ALine(
				m_originX,
				m_originY,
				0,
				- m_yAxisLength
				);
		
		m_title = new ALabel(
				TITLE_OFFSET_X,
				TITLE_OFFSET_Y,
				200,
				20,
				title,
				null
				);
		
		m_yAxisTickMarks = new AListenableVector<PlotTickMark>();
		m_xAxisTickMarks = new AListenableVector<PlotTickMark>();
		int tickStep = m_yAxisLength / NUM_Y_AXIS_TICKS;
		int tickWidth = 10;
		
		for ( int i = 0; i < NUM_Y_AXIS_TICKS + 1; i++ ) {
			int tickX = m_originX - 5;
			int tickY = m_originY - i * tickStep;
			Point startPoint = new APoint(
					tickX,
					tickY
					);
			Point endPoint = new APoint(
					tickX + tickWidth,
					tickY
					);
			
			String tickValueText = new Integer( i * tickStep ).toString();
			
			PlotTickMark tickMark = new APlotTickMark(
					tickValueText,
					startPoint,
					endPoint,
					false
					);
			m_yAxisTickMarks.add( tickMark );
		}
		
		for ( int i = 0; i < NUM_X_AXIS_TICKS + 1; i++ ) {
			int tickX = m_originX + i * m_horizontalTickWidth;
			int tickY = m_originY - 10;
			int tickHeight = 20;
			Point startPoint = new APoint(
					tickX,
					tickY
					);
			Point endPoint = new APoint(
					tickX,
					tickY + tickHeight
					);
			
			String tickValueText = new Integer( i ).toString();
			
			PlotTickMark tickMark = new APlotTickMark(
					tickValueText,
					startPoint,
					endPoint,
					true
					);
			m_xAxisTickMarks.add( tickMark );
		}

		
		m_legend = new APlotLegend(
				LEGEND_OFFSET_X,
				LEGEND_OFFSET_Y
				);
	}
	
	public PlotLine addPlotLine(
			String name,
			Color color
			) {
		
		PlotLine newPlotLine = new APlotLine(
				this,
				m_originX,
				m_originY,
				name
				);
		newPlotLine.setColor( color );
		newPlotLine.setHorizontalTickWidth( m_horizontalTickWidth );
		m_plotLines.add( newPlotLine );
		
		m_legend.addEntry(
				name,
				color
				);
		
		return newPlotLine;
		
	}
	
	public AListenableVector<PlotLine> getPlotLines() {
		return m_plotLines;
	}
	
	public void removePlotLine(
			String lineId
			) {
		
		for ( int i = 0; i < m_plotLines.size(); i++ ) {
			if ( m_plotLines.elementAt( i ).getLineId().equals( lineId ) ) {
				m_plotLines.removeElementAt( i );
				break;
			}
		}
		
		m_legend.removeEntry( lineId );
		
	}
	
	public PlotLine getPlotLine(
			String lineId
			) {

		PlotLine plotLine = null;
		
		for ( int i = 0; i < m_plotLines.size(); i++ ) {
			if ( m_plotLines.elementAt( i ).getLineId().equals( lineId ) ) {
				plotLine = m_plotLines.elementAt( i );
				break;
			}
		}
		
		return plotLine;
		
	}
	
	public Label getTitle() {
		return m_title;
	}
	
	public Line getXAxis() {
		return m_xAxis;
	}
	
	public Line getYAxis() {
		return m_yAxis;
	}
	
	public AListenableVector<PlotTickMark> getXAxisTickMarks() {
		return m_xAxisTickMarks;
	}
	
	public AListenableVector<PlotTickMark> getYAxisTickMarks() {
		return m_yAxisTickMarks;
	}
	
	public void setIsAutoScale( boolean newIsAutoScale ) {
		m_isAutoScale = newIsAutoScale;
		
		if ( m_isAutoScale ) {
			scalePlot();
		}
	}
	
	public boolean getIsAutoScale() {
		return m_isAutoScale;
	}
	
	public void autoScale(){
		scalePlot();
	}
	
//	public void setShowLegend( boolean newShowLegend ) {
//		m_showLegend = newShowLegend;
//		m_legend.setIsVisible( newShowLegend );
//	}
//	
//	public boolean getShowLegend() {
//		return m_showLegend;
//	}
	
//	public boolean preGetLegend() {
//		return m_showLegend;
//	}
	public PlotLegend getLegend() {
		return m_legend;
	}
	
	void scalePlot() {
		
		double newScalingFactor = 1;
		int maxYValue = Integer.MIN_VALUE;
		
		for ( int i = 0; i < m_plotLines.size(); i++ ) {
			maxYValue = Math.max( m_plotLines.elementAt( i ).getMaxYValue(), maxYValue );
		}
		
		if ( maxYValue > m_prevMaxYValue && maxYValue > m_yAxisLength ) {
			m_prevMaxYValue = maxYValue;
			newScalingFactor = Math.min( 1, (double) m_yAxisLength / (double) maxYValue );

			for ( int i = 0; i < m_plotLines.size(); i++ ) {
				m_plotLines.elementAt( i ).setVerticalScalingFactor( newScalingFactor );
			}

			int tickStep = maxYValue / NUM_Y_AXIS_TICKS;
			for ( int i = 0; i < NUM_Y_AXIS_TICKS + 1; i++ ) {
				String tickValueText = new Integer( i * tickStep ).toString();
				m_yAxisTickMarks.elementAt( i ).getLabel().setText( tickValueText );
			}
		}
	}
	
	public void plotLineValuesChanged(
			String plotLineName,
			int newValue
			) {
		
		if ( m_prevMaxYValue > newValue ) {
			return;
		}
		
		if ( m_isAutoScale ) {
			scalePlot();
		}
		
	}
	
}
