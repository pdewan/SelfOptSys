package commonutils.ui.plot;

import commonutils.ui.shapes.*;

public class APlotTickMark 
	implements PlotTickMark {

	private Label m_tickLabel;
	private Line m_tickLine;
	
	public APlotTickMark(
			String text,
			Point lineStartPoint,
			Point lineEndPoint,
			boolean xAxisTickMark
			) {
		
		if ( xAxisTickMark ) { 
			m_tickLabel = new ALabel(
					lineStartPoint.getX() - 5,
					lineEndPoint.getY() + 10,
					20,
					20,
					text,
					null
					);
		}
		else {
			m_tickLabel = new ALabel(
					lineStartPoint.getX() - 40,
					lineStartPoint.getY() - 10,
					30,
					20,
					text,
					null
					);
		}
		
		m_tickLine = new ALine(
				lineStartPoint.getX(),
				lineStartPoint.getY(),
				lineEndPoint.getX() - lineStartPoint.getX(),
				lineEndPoint.getY() - lineStartPoint.getY()
				);
	}
	
	public Label getLabel() {
		return m_tickLabel;
	}

	public Line getLine() {
		return m_tickLine;
	}

}
