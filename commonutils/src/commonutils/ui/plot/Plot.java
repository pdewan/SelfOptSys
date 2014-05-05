package commonutils.ui.plot;

import java.awt.Color;

import commonutils.ui.shapes.*;

import util.models.AListenableVector;

public interface Plot {

	public final static int DEFAULT_ORIGIN_X = 60;
	public final static int DEFAULT_ORIGIN_Y = 650;
	public final static int DEFAULT_X_AXIS_LENGTH = 400;
	public final static int DEFAULT_Y_AXIS_LENGTH = 600;
	
	public final static int LEGEND_OFFSET_X = 150;
	public final static int LEGEND_OFFSET_Y = 10;
	
	public final static int TITLE_OFFSET_X = 10;
	public final static int TITLE_OFFSET_Y = 10;
	
	PlotLine addPlotLine(
			String lineId,
			Color color
			);
	
	void removePlotLine(
			String lineId
			);
	
	PlotLine getPlotLine(
			String lineId
			);
	
	AListenableVector<PlotLine> getPlotLines();

	Label getTitle();
	
	Line getXAxis();
	Line getYAxis();
	
	AListenableVector<PlotTickMark> getXAxisTickMarks();
	AListenableVector<PlotTickMark> getYAxisTickMarks();
	
	void setIsAutoScale( boolean newIsAutoScale );
	boolean getIsAutoScale();
	
//	void setShowLegend( boolean newShowLegend );
//	boolean getShowLegend();
	
	void autoScale();
	
	void plotLineValuesChanged(
		String plotLineName,
		int newValue
		);
	
}
