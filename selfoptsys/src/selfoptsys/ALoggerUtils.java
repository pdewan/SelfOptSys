package selfoptsys;


public class ALoggerUtils {
    
    public static int getSeqIdOfFinalInputCommandForUser(
            int userIndex,
            int[] userTurns
            ) {
        int seqId = -1;
        
        for ( int i = 0; i < userTurns.length; i++ ) {
            if ( userTurns[ i ] == userIndex ) {
                seqId++;
            }
        }
        
        return seqId;
    }
    
    public static int getSeqIdForUserOfInputCommand(
            int userIndex,
            int inputNumber,
            int[] userTurns
            ) {
        int seqId = -1;
        
        for ( int i = 0; i <= inputNumber; i++ ) {
            if ( userTurns[ i ] == userIndex ) {
                seqId++;
            }
        }
        
        return seqId;
    }
    

}
