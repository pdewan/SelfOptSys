package commonutils.scheduling;

import java.util.*;
import winsyslib.*;


public class SchedulingUtils {

    public static void addCoresToUseFromSpec(
            List<Integer> coresToUse,
            int[] specedCoresToUse
            ) {
        int maxCoreNum = WinSysLibUtilities.getNumProcessors() - 1;
        
        for ( int i = 0; i < specedCoresToUse.length; i++ ) {
            int nextCoreToUse = specedCoresToUse[i];
            if ( nextCoreToUse > maxCoreNum ) {
                nextCoreToUse = maxCoreNum;
            }
            if ( !coresToUse.contains( nextCoreToUse ) ) {
                int insertPos = 0;
                for ( int j = 0; j < coresToUse.size(); j++ ) {
                    if ( coresToUse.get( j ) > nextCoreToUse ) {
                        break;
                    }
                    insertPos++;
                }
                coresToUse.add( insertPos, nextCoreToUse );
            }
        }
    }
    
    public static int pickOneCoreToUseForThreadFromPossibleCores(
            int[] possibleCoresToUseForThread
            ) {
        int seed = (int) ( System.nanoTime() % 1000000000 );
        Random rand = new Random( seed );
        int index = rand.nextInt( possibleCoresToUseForThread.length );
        return possibleCoresToUseForThread[ index ];
    }
    
    public static int[] parseCoresToUseFromSpec(
            int[] specedCoresToUse
            ) {
        List<Integer> coresToUse = new LinkedList<Integer>();
        SchedulingUtils.addCoresToUseFromSpec(
                coresToUse,
                specedCoresToUse
                );

        int[] coresToUseArray = new int[ coresToUse.size() ];
        for ( int i = 0; i < coresToUse.size(); i++ ) {
            coresToUseArray[ i ] = coresToUse.get( i );
        }
        
        return coresToUseArray;
    }
    
}
