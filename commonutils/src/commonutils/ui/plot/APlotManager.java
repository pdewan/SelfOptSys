package commonutils.ui.plot;

import java.awt.*;
import java.util.concurrent.*;

public class APlotManager 
	extends Thread 
	implements PlotManager {

	private BlockingQueue<PlotCommand> m_commands;
	
	private Plot m_plot;
	
	public APlotManager(
			Plot plot
			) {
		m_plot = plot;
		m_commands = new ArrayBlockingQueue<PlotCommand>( 100 );
	}
	
	public void run() {
		
		for (;;) {
			try {
				PlotCommand command = m_commands.take();
				processCommand( command );
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void put(
			PlotCommand command
			) {
		try {
			m_commands.put( command );
		}
		catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	private void processCommand(
			PlotCommand command
			) {
		
		PlotCommandType commandType = command.getPlotCommandType();
		
		if ( commandType == PlotCommandType.ADD_PLOT_LINE ) {
			addPlotLine(
					command.getPlotLineId()
					);
		}
		else if ( commandType == PlotCommandType.ADD_PLOT_VALUE ) {
			addPlotLineValue(
					command.getPlotLineId(),
					command.getPlotLineValue(),
					command.getPlotLineValueIndex()
					);
		}
		else if ( commandType == PlotCommandType.REMOVE_PLOT_LINE ) {
			removePlotLine(
					command.getPlotLineId()
					);
		}
		else if ( commandType == PlotCommandType.REMOVE_PLOT_VALUE ) {
			removePlotLinePointAtIndex(
					command.getPlotLineId(),
					command.getPlotLineValueIndex()
					);					
		}
		else if ( commandType == PlotCommandType.REMOVE_PLOT_LINE_VALUES ) {
			removePlotLineValues(
					command.getPlotLineId()
					);
		}
		
	}
	
	private PlotLine addPlotLine(
			String plotLineId
			) {
		
		PlotLine plotLine = m_plot.getPlotLine( plotLineId );
		if ( plotLine != null ) {
			return plotLine;
		}
		
		plotLine = m_plot.addPlotLine(
				plotLineId,
				getPlotLineColor()
				);
		
		return plotLine;
	}
	
	private void addPlotLineValue(
			String plotLineId,
			int value,
			int valueIndex
			) {
		
		PlotLine plotLine = m_plot.getPlotLine( plotLineId );
		
		if ( plotLine == null ) {
			plotLine = addPlotLine( plotLineId );
		}
		
		plotLine.addNewValue( 
				value,
				valueIndex
				);
		
	}
	
	private void removePlotLine(
			String plotLineId
			) {
		m_plot.removePlotLine( plotLineId );
	}
	
	private void removePlotLinePointAtIndex(
			String plotLineId,
			int pointIndex
			) {
		PlotLine plotLine = m_plot.getPlotLine( plotLineId );
		if ( plotLine == null ) {
			return;
		}
		plotLine.removePointAtIndex( pointIndex );
	}
	
    private Color getPlotLineColor() {
		Color c = Color.BLACK;

		int numPlotLines = m_plot.getPlotLines().size();
		switch (numPlotLines % 6) {
    		case 0: {
    			c = Color.RED;
    			break;
    		}
    		case 1: {
    			c = Color.BLUE;
    			break;
    		}
    		case 2: {
    			c = Color.BLACK;
    			break;
    		}
    		case 3: {
    			c = Color.GREEN;
    			break;
    		}
    		case 4: {
    			c = Color.ORANGE;
    			break;
    		}
    		case 5: {
    			c = Color.MAGENTA;
    			break;
    		}
		}
		
		return c;
    }
    
    private void removePlotLineValues(
    		String plotLineId
    		) {
    	PlotLine plotLine = m_plot.getPlotLine( plotLineId );
    	if ( plotLine == null ) {
    		return;
    	}
    	plotLine.clear();
    }
}