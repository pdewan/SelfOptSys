package selfoptsys;

import java.io.*;
import java.util.*;

import commonutils.basic.ErrorHandlingUtils;
import commonutils.basic.MathUtils;

public class ADefaultResultsPrinter 
	implements ResultsPrinter {

    protected final int THOUSAND = 1000;
    protected final int MILLION = 1000000;

    protected String m_outputFileName;
    protected int[] m_userTurns;
    protected Map<Integer, ATimeServerUserInfo> m_userInfos;
    protected List<Long> m_configurationChangeStartTimes;
    protected List<Long> m_configurationChangeEndTimes;
    
    protected PrintStream m_printStream;
    
    protected Vector<Integer> m_userIndicesSorted;
    protected int m_firstInputtingUserIndex;
    protected int m_firstSeqIdOfFirstInputtingUser;
    protected long m_firstInputEnteredReportTime;
    protected long m_taskCompletionTimeInMs;
                	
    public void printTimingInformation(
			String outputFileName,
			int[] userTurns,
			Map<Integer, ATimeServerUserInfo> userInfos,
			List<Long> configurationChangeStartTimes,
			List<Long> configurationChangeEndTimes
			) {
    	
    	m_outputFileName = outputFileName;
		m_userTurns = userTurns;
		m_userInfos = userInfos;
		m_configurationChangeStartTimes = configurationChangeStartTimes;
		m_configurationChangeEndTimes = configurationChangeEndTimes;
    	
        ErrorHandlingUtils.logInfoMessageAndContinue( this.getClass().getSimpleName() + ": Printing Timing Info ...", 1 );
        
        updateOutputFileName();
        m_printStream = getPrintStream( m_outputFileName );
        if ( m_printStream == System.out ) {
        	ErrorHandlingUtils.logWarningMessageAndContinue( "Could not print to file '" + outputFileName + "'. Print to screen ... ", 2 );
        }
        prepareToPrint();
        print();
        finishPrint();
        ErrorHandlingUtils.logInfoMessageAndContinue( "COMPLETED!", 2 );
		
	}
    
    protected void updateOutputFileName() {
    	int index = m_outputFileName.lastIndexOf( ".txt" );
    	if ( index == m_outputFileName.length() - 3 ) {
    		m_outputFileName = m_outputFileName.substring( 0, index - 1 );
    	}
    	m_outputFileName += ".default.txt";
    }
    
    protected PrintStream getPrintStream(
    		String outputFileName
    		) {
    	
    	PrintStream printStream = System.out;
    	
    	if ( outputFileName != null ) {
    		try {
    			printStream = new PrintStream( new File( outputFileName ) );
    		}
    		catch ( Exception e ) {
    			printStream = System.out;
    		}
    	}
    	
    	return printStream;

    }

    protected void prepareToPrint() {

        Iterator<Map.Entry<Integer, ATimeServerUserInfo>> userInfoIterator;

        m_userIndicesSorted = new Vector<Integer>();
        userInfoIterator = m_userInfos.entrySet().iterator();
        while ( userInfoIterator.hasNext() ) {
            ATimeServerUserInfo userInfo = userInfoIterator.next().getValue();
            if ( userInfo.getMeasurePerformance() == false ) {
                continue;
            }
            
            int insertionPoint = 0;
            for ( int i = 0; i < m_userIndicesSorted.size(); i++ ) {
                if ( m_userIndicesSorted.get( i ) < userInfo.getUserIndex() ) {
                    insertionPoint++;
                }
                else {
                    break;
                }
            }
            
            m_userIndicesSorted.insertElementAt(
                    userInfo.getUserIndex(),
                    insertionPoint
                    );
        }
        
        m_firstInputtingUserIndex = m_userTurns[ 0 ];
        m_firstSeqIdOfFirstInputtingUser = 0;
        m_firstInputEnteredReportTime = 
            m_userInfos.get( m_firstInputtingUserIndex ).getInputEnteredTime( m_firstSeqIdOfFirstInputtingUser );
        long lastOutputProcessedReportTime = Long.MIN_VALUE;
        int lastInputtingUserIndex = m_userTurns[ m_userTurns.length - 1 ];
        int lastSeqIdOfLastInputtingUser = ALoggerUtils.getSeqIdOfFinalInputCommandForUser(
                lastInputtingUserIndex,
                m_userTurns
                );
        
        userInfoIterator = m_userInfos.entrySet().iterator();
        while ( userInfoIterator.hasNext() ) {
            ATimeServerUserInfo userInfo = userInfoIterator.next().getValue();
            if ( userInfo.getMeasurePerformance() == false ) {
                continue;
            }
            
            long outputProcessedReportTime = 
                userInfo.getOutputProcessedTime(
                        lastInputtingUserIndex,
                        lastSeqIdOfLastInputtingUser
                        );
            
            lastOutputProcessedReportTime = Math.max( lastOutputProcessedReportTime, outputProcessedReportTime );
        }
        
        m_taskCompletionTimeInMs = 
            ( lastOutputProcessedReportTime - m_firstInputEnteredReportTime ) / MILLION;
        

    }
    
	protected void print() {
        
		int curConfigChangeStartIndex = 0;
	    int curConfigChangeEndIndex = 0;
	    
	    m_printStream.println( "Response Times (in ms):" );
        for ( int i = 0; i < m_userTurns.length; i++ ) {
            String outputLine = "";
            
            m_printStream.print( "Turn " + i );
            m_printStream.print( "\t" + "SChng" );
            m_printStream.print( "\t" + "EChng" );
            m_printStream.print( "\t" + "ChngT" );
            for ( int j = 0; j < m_userIndicesSorted.size(); j++ ) {
                String userIndex = "user " + m_userIndicesSorted.elementAt( j ); 
                m_printStream.print( "\t" + userIndex );
            }
            m_printStream.println( "" );
            
            ATimeServerUserInfo inputtingUserInfo = m_userInfos.get( m_userTurns[ i ] );
            int inputtingUserSeqId = ALoggerUtils.getSeqIdForUserOfInputCommand(
                    inputtingUserInfo.getUserIndex(),
                    i,
                    m_userTurns
                    );
            
            long curInputEnteredTime =
                inputtingUserInfo.getInputEnteredTime(
                        inputtingUserSeqId
                        );
            
            outputLine += "\t";
            
            boolean startChangeHappened = false;
            if ( m_configurationChangeStartTimes.size() > curConfigChangeStartIndex ) {
                long configChangeStartTime = m_configurationChangeStartTimes.get( curConfigChangeStartIndex );
                if ( configChangeStartTime <= curInputEnteredTime ) {
                    outputLine += "\t" + "1";
                    curConfigChangeStartIndex++;
                    startChangeHappened = true;
                }
            }
            if ( startChangeHappened == false ) {
                outputLine += "\t" + "\t";
            }
            
            boolean endChangeHappened = false;
            if ( m_configurationChangeEndTimes.size() > curConfigChangeEndIndex ) {
                long configChangeEndTime = m_configurationChangeEndTimes.get( curConfigChangeEndIndex );
                if ( configChangeEndTime <= curInputEnteredTime ) {
                    double time = 
                        configChangeEndTime -
                        m_configurationChangeStartTimes.get( curConfigChangeEndIndex );
                    time = MathUtils.round( ( time / 1000 ) / (double) 1000 , 3 );
                    outputLine += "\t" + "1" + "\t" + time;
                    curConfigChangeEndIndex++;
                    endChangeHappened = true;
                }
            }
            if ( endChangeHappened == false ) {
                outputLine += "\t" + "\t" + "\t" + "\t";
            }
            
            for ( int j = 0; j < m_userIndicesSorted.size(); j++ ) {
                int curUserIndex = m_userIndicesSorted.elementAt( j );
                ATimeServerUserInfo curUserInfo = m_userInfos.get( curUserIndex );

                long responseTime = 
                    curUserInfo.getOutputProcessedTime(
                            inputtingUserInfo.getUserIndex(),
                            inputtingUserSeqId
                            ) - 
                    inputtingUserInfo.getInputEnteredTime(
                            inputtingUserSeqId
                            );
                responseTime /= THOUSAND;
                double responseTimeDouble = (double) responseTime / (double) THOUSAND;
                
                outputLine += "\t" + MathUtils.round( responseTimeDouble, 2 );
            }
            
            m_printStream.print( outputLine );
            
            m_printStream.println( "" );
        }        
        
        m_printStream.println( "" );
        m_printStream.println( "Task Completion Time (in ms): " );
        m_printStream.println( m_taskCompletionTimeInMs );
        
	}
	
	protected void finishPrint() {
        
		m_printStream.flush();
        try {
            Thread.sleep(1000);
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "Error while printing timing information",
                    e
                    );
        }

	}

}
