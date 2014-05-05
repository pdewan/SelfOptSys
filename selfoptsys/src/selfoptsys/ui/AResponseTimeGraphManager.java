package selfoptsys.ui;

import java.awt.Color;
import java.util.concurrent.*;

import selfoptsys.config.Parameters;

import commonutils.scheduling.SchedulingUtils;
import commonutils.ui.plot.*;
import commonutils.basic2.*;

public class AResponseTimeGraphManager 
	extends ASelfOptArchThread {

	BlockingQueue<PlotCommand> m_commands;
	
	Plot m_plot;
	
	public AResponseTimeGraphManager(
			Plot plot
			) {
		
		m_commands = new ArrayBlockingQueue<PlotCommand>( 100 );
		
		m_plot = plot;
		
        int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
        coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );
	}
	
	public void run() {
		super.run();
	    
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
	
	public void put( PlotCommand command ) {
		try {
			m_commands.put( command );
		}
		catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	void processCommand(
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
		
	}
	
	PlotLine addPlotLine(
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
	
	void addPlotLineValue(
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
	
    Color getPlotLineColor() {
		Color c = Color.BLACK;

		int numPlotLines = m_plot.getPlotLines().size();
		switch (numPlotLines % 4) {
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
		}
		
		return c;
    }
	
}
