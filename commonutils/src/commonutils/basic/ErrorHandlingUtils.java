package commonutils.basic;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Level;


public class ErrorHandlingUtils {

    private static AtomicBoolean m_outputStreamInitialized = new AtomicBoolean( false );
    private static PrintStream m_outputStream = null;
    
    private static void createOutputStream() {
        try {
            boolean outputStreamAlreadyInitialized = m_outputStreamInitialized.getAndSet( true );
            if ( outputStreamAlreadyInitialized ) {
                return;
            }
            
            m_outputStream = new PrintStream( new FileOutputStream( new File( "ExceptionsLog.txt" ), false ) );
        }
        catch ( Exception e ) {
            LoggingUtils.logMessage(
                    Level.SEVERE,
                    "ErrorHandlingUtils: Could not create output stream: " + 
                    "Exception: " + e.getMessage() + "\n" + 
                    ErrorHandlingUtils.printExceptionStackTraceToString( e )
                    );
        }
    }
    
    private static void logExceptionToFile(
            String message,
            Exception e
            ) {
        createOutputStream();
        
        if ( m_outputStream != null ) {
            Date now = new Date( System.currentTimeMillis() );
            m_outputStream.println(
                    now.toString() + " \n" + 
                    message + 
                    "Exception: " + e.getMessage() + "\n" + 
                    ErrorHandlingUtils.printExceptionStackTraceToString( e ) + "\n"
                    );
        }
    }
    
    public static String printExceptionStackTraceToString(
            Exception e
            ) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );
            return sw.toString();
        }
        catch ( Exception exception ) {
            return "bad stack2string";
        }
    }

    public static void logSevereExceptionAndContinue(
            String message,
            Exception e
            ) {
        logSevereExceptionAndContinue(
        		message,
        		e,
        		0
        		);
    }
    
    public static void logSevereExceptionAndContinue(
            String message,
            Exception e,
            int indentationLevel
            ) {
        LoggingUtils.createLoggers( false );
        
        String outputMessage = "";
        if ( message != null && message != "" ) {
            outputMessage += message + "\n";
        }
        
        String indentation = getIndentationStringForIndentationLevel( indentationLevel );
        logExceptionToFile(
                outputMessage,
                e
                );
        LoggingUtils.logMessage(
                Level.SEVERE,
                indentation +
                outputMessage + 
                "Exception: " + e.getMessage() + "\n" + 
                ErrorHandlingUtils.printExceptionStackTraceToString( e )
                );
    }
    
    public static void logSevereMessageAndContinue(
            String message
            ) {
        logSevereMessageAndContinue(
        		message,
        		0
        		);
    }
    
    public static void logSevereMessageAndContinue(
            String message,
            int indentationLevel
            ) {
        LoggingUtils.createLoggers( false );
        
        String indentation = getIndentationStringForIndentationLevel( indentationLevel );
        LoggingUtils.logMessage(
                Level.SEVERE,
                indentation + 
                message
                );
    }
    
    public static void logWarningMessageAndContinue(
            String message
            ) {
        logWarningMessageAndContinue(
        		message,
        		0
        		);
    }
    
    public static void logWarningMessageAndContinue(
            String message,
            int indentationLevel
            ) {
        LoggingUtils.createLoggers( false );
        
        String indentation = getIndentationStringForIndentationLevel( indentationLevel );
        LoggingUtils.logMessage(
                Level.WARNING,
                indentation +
                message
                );
    }
    
    public static void logInfoMessageAndContinue(
            String message
            ) {
        logInfoMessageAndContinue(
        		message,
        		0
        		);
    }
    
    public static void logInfoMessageAndContinue(
            String message,
            int indentationLevel
            ) {
        LoggingUtils.createLoggers( false );
        
        String indentation = getIndentationStringForIndentationLevel( indentationLevel );
        LoggingUtils.logMessage(
                Level.INFO,
                indentation +
                message
                );
    }
    
    public static void logFineMessageAndContinue(
            String message
            ) {
        logFineMessageAndContinue(
        		message,
        		0
        		);
    }
    
    public static void logFineMessageAndContinue(
            String message,
            int indentationLevel
            ) {
        LoggingUtils.createLoggers( false );
        
        String indentation = getIndentationStringForIndentationLevel( indentationLevel );
        LoggingUtils.logMessage(
                Level.FINE,
                indentation +
                message
                );
    }
    
    private static String getIndentationStringForIndentationLevel(
    		int indentationLevel
    		) {
    	String str = "";
    	
    	for ( int i = 0; i < indentationLevel; i++ ) {
    		str += "\t";
    	}
    	
    	return str;
    }
    
}
