package selfoptsys;

import commonutils.basic.MathUtils;

public class ADetailedDefaultResultsPrinter
	extends ADefaultResultsPrinter 
	implements ResultsPrinter {

    protected void updateOutputFileName() {
    	int index = m_outputFileName.lastIndexOf( ".txt" );
    	if ( index == m_outputFileName.length() - 3 ) {
    		m_outputFileName = m_outputFileName.substring( 0, index - 1 );
    	}
    	m_outputFileName += ".detaileddefault.txt";
    }

    protected void print() {
        
        m_printStream.println( "Response Times (in ms):" );
        for ( int i = 0; i < m_userTurns.length; i++ ) {
            m_printStream.print( "Turn " + i );
            for ( int j = 0; j < m_userIndicesSorted.size(); j++ ) {
                String userIndex = "user " + m_userIndicesSorted.elementAt( j ); 
                m_printStream.print( "\t" + userIndex );
            }
            m_printStream.println( "" );
            
            String lineInputReportTimes = "";
            String lineOutputReportTimes = "";
            String lineResponseTimes = "";

            for ( int j = 0; j < m_userIndicesSorted.size(); j++ ) {
                int curUserIndex = m_userIndicesSorted.elementAt( j );
                ATimeServerUserInfo curUserInfo = m_userInfos.get( curUserIndex );
                ATimeServerUserInfo inputtingUserInfo = m_userInfos.get( m_userTurns[ i ] );

                int inputtingUserSeqId = ALoggerUtils.getSeqIdForUserOfInputCommand(
                        inputtingUserInfo.getUserIndex(),
                        i,
                        m_userTurns
                        );
                
                long curInputEnteredTimeRawWrtStart = 
                    inputtingUserInfo.getInputEnteredTime( inputtingUserSeqId ) - 
                    m_firstInputEnteredReportTime;
                double curInputEnteredTime = 
                    MathUtils.round( ( curInputEnteredTimeRawWrtStart / 1000 ) / (double) 1000, 4 );
                lineInputReportTimes += "\t" + curInputEnteredTime;

                long curOutputProcessedTimeRawWrtStart = 
                    curUserInfo.getOutputProcessedTime( inputtingUserInfo.getUserIndex(), inputtingUserSeqId ) - 
                    m_firstInputEnteredReportTime;
                double curOutputEnteredTime = 
                    MathUtils.round( ( curOutputProcessedTimeRawWrtStart / 1000 ) / (double) 1000, 4 );
                lineOutputReportTimes += "\t" + curOutputEnteredTime;
                
                long responseTime = 
                    curUserInfo.getOutputProcessedTime(
                            inputtingUserInfo.getUserIndex(),
                            inputtingUserSeqId
                            ) - 
                    inputtingUserInfo.getInputEnteredTime(
                            inputtingUserSeqId
                            );
                responseTime /= THOUSAND;
                double responseTimeDouble = 
                    MathUtils.round( (double) responseTime / (double) THOUSAND, 4 );
                lineResponseTimes += "\t" + responseTimeDouble;
                
            }
            
            m_printStream.println( lineInputReportTimes );
            m_printStream.println( lineOutputReportTimes );
            m_printStream.println( lineResponseTimes );
            m_printStream.println( "" );
        }        
        
        m_printStream.println( "" );
        m_printStream.println( "Task Completion Time (in ms): " );
        m_printStream.println( m_taskCompletionTimeInMs );
        
	}
	
}
