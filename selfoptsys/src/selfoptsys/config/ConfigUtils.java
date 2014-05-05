package selfoptsys.config;

import java.util.*;
import winsyslib.WinSysLibUtilities;
import commonutils.scheduling.*;
import commonutils.basic2.*;


public class ConfigUtils {

    public static void setProcessorCoreAffinityMask() {

        ConfigParamProcessor mainCpp = AMainConfigParamProcessor.getInstance();
        int[] temp = new int[ 1 ];
        
        List<Integer> coresToUse = new LinkedList<Integer>();
        SchedulingUtils.addCoresToUseFromSpec(
                coresToUse,
                mainCpp.getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS )
                );

        int coreToUseForProcessingThread = mainCpp.getIntParam( Parameters.CORE_TO_USE_FOR_PROCESSING_TASK_THREAD );
        temp[ 0 ] = coreToUseForProcessingThread;
        SchedulingUtils.addCoresToUseFromSpec(
                coresToUse,
                temp
                );
        
        int coreToUseForTransmissionThread = mainCpp.getIntParam( Parameters.CORE_TO_USE_FOR_TRANSMISSION_TASK_THREAD );
        temp[ 0 ] = coreToUseForTransmissionThread;
        SchedulingUtils.addCoresToUseFromSpec(
                coresToUse,
                temp
                );
        
        int[] coresToUseIntArray = new int[ coresToUse.size() ];
        for ( int i = 0; i < coresToUse.size(); i++ ) {
            coresToUseIntArray[i] = coresToUse.get( i );
        }
        
        WinSysLibUtilities.setProcessorsToUseForProcess_0Based( coresToUseIntArray );
        
    }
    
    public static int getNumCoresUsedByTransAndProcThreads(
            int[] coresToUseForProcessingThreads,
            int[] coresToUseForTransmissionThreads
            ) {

        List<Integer> coresToUse = new LinkedList<Integer>();
        SchedulingUtils.addCoresToUseFromSpec(
                coresToUse,
                coresToUseForProcessingThreads
                );
        SchedulingUtils.addCoresToUseFromSpec(
                coresToUse,
                coresToUseForTransmissionThreads
                );
        
        return coresToUse.size();
    }
    
}
