package selfoptsys;

import java.io.*;
import selfoptsys.config.*;
import commonutils.basic.MathUtils;
import commonutils.basic2.*;


public class ALoggableCostReporter {

    public void printCosts(
            ALoggableUserInfo userInfo,
            int[] userTurns,
            PrintStream printStream
            ) {
        
        OperationMode operationMode = OperationMode.valueOf( 
                AMainConfigParamProcessor.getInstance().getStringParam( Parameters.OPERATION_MODE )
                );
        if ( operationMode != OperationMode.REPLAY ) {
            return;
        }
        
        printStream.println( "Cmd\tThinkT\tInputP\tOutputP\tInputT\tOutputT" );
        for ( int i = 0; i < userTurns.length; i++ ) {
            String outputLine = "" + i;
            
            int inputtingUserIndex = userTurns[ i ];
            int inputtingUserSeqId = ALoggerUtils.getSeqIdForUserOfInputCommand(
                    inputtingUserIndex,
                    i,
                    userTurns
                    );
            
            if ( inputtingUserIndex == userInfo.getUserIndex() ) {
                long thinkTimeLong = userInfo.getThinkTime(
                        inputtingUserSeqId
                        );
                double thinkTime = MathUtils.round( (double) ( thinkTimeLong / 1000 ) / (double) 1000, 3 );
                outputLine += "\t" + thinkTime;
            }
            else {
                outputLine += "\t" + "\t";
            }

            long inputProcTimeLong = userInfo.getInputProcessingTime(
                    inputtingUserIndex,
                    inputtingUserSeqId
                    );
            if ( inputProcTimeLong == -1 ) {
                outputLine += "\t" + "\t";
            }
            else {
                double inputProcTime = MathUtils.round( (double) ( inputProcTimeLong / 1000 ) / (double) 1000, 3 );
                outputLine += "\t" + inputProcTime;
            }
            
            long outputProcTimeLong = userInfo.getOutputProcessingTime(
                    inputtingUserIndex,
                    inputtingUserSeqId
                    );
            if ( outputProcTimeLong == -1 ) {
                outputLine += "\t" + "\t";
            }
            else {
                double outputProcTime = MathUtils.round( (double) ( outputProcTimeLong / 1000 ) / (double) 1000, 3 );
                outputLine += "\t" + outputProcTime;
            }
            
            long inputTransTimeLong = userInfo.getInputTransmissionTime(
                    inputtingUserIndex,
                    inputtingUserSeqId
                    );
            if ( inputTransTimeLong == -1 ) {
                outputLine += "\t" + "\t";
            }
            else {
                double inputTransTime = MathUtils.round( (double) ( inputTransTimeLong / 1000 ) / (double) 1000, 3 );
                outputLine += "\t" + inputTransTime;
            }

            long outputTransTimeLong = userInfo.getOutputTransmissionTime(
                    inputtingUserIndex,
                    inputtingUserSeqId
                    );
            if ( outputTransTimeLong == -1 ) {
                outputLine += "\t" + "\t";
            }
            else {
                double outputTransTime = MathUtils.round( (double) ( outputTransTimeLong / 1000 ) / (double) 1000, 3 );
                outputLine += "\t" + outputTransTime;
            }

            printStream.println( outputLine );
        }
        
    }
    
}
