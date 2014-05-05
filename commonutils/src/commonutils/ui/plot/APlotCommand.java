package commonutils.ui.plot;

public class APlotCommand 
	implements PlotCommand {

	PlotCommandType m_plotCommandType;
	String m_plotLineId;
	int m_plotLineValue;
	int m_plotLineValueIndex;
	
	public APlotCommand(
			PlotCommandType plotCommandType,
			String plotLineId,
			int plotLineValue,
			int plotLineValueIndex
			) {
		m_plotCommandType = plotCommandType;
		m_plotLineId = plotLineId;
		m_plotLineValue = plotLineValue;
		m_plotLineValueIndex = plotLineValueIndex;
	}
	
	public PlotCommandType getPlotCommandType() {
		return m_plotCommandType;
	}
	
	public String getPlotLineId() {
		return m_plotLineId;
	}
	
	public int getPlotLineValue() {
		return m_plotLineValue;
	}
	
	public int getPlotLineValueIndex() {
		return m_plotLineValueIndex;
	}
	
}
