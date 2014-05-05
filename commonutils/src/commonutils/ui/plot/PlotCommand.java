package commonutils.ui.plot;

public interface PlotCommand {

	PlotCommandType getPlotCommandType();
	
	String getPlotLineId();
	
	int getPlotLineValue();
	
	int getPlotLineValueIndex();
	
}
