package commonutils.basic;

import java.util.*;
import java.util.logging.*;

public class LoggingUtils {
	
    private static boolean m_initialized = false;
	private static Level m_loggingLevel = Level.FINEST;
    private static Logger m_severeLogger = Logger.getLogger( "SEVERE" );
    private static Logger m_nonSevereLogger = Logger.getLogger( "NONSEVERE" );
    	
	public static synchronized void setLoggingLevel(
			String loggingLevel
			) {
		
		if ( loggingLevel.toLowerCase().equals( "severe" ) ) {
			m_loggingLevel = Level.SEVERE;
		}
		else if ( loggingLevel.toLowerCase().equals( "warning" ) ) {
			m_loggingLevel = Level.WARNING;
		}
		else if ( loggingLevel.toLowerCase().equals( "info" ) ) {
			m_loggingLevel = Level.INFO;
		}
		else if ( loggingLevel.toLowerCase().equals( "config" ) ) {
			m_loggingLevel = Level.CONFIG;
		}
		else if ( loggingLevel.toLowerCase().equals( "fine" ) ) {
			m_loggingLevel = Level.FINE;
		}
		else if ( loggingLevel.toLowerCase().equals( "finer" ) ) {
			m_loggingLevel = Level.FINER;
		}
		else if ( loggingLevel.toLowerCase().equals( "finest" ) ) {
			m_loggingLevel = Level.FINEST;
		}
		else {
			m_loggingLevel = Level.WARNING;
		}
		
		if ( m_initialized ) {
			m_severeLogger.setLevel( m_loggingLevel );
			m_nonSevereLogger.setLevel( m_loggingLevel );
		}
		
	}
	
	public static synchronized Level getLoggingLevel() {
		return m_loggingLevel;
	}
	
	public static synchronized void createLoggers(
	        boolean reinit
	        ) {
	    
	    if ( m_initialized == true && reinit == false ) {
	        return;
	    }
	    
	    if ( !m_initialized ) {
            Logger.getLogger( "" ).removeHandler( Logger.getLogger( "" ).getHandlers()[0] );
	    }
	    
	    if ( m_initialized ) {
	        m_severeLogger.removeHandler( m_severeLogger.getHandlers()[0] );
	        m_nonSevereLogger.removeHandler( m_nonSevereLogger.getHandlers()[0] );
	    }
	    
        Handler severeHandler = new AMyConsoleHandler( System.err );
        severeHandler.setFormatter( new SingleLineFormatter() );
        m_severeLogger.addHandler( severeHandler );
        m_severeLogger.setLevel( getLoggingLevel() );

        Handler nonSevereHandler = new AMyConsoleHandler( System.out );
        nonSevereHandler.setFormatter( new SingleLineFormatter() );
        m_nonSevereLogger.addHandler( nonSevereHandler );
        m_nonSevereLogger.setLevel( getLoggingLevel() );

        m_initialized = true;
	}
	
	public static synchronized void createLoggers(
	        String loggingLevel
	        ) {

	    setLoggingLevel( loggingLevel );
	    createLoggers( true );
	}
	
	public static void logMessage(
	        Level level,
	        String message
	        ) {
	    if ( level == Level.SEVERE ) {
	        m_severeLogger.log( level, message );
	    }
	    else {
	        m_nonSevereLogger.log( level, message );
	    }
	}
	
}

class SingleLineFormatter 
    extends java.util.logging.Formatter {

    public String format(
            LogRecord record
            ) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( record.getMillis() );
        String message = 
            "[" + c.getTime().toString() + "]" +
            " " + record.getLevel() + ":" + 
            " " + record.getMessage() + "\n";
        return message;
    }
    
}
